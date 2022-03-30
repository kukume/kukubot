package me.kuku.yuq.logic

import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.kuku.yuq.entity.BaiduEntity
import me.kuku.yuq.entity.BaiduService
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.utils.*
import org.jsoup.Jsoup
import java.net.URLEncoder
import javax.inject.Inject

class BaiduLogic @Inject constructor(
    private val baiduService: BaiduService
) {
    private val appId = 716027609L
    private val daId = 383
    private val ptAid = 100312028L

    suspend fun getQrcode(): QqLoginQrcode {
        return QqQrCodeLoginUtils.getQrCode(appId, daId, ptAid)
    }

    suspend fun checkQrcode(qrcode: QqLoginQrcode): Result<BaiduEntity> {
        val checkRes = QqQrCodeLoginUtils.checkQrCode(appId, daId, ptAid,
            "https://graph.qq.com/oauth2.0/login_jump", qrcode.sig)
        if (checkRes.isFailure) return Result.failure(checkRes.code, checkRes.message)
        else {
            val response =
                OkHttpUtils.get("https://passport.baidu.com/phoenix/account/startlogin?type=15&tpl=mn&u=https%3A%2F%2Fwww.baidu.com%2F&display=page&act=implicit&xd=https%3A%2F%2Fwww.baidu.com%2Fcache%2Fuser%2Fhtml%2Fxd.html%23display%3Dpopup&fire_failure=1")
            response.close()
            val cookie = OkUtils.cookie(response)
            val mKey = OkUtils.cookie(cookie, "mkey")
            val qqLoginPojo = checkRes.data
            val result = QqQrCodeLoginUtils.authorize(
                qqLoginPojo,
                ptAid,
                System.currentTimeMillis().toString(),
                "https://passport.baidu.com/phoenix/account/afterauth?mkey=$mKey&tpl=mn"
            )
            if (result.isFailure) return Result.failure(result.message)
            else {
                val url = result.data
                val baiduResponse = OkHttpUtils.get(url, mapOf("referer" to "https://graph.qq.com/",
                    "cookie" to cookie, "user-agent" to UA.PC.value)
                )
                baiduResponse.close()
                val resCookie = OkUtils.cookie(baiduResponse)
                val sToken = OkUtils.cookie(resCookie, "STOKEN")!!
                val bdUss = OkUtils.cookie(resCookie, "BDUSS")!!
                return Result.success(BaiduEntity().also {
                    it.cookie = resCookie
                    it.sToken = sToken
                    it.bdUss = bdUss
                })
            }
        }
    }

    fun ybbSign(baiduEntity: BaiduEntity): Result<Void> {
        val time = System.currentTimeMillis()
        val tenTime = time / 1000
        val jsonObject = JSONObject()
        jsonObject["end_time"] = tenTime
        jsonObject["start_time"] = tenTime
        jsonObject["task"] = 1
        val map: MutableMap<String, String> = HashMap()
        map["X-Channel-Name"] = "vivo"
        map["X-Device-Name"] = "android"
        map["X-Client-Version"] = "2.0.13"
        map["X-System-Version"] = "29"
        map["X-Auth-Timestamp"] = time.toString()
        map["cookie"] = baiduEntity.cookie
        val resultJsonObject = OkHttpUtils.postJson(
            "https://api-gt.baidu.com/v1/server/task",
            OkUtils.json(jsonObject), OkHttpUtils.addHeaders(map)
        )
        return if (resultJsonObject.getBoolean("success")) Result.success("观看广告成功！", null)
        else Result.failure(resultJsonObject.getJSONObject("errors").getString("message_cn"))
    }

    private fun getSToken(baiduEntity: BaiduEntity, url: String): String {
        val cookie = baiduEntity.cookie
        val response = OkHttpUtils.get(url, OkUtils.cookie(cookie)).apply { close() }
        if (response.code != 302) throw RuntimeException("您的百度cookie已失效！")
        val firstUrl = response.header("location")!!
        val firstResponse = OkHttpUtils.get(firstUrl, OkUtils.cookie(cookie)).apply { close() }
        val finallyUrl = firstResponse.header("location")!!
        val finallyResponse = OkHttpUtils.get(finallyUrl, OkUtils.cookie(cookie)).apply { close() }
        return OkUtils.cookie(finallyResponse, "STOKEN")!!
    }

    suspend fun tieBaSign(baiduEntity: BaiduEntity): Result<Void> {
        var sToken = baiduEntity.tieBaSToken
        val url = "http://tieba.baidu.com/f/like/mylike?v=${System.currentTimeMillis()}"
        if (sToken.isEmpty()){
            sToken = getSToken(baiduEntity, url)
            baiduEntity.tieBaSToken = sToken
            baiduService.save(baiduEntity)
        }
        val headers = mapOf("user-agent" to UA.PC.value, "cookie" to baiduEntity.tieBaSToken)
        val likeHtml = OkHttpUtils.getStr(url,
            headers)
        val trElements = Jsoup.parse(likeHtml).getElementsByTag("tr")
        val list = mutableListOf<String>()
        for (tr in trElements) {
            val a = tr.getElementsByTag("a")
            if (a.isNotEmpty()) list.add(a[0].attr("title"))
        }
        for (s in list) {
            delay(1000)
            val html = withContext(Dispatchers.IO) {
                OkHttpUtils.getStr("https://tieba.baidu.com/f?kw=${URLEncoder.encode(s, "utf-8")}&fr=index", headers)
            }
            val tbs = MyUtils.regex("'tbs': \"", "\"", html)!!
            val jsonObject = OkHttpUtils.postJson("https://tieba.baidu.com/sign/add", mapOf("ie" to "utf-8", "kw" to s, "tbs" to tbs),
                headers)
            if (!arrayOf(1101, 0).contains(jsonObject.getInteger("no"))) return Result.failure(jsonObject.getString("error"))
        }
        return Result.success("贴吧签到成功！", null)
    }
}
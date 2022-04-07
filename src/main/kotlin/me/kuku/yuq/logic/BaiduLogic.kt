package me.kuku.yuq.logic

import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.delay
import me.kuku.yuq.entity.BaiduEntity
import me.kuku.yuq.entity.BaiduService
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.utils.*
import org.jsoup.Jsoup
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
                OkHttpKtUtils.get("https://passport.baidu.com/phoenix/account/startlogin?type=15&tpl=mn&u=https%3A%2F%2Fwww.baidu.com%2F&display=page&act=implicit&xd=https%3A%2F%2Fwww.baidu.com%2Fcache%2Fuser%2Fhtml%2Fxd.html%23display%3Dpopup&fire_failure=1")
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
                val baiduResponse = OkHttpKtUtils.get(url, mapOf("referer" to "https://graph.qq.com/",
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

    private fun ybbDefaultHeader(): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        map["X-Channel-Name"] = "xiaomi"
        map["X-Device-Name"] = "android"
        map["X-Client-Version"] = "2.3.14"
        map["X-System-Version"] = "31"
        map["X-Auth-Timestamp"] = System.currentTimeMillis().toString()
        return map
    }

    suspend fun ybbWatchAd(baiduEntity: BaiduEntity): Result<Void> {
        val preJsonObject = OkHttpKtUtils.getJson("https://api-gt.baidu.com/v1/server/task?version=v2", ybbDefaultHeader().also {
            it["cookie"] = baiduEntity.cookie
        })
        if (!preJsonObject.getBoolean("success")) return Result.failure(preJsonObject.getJSONObject("errors").getString("message_cn"))
        val preResult = preJsonObject.getJSONArray("result")
        val ll = preResult.map { it as JSONObject }.filter { it.getString("name") == "看视频送时长" }
        if (ll.isEmpty()) return Result.failure("没有这个任务")
        val sign = ll[0].getString("sign")
        val time = System.currentTimeMillis()
        val tenTime = time / 1000
        val jsonObject = JSONObject()
        jsonObject["end_time"] = tenTime
        jsonObject["start_time"] = tenTime
        jsonObject["task"] = 1
        jsonObject["sign"] = sign
        val resultJsonObject = OkHttpKtUtils.postJson(
            "https://api-gt.baidu.com/v1/server/task",
            OkUtils.json(jsonObject), ybbDefaultHeader().also {
                it["cookie"] = baiduEntity.cookie
            }
        )
        return if (resultJsonObject.getBoolean("success")) Result.success("观看广告成功！", null)
        else Result.failure(resultJsonObject.getJSONObject("errors").getString("message_cn"))
    }

    suspend fun ybbSign(baiduEntity: BaiduEntity): Result<Void> {
        val map = ybbDefaultHeader()
        map["cookie"] = baiduEntity.cookie
        map["referer"] = "https://ybb.baidu.com/m/pages/h5/sign-activity?channel=xiaomi&device=android&appversion=2.3.14&cuid=8D795D0D8C8AB781BD0E0B807B0B1B0F%7CVCUIVQGDM&systemversion=31"
        map["user-agent"] = "Mozilla/5.0 (Linux; Android 12; M2007J3SC Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/100.0.4896.79 Mobile Safari/537.36 com.baidu.ybb/2.3.14"
        val jsonObject = OkHttpKtUtils.postJson("https://ybb.baidu.com/api/v1/server/scores",
            OkUtils.json("""{"type": "daily"}"""), map)
        return if (jsonObject.getBoolean("success")) Result.success()
        else Result.failure(jsonObject.getJSONObject("errors").getString("message_cn"))
    }

    private suspend fun getSToken(baiduEntity: BaiduEntity, url: String): String {
        val cookie = baiduEntity.cookie
        val response = OkHttpKtUtils.get(url, OkUtils.cookie(cookie)).apply { close() }
        if (response.code != 302) throw RuntimeException("您的百度cookie已失效！")
        val firstUrl = response.header("location")!!
        val firstResponse = OkHttpKtUtils.get(firstUrl, OkUtils.cookie(cookie)).apply { close() }
        val finallyUrl = firstResponse.header("location")!!
        val finallyResponse = OkHttpKtUtils.get(finallyUrl, OkUtils.cookie(cookie)).apply { close() }
        return OkUtils.cookie(finallyResponse, "STOKEN")!!
    }

    suspend fun tieBaSign(baiduEntity: BaiduEntity): Result<Void> {
        var sToken = baiduEntity.config.tieBaSToken
        val url = "http://tieba.baidu.com/f/like/mylike?v=${System.currentTimeMillis()}"
        if (sToken.isEmpty()){
            sToken = getSToken(baiduEntity, url)
            baiduEntity.config.tieBaSToken = sToken
            baiduService.save(baiduEntity)
        }
        val headers = mapOf("user-agent" to UA.PC.value, "cookie" to baiduEntity.config.tieBaSToken)
        val likeHtml = OkHttpKtUtils.getStr(url,
            headers)
        val trElements = Jsoup.parse(likeHtml).getElementsByTag("tr")
        val list = mutableListOf<String>()
        for (tr in trElements) {
            val a = tr.getElementsByTag("a")
            if (a.isNotEmpty()) list.add(a[0].attr("title"))
        }
        for (s in list) {
            delay(1000)
            val html =
                OkHttpKtUtils.getStr("https://tieba.baidu.com/f?kw=${s.toUrlEncode()}&fr=index", headers)
            val tbs = MyUtils.regex("'tbs': \"", "\"", html)!!
            val jsonObject = OkHttpKtUtils.postJson("https://tieba.baidu.com/sign/add", mapOf("ie" to "utf-8", "kw" to s, "tbs" to tbs),
                headers)
            if (!arrayOf(1101, 0).contains(jsonObject.getInteger("no"))) return Result.failure(jsonObject.getString("error"))
        }
        return Result.success("贴吧签到成功！", null)
    }
}
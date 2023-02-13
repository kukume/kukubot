package me.kuku.mirai.logic

import kotlinx.coroutines.delay
import me.kuku.pojo.CommonResult
import me.kuku.pojo.UA
import me.kuku.mirai.entity.BaiduEntity
import me.kuku.mirai.entity.BaiduService
import me.kuku.utils.*
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import java.util.*

@Service
class BaiduLogic (
    private val baiduService: BaiduService
) {

    suspend fun getQrcode(): BaiduQrcode {
        val uuid = UUID.randomUUID().toString()
        val jsonNode = OkHttpKtUtils.getJsonp("https://passport.baidu.com/v2/api/getqrcode?lp=pc&qrloginfrom=pc&gid=$uuid&callback=tangram_guid_${System.currentTimeMillis()}&apiver=v3&tt=${System.currentTimeMillis()}&tpl=mn&logPage=traceId:pc_loginv5_1653405990,logPage:loginv5&_=${System.currentTimeMillis()}")
        val url = "https://" + jsonNode.getString("imgurl")
        val sign = jsonNode.getString("sign")
        return BaiduQrcode(url, sign, uuid)
    }

    suspend fun checkQrcode(baiduQrcode: BaiduQrcode): CommonResult<BaiduEntity> {
        val jsonNode =
            OkHttpKtUtils.getJsonp("https://passport.baidu.com/channel/unicast?channel_id=${baiduQrcode.sign}&gid=${baiduQrcode.uuid}&tpl=mn&_sdkFrom=1&callback=tangram_guid_${System.currentTimeMillis()}&apiver=v3&tt=${System.currentTimeMillis()}&_=${System.currentTimeMillis()}")
        return when (jsonNode.getInteger("errno")) {
            1 -> CommonResult.failure("未扫码或已失效")
            0 -> {
                val ss = jsonNode.getString("channel_v").toJsonNode()
                if (ss.getInteger("status") == 0) {
                    val v = ss.getString("v")
                    val response = OkHttpKtUtils.get("https://passport.baidu.com/v3/login/main/qrbdusslogin?v=${System.currentTimeMillis()}&bduss=$v")
                    response.close()
                    val cookie = OkUtils.cookie(response)
                    CommonResult.success(BaiduEntity().also {
                        it.cookie = cookie
                    })
                } else CommonResult.failure("已扫码")
            }
            else -> CommonResult.failure("未知错误")
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

    suspend fun ybbWatchAd(baiduEntity: BaiduEntity, version: String = "v2"): String {
        val preJsonNode = OkHttpKtUtils.getJson("https://api-gt.baidu.com/v1/server/task?version=$version", ybbDefaultHeader().also {
            it["cookie"] = baiduEntity.cookie
        })
        if (!preJsonNode.getBoolean("success")) error(preJsonNode["errors"]["message_cn"].asText())
        val preResult = preJsonNode["result"]
        val ll = preResult.filter { it.getString("name") in listOf("看视频送时长", "看视频送积分") }
        if (ll.isEmpty()) error("没有这个任务")
        val sign = ll[0].getString("sign")
        val time = System.currentTimeMillis()
        val tenTime = time / 1000
        val jsonNode = Jackson.createObjectNode()
        jsonNode.put("end_time", tenTime)
        jsonNode.put("start_time", tenTime)
        jsonNode.put("task", ll[0].getInteger("id"))
        jsonNode.put("sign", sign)
        val resultJsonObject = OkHttpKtUtils.postJson(
            "https://api-gt.baidu.com/v1/server/task${if (version.contains("v3")) "?version=v3" else ""}",
            OkUtils.json(jsonNode), ybbDefaultHeader().also {
                it["cookie"] = baiduEntity.cookie
            }
        )
        return if (resultJsonObject.getBoolean("success")) "观看广告成功！"
        else error(resultJsonObject["errors"]["message_cn"].asText())
    }

    suspend fun ybbSign(baiduEntity: BaiduEntity): String {
        val map = ybbDefaultHeader()
        map["cookie"] = baiduEntity.cookie
        map["referer"] = "https://ybb.baidu.com/m/pages/h5/sign-activity?channel=xiaomi&device=android&appversion=2.3.14&cuid=8D795D0D8C8AB781BD0E0B807B0B1B0F%7CVCUIVQGDM&systemversion=31"
        map["user-agent"] = "Mozilla/5.0 (Linux; Android 12; M2007J3SC Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/100.0.4896.79 Mobile Safari/537.36 com.baidu.ybb/2.3.14"
        val jsonObject = OkHttpKtUtils.postJson("https://ybb.baidu.com/api/v1/server/scores",
            OkUtils.json("""{"type": "daily"}"""), map)
        return if (jsonObject.getBoolean("success")) "游帮帮加速器签到成功"
        else error(jsonObject["errors"]["message_cn"].asText())
    }

    private suspend fun getSToken(baiduEntity: BaiduEntity, url: String): String {
        val cookie = baiduEntity.cookie
        val headers = mapOf("cookie" to cookie, "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36")
        val response = OkHttpKtUtils.get("https://wappass.baidu.com/v3/login/api/auth?jump=&notjump=1&return_type=3&tpl=tb&u=${url.toUrlEncode()}", headers).apply { close() }
        if (response.code !in listOf(302, 301)) throw RuntimeException("您的百度cookie已失效！")
        val firstUrl = response.header("location")!!
        val firstResponse = OkHttpKtUtils.get(firstUrl, headers).apply { close() }
        return OkUtils.cookie(firstResponse, "STOKEN")!!
    }

    private suspend fun saveSToken(baiduEntity: BaiduEntity, url: String): String {
        val sToken = getSToken(baiduEntity, url)
        baiduEntity.tieBaSToken = sToken
        baiduService.save(baiduEntity)
        return sToken
    }

    suspend fun tieBaSign(baiduEntity: BaiduEntity): String {
        val sToken = baiduEntity.tieBaSToken
        val url = "https://tieba.baidu.com/f/like/mylike?v=${System.currentTimeMillis()}"
        if (sToken.isEmpty()) saveSToken(baiduEntity, url)
        val headers = mapOf("user-agent" to UA.PC.value, "cookie" to baiduEntity.teiBaCookie())
        val likeHtml = OkHttpKtUtils.getStr(url,
            headers)
        if (likeHtml.isEmpty()) saveSToken(baiduEntity, url)
        val trElements = Jsoup.parse(likeHtml).getElementsByTag("tr")
        val list = mutableListOf<String>()
        for (tr in trElements) {
            val a = tr.getElementsByTag("a")
            if (a.isNotEmpty()) list.add(a[0].attr("title"))
        }
        for (s in list) {
            delay(5000)
            val html =
                OkHttpKtUtils.getStr("https://tieba.baidu.com/f?kw=${s.toUrlEncode()}&fr=index", headers)
            val tbs = MyUtils.regex("'tbs': \"", "\"", html)!!
            val jsonObject = OkHttpKtUtils.postJson("https://tieba.baidu.com/sign/add", mapOf("ie" to "utf-8", "kw" to s, "tbs" to tbs),
                headers)
            if (!arrayOf(1101, 0).contains(jsonObject.getInteger("no"))) error(jsonObject.getString("error"))
        }
        return "贴吧签到成功！"
    }
}

data class BaiduQrcode(val image: String, val sign: String, val uuid: String)

package me.kuku.mirai.logic

import me.kuku.pojo.CommonResult
import me.kuku.pojo.UA
import me.kuku.mirai.entity.KuGouEntity
import me.kuku.utils.*
import java.util.*

data class KuGouQrcode (
    var url: String,
    var qrcode: String,
    var mid: String
)
object KuGouLogic {
    private fun e(): String {
        return Integer.toHexString(((65536 * (1 + Math.random())).toInt()))
    }

    fun mid(): String{
        val s = e() + e() + "-" + e() + "-" + e() + "-" + e() + "-" + e() + e() + e()
        return MD5Utils.toMD5(s)
    }

    private fun clientTime(): Int{
        return (System.currentTimeMillis() / 1e3).toInt()
    }

    private fun signature(map: MutableMap<String, String>): String{
        val list = mutableListOf<String>()
        val sb = StringBuilder()
        for ((k, v) in map){
            list.add(v)
            sb.append("$k=$v&")
        }
        list.sort()
        val s = StringUtils.join(list, "")
        val signature = MD5Utils.toMD5(s)
        sb.append("signature=$signature")
        return sb.toString()
    }

    private fun signature(ss: String, map: MutableMap<String, String>, other: String = ""): String {
        val list = LinkedList<String>()
        val sb = StringBuilder()
        for ((k, v) in map){
            list.add("$k=$v")
            sb.append("$k=$v&")
        }
        list.sort()
        list.addFirst(ss)
        list.add(other)
        list.add(ss)
        val s = StringUtils.join(list, "")
        val signature = MD5Utils.toMD5(s)
        sb.append("signature=$signature")
        return sb.toString()
    }

    private fun signature2(map: MutableMap<String, String>, other: String = ""): String{
        return signature("NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt", map, other)
    }

    private fun signature3(map: MutableMap<String, String>, other: String = ""): String{
        return signature("LnT6xpN3khm36zse0QzvmgTZ3waWdRSA", map, other)
    }

    suspend fun getQrcode(mid: String?): KuGouQrcode {
        val newMid = mid ?: mid()
        val map = mutableMapOf(
            "appid" to "1014", "clientver" to "8131", "clienttime" to clientTime().toString(),
            "uuid" to newMid, "mid" to newMid, "type" to "1"
        )
        val jsonNode = OkHttpKtUtils.getJson("https://login-user.kugou.com/v1/qrcode?${signature(map)}")
        val qrcode = jsonNode["data"].getString("qrcode")
        return KuGouQrcode("https://h5.kugou.com/apps/loginQRCode/html/index.html?qrcode=$qrcode&appid=1014",
            qrcode, newMid)
    }

    suspend fun checkQrcode(kuGouQrcode: KuGouQrcode): CommonResult<KuGouEntity> {
        val map = mutableMapOf("appid" to "1014", "clientver" to "8131",
            "clienttime" to clientTime().toString(), "qrcode" to kuGouQrcode.qrcode, "dfid" to "-",
            "mid" to kuGouQrcode.mid, "plat" to "4", "uuid" to kuGouQrcode.mid)
        val jsonNode = OkHttpKtUtils.getJson(
            "https://login-user.kugou.com/v1/get_userinfo_qrcode?${signature(map)}"
        )
        val dataStatus = jsonNode["data"].getInteger("status")
        return when (dataStatus) {
            1, 2 -> CommonResult.failure(code = 0, message = "二维码未被扫描或已被扫描")
            0 -> CommonResult.failure("二维码已失效！")
            4 -> {
                val token = jsonNode["data"].getString("token")
                val userid = jsonNode["data"].getLong("userid")
                val response =
                    OkHttpKtUtils.get("https://login-user.kugou.com/v1/autologin?a_id=1014&userid=$userid&t=$token&ct=${clientTime()}&callback=qrcodeLoginCallback&domain=kugou.com&uuid=${kuGouQrcode.mid}&mid=$${kuGouQrcode.mid}&plat=4&dfid=-&kguser_jv=180925")
                val cookie = OkUtils.cookie(response)
                val kuGoo = OkUtils.cookie(cookie, "KuGoo")
                val kuGouEntity = KuGouEntity()
                kuGouEntity.token = token
                kuGouEntity.userid = userid
                kuGouEntity.mid = kuGouQrcode.mid
                kuGouEntity.kuGoo = kuGoo!!
                CommonResult.success(kuGouEntity)
            }
            else -> CommonResult.failure("未知的错误代码：$dataStatus")
        }
    }

    suspend fun sendMobileCode(phone: String, mid: String): CommonResult<Void> {
        val time = System.currentTimeMillis()
        val map = mutableMapOf(
            // 1058
            "appid" to "3116",
            "clientver" to "1000",
            "clienttime" to time.toString(),
            "mid" to mid,
            "uuid" to mid,
            "dfid" to "-",
            "srcappid" to "2919"
        )
        val preJsonNode = OkHttpKtUtils.postJson(
            "https://api.kukuqaq.com/exec/kuGou",
            mutableMapOf("phone" to phone, "time" to time.toString())
        )
        val params = preJsonNode.getString("params")
        val pk = preJsonNode.getString("pk")
        val mobile = phone.substring(0, 2) + "********" + phone.substring(phone.length - 1)
        val other = "{\"plat\":4,\"clienttime_ms\":$time,\"businessid\":5,\"pk\":\"$pk\",\"params\":\"$params\",\"mobile\":\"$mobile\"}"
        val jsonNode = OkHttpKtUtils.postJson(
            "https://gateway.kugou.com/v8/send_mobile_code?${signature2(map, other)}",
            OkUtils.text(other),
            mutableMapOf("x-router" to "loginservice.kugou.com", "referer" to "https://m3ws.kugou.com/",
                "user-agent" to UA.PC.value)
        )
        return if (jsonNode.getInteger("error_code") == 0) CommonResult.success()
        else CommonResult.failure(jsonNode.getString("data"))
    }

    suspend fun verifyCode(phone: String, code: String, mid: String): CommonResult<KuGouEntity> {
        val time = clientTime()
        val map = mutableMapOf(
            "appid" to "3116",
            "clientver" to "10",
            "clienttime" to time.toString(),
            "mid" to mid,
            "uuid" to mid,
            "dfid" to "-",
            "srcappid" to "2919"
        )
        val other = "{\"plat\":4,\"mobile\":\"$phone\",\"code\":\"$code\",\"expire_day\":60,\"support_multi\":1,\"userid\":\"\",\"force_login\":0}"
        val response = OkHttpKtUtils.post(
            "https://login-user.kugou.com/v2/loginbyverifycode/?${signature2(map, other)}",
            OkUtils.text(other),
            mutableMapOf("x-router" to "loginservice.kugou.com", "referer" to "https://m3ws.kugou.com/",
                "user-agent" to UA.PC.value)
        )
        val jsonNode = OkUtils.json(response)
        return if (jsonNode.getInteger("error_code") == 0) {
            val cookie = OkUtils.cookie(response)
            val kuGoo = jsonNode["data"].getString("value")
            val token = OkUtils.cookie(cookie, "t")
            val userid = OkUtils.cookie(cookie, "KugooID")!!
            CommonResult.success(KuGouEntity().also {
                it.token = token!!
                it.userid = userid.toLong()
                it.kuGoo = kuGoo
                it.mid = mid
            })
        }
        else CommonResult.failure(jsonNode.getString("data"))
    }

    suspend fun login(username: String, password: String, mid: String?): CommonResult<KuGouEntity> {
        val newMid = mid ?: mid()
        val md5Pwd = MD5Utils.toMD5(password)
        val params = "appid=1058&username=$username&pwd=$md5Pwd&code=&ticket=&clienttime=${clientTime()}&expire_day=60&autologin=false&redirect_uri=&state=&callback=loginModule.loginCallback&login_ver=1&mobile=&mobile_code=&plat=4&dfid=-&mid=$newMid&kguser_jv=180925"
        val headers = OkUtils.headers("", "https://m3ws.kugou.com/", UA.PC)
        val response =
            OkHttpKtUtils.get("https://login-user.kugou.com/v1/login/?$params", headers)
        val jsonNode = OkUtils.jsonp(response)
        return when (jsonNode["errorCode"]?.asInt()){
            30791 -> {
                // 验证码
                CommonResult.failure("需要验证验证码，请使用短信验证码登陆")
            }
            null -> {
                val cookie = OkUtils.cookie(response)
                val kuGoo = OkUtils.cookie(cookie, "KuGoo")
                val token = jsonNode.getString("token")
                val userid = jsonNode.getLong("userid")
                CommonResult.success(KuGouEntity().also {
                    it.token = token
                    it.userid = userid
                    it.kuGoo = kuGoo!!
                    it.mid = newMid
                })
            }
            30768 -> CommonResult.failure("需要短信验证码！请直接使用短信验证码登录！<酷狗验证码 phone>")
            else -> CommonResult.failure(jsonNode.getString("errorMsg"))
        }
    }

    suspend fun musicianSign(kuGouEntity: KuGouEntity): String {
        // 1014
        // 1058
        val kuGoo = kuGouEntity.kuGoo
        val aId = MyUtils.regex("a_id=", "&", kuGoo)!!
        val time = System.currentTimeMillis().toString()
        val map = mutableMapOf("appid" to aId, "token" to kuGouEntity.token,
            "kugouid" to kuGouEntity.userid.toString(), "srcappid" to "2919", "clientver" to "20000",
            "clienttime" to time, "dfid" to "-",
            "mid" to time, "uuid" to time)
        val jsonNode =
            OkHttpKtUtils.postJson("https://h5activity.kugou.com/v1/musician/do_signed?${signature2(map)}", mapOf())
        return if (jsonNode.getInteger("errcode") == 0) "酷狗音乐人签到成功！"
        else error("酷狗音乐人签到失败！" + jsonNode.getString("errmsg"))
    }

    suspend fun listenMusic(kuGouEntity: KuGouEntity): String {
//        val aId = MyUtils.regex("a_id=", "&", kuGouEntity.kuGoo)!!
        val map = mutableMapOf("userid" to kuGouEntity.userid.toString(), "token" to kuGouEntity.token,
            "appid" to "3116", "clientver" to "10547", "clienttime" to (System.currentTimeMillis() / 1000).toString(),
            "mid" to kuGouEntity.mid, "uuid" to MyUtils.randomLetter(32), "dfid" to "-")
        val other = """{"mixsongid":273263741}"""
        val jsonNode = OkHttpKtUtils.postJson("https://gateway.kugou.com/v2/report/listen_song?${signature3(map, other)}",
            OkUtils.text(other), mapOf("x-router" to "youth.kugou.com", "User-Agent" to "Android12-1070-10536-130-0-ReportPlaySongToServerProtocol-wifi"))
        val code = jsonNode.getInteger("error_code")
        return if (code == 0 || code == 130012) "成功"
        else error(jsonNode.getString("error_msg"))
    }
}

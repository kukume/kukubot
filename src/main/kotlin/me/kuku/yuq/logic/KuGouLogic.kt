package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.utils.*
import me.kuku.yuq.entity.KuGouEntity
import java.util.*

data class KuGouQrcode (
    var url: String,
    var qrcode: String,
    var mid: String
)

@AutoBind
interface KuGouLogic{
    fun mid(): String
    fun getQrcode(mid: String? = null): KuGouQrcode
    fun checkQrcode(kuGouQrcode: KuGouQrcode): Result<KuGouEntity>
    fun sendMobileCode(phone: String, mid: String): Result<Void>
    fun verifyCode(phone: String, code: String, mid: String): Result<KuGouEntity>
    fun login(username: String, password: String, mid: String? = null): Result<KuGouEntity>
    fun musicianSign(kuGouEntity: KuGouEntity): Result<Void>
}

class KuGouLogicImpl: KuGouLogic{

    private fun e(): String {
        return Integer.toHexString(((65536 * (1 + Math.random())).toInt()))
    }

    override fun mid(): String{
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

    private fun signature2(map: MutableMap<String, String>, other: String = ""): String{
        val ss = "NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt"
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

    override fun getQrcode(mid: String?): KuGouQrcode {
        val newMid = mid ?: mid()
        val map = mutableMapOf(
            "appid" to "1014", "clientver" to "8131", "clienttime" to clientTime().toString(),
            "uuid" to newMid, "mid" to newMid, "type" to "1"
        )
        val jsonObject = OkHttpUtils.getJson("https://login-user.kugou.com/v1/qrcode?${signature(map)}")
        val qrcode = jsonObject.getJSONObject("data").getString("qrcode")
        return KuGouQrcode("https://h5.kugou.com/apps/loginQRCode/html/index.html?qrcode=$qrcode&appid=1014",
            qrcode, newMid)
    }

    override fun checkQrcode(kuGouQrcode: KuGouQrcode): Result<KuGouEntity> {
        val map = mutableMapOf("appid" to "1014", "clientver" to "8131",
            "clienttime" to clientTime().toString(), "qrcode" to kuGouQrcode.qrcode, "dfid" to "-",
            "mid" to kuGouQrcode.mid, "plat" to "4", "uuid" to kuGouQrcode.mid)
        val jsonObject = OkHttpUtils.getJson(
            "https://login-user.kugou.com/v1/get_userinfo_qrcode?${signature(map)}"
        )
        val dataStatus = jsonObject.getJSONObject("data").getInteger("status")
        return when (dataStatus) {
            1, 2 -> Result.failure(0, "二维码未被扫描或已被扫描")
            0 -> Result.failure("二维码已失效！")
            4 -> {
                val token = jsonObject.getJSONObject("data").getString("token")
                val userid = jsonObject.getJSONObject("data").getLong("userid")
                val response =
                    OkHttpUtils.get("https://login-user.kugou.com/v1/autologin?a_id=1014&userid=$userid&t=$token&ct=${clientTime()}&callback=qrcodeLoginCallback&domain=kugou.com&uuid=${kuGouQrcode.mid}&mid=$${kuGouQrcode.mid}&plat=4&dfid=-&kguser_jv=180925")
                val cookie = OkHttpUtils.getCookie(response)
                val kuGoo = OkHttpUtils.getCookie(cookie, "KuGoo")
                val kuGouEntity = KuGouEntity(
                    token = token,
                    userid = userid,
                    mid = kuGouQrcode.mid,
                    kuGoo = kuGoo
                )
                Result.success(kuGouEntity)
            }
            else -> Result.failure("未知的错误代码：$dataStatus")
        }
    }

    override fun sendMobileCode(phone: String, mid: String): Result<Void> {
        val time = System.currentTimeMillis()
        val map = mutableMapOf(
            "appid" to "1058",
            "clientver" to "1000",
            "clienttime" to time.toString(),
            "mid" to mid,
            "uuid" to mid,
            "dfid" to "-",
            "srcappid" to "2919"
        )
        val preJsonObject = OkHttpUtils.postJson(
            "https://apicf.kuku.me/tool/kuGou",
            mutableMapOf("phone" to phone, "time" to time.toString())
        )
        val params = preJsonObject.getString("params")
        val pk = preJsonObject.getString("pk")
        val mobile = phone.substring(0, 2) + "********" + phone.substring(phone.length - 1)
        val other = "{\"plat\":4,\"clienttime_ms\":$time,\"businessid\":5,\"pk\":\"$pk\",\"params\":\"$params\",\"mobile\":\"$mobile\"}"
        val jsonObject = OkHttpUtils.postJson(
            "https://gateway.kugou.com/v8/send_mobile_code?${signature2(map, other)}",
            OkHttpUtils.addText(other),
            mutableMapOf("x-router" to "loginservice.kugou.com", "referer" to "https://m3ws.kugou.com/",
                "user-agent" to UA.PC.value)
        )
        return if (jsonObject.getInteger("error_code") == 0) Result.success()
        else Result.failure(jsonObject.getString("data"))
    }

    override fun verifyCode(phone: String, code: String, mid: String): Result<KuGouEntity> {
        val time = clientTime()
        val map = mutableMapOf(
            "appid" to "1058",
            "clientver" to "10",
            "clienttime" to time.toString(),
            "mid" to mid,
            "uuid" to mid,
            "dfid" to "-",
            "srcappid" to "2919"
        )
        val other = "{\"plat\":4,\"mobile\":\"$phone\",\"code\":\"$code\",\"expire_day\":60,\"support_multi\":1,\"userid\":\"\",\"force_login\":0}"
        val response = OkHttpUtils.post(
            "https://login-user.kugou.com/v2/loginbyverifycode/?${signature2(map, other)}",
            OkHttpUtils.addText(other),
            mutableMapOf("x-router" to "loginservice.kugou.com", "referer" to "https://m3ws.kugou.com/",
                "user-agent" to UA.PC.value)
        )
        val jsonObject = OkHttpUtils.getJson(response)
        return if (jsonObject.getInteger("error_code") == 0) {
            val cookie = OkHttpUtils.getCookie(response)
            val kuGoo = jsonObject.getJSONObject("data").getString("value")
            val token = OkHttpUtils.getCookie(cookie, "t")
            val userid = OkHttpUtils.getCookie(cookie, "KugooID")!!
            Result.success(KuGouEntity(token = token, userid = userid.toLong(), kuGoo = kuGoo, mid = mid))
        }
        else Result.failure(jsonObject.getString("data"))
    }

    override fun login(username: String, password: String, mid: String?): Result<KuGouEntity> {
        val newMid = mid ?: mid()
        val md5Pwd = MD5Utils.toMD5(password)
        var params = "appid=1058&username=$username&pwd=$md5Pwd&code=&ticket=&clienttime=${clientTime()}&expire_day=60&autologin=false&redirect_uri=&state=&callback=loginModule.loginCallback&login_ver=1&mobile=&mobile_code=&plat=4&dfid=-&mid=$newMid&kguser_jv=180925"
        val headers = OkHttpUtils.addHeaders("", "https://m3ws.kugou.com/", UA.PC)
        var response =
            OkHttpUtils.get("https://login-user.kugou.com/v1/login/?$params", headers)
        var jsonObject = OkHttpUtils.getJsonp(response)
        return when (jsonObject.getInteger("errorCode")){
            30791 -> {
                // 验证码
                val captchaJsonObject =
                    OkHttpUtils.getJsonp("https://login-user.kugou.com/v1/get_img_code?type=LoginCheckCode&appid=1058&codetype=3&t=${System.currentTimeMillis()}&callback=kgSliderVerifyCodeHandler&kguser_jv=180925",
                        headers)
                val captchaUrl = captchaJsonObject.getString("url")
                val appId = MyUtils.regex("appid=", "&", captchaUrl)
                val aSig = MyUtils.regex("(?<=asig=).*", captchaUrl)
                val res = QCloudCaptchaUtils.identify(appId, aSig)
                if (res.isSuccess){
                    val ticket = res.data.ticket
                    params = params.replace("ticket=", "ticket=$ticket")
                    response = OkHttpUtils.get("https://login-user.kugou.com/v1/login/?$params", headers)
                    jsonObject = OkHttpUtils.getJsonp(response)
                    if (!jsonObject.containsKey("errorCode")){
                        val token = jsonObject.getJSONObject("data").getString("token")
                        val userid = jsonObject.getJSONObject("data").getLong("userid")
                        val cookie = OkHttpUtils.getCookie(response)
                        val kuGoo = OkHttpUtils.getCookie(cookie, "KuGoo")
                        Result.success(KuGouEntity(token = token, userid = userid, kuGoo = kuGoo, mid = newMid))
                    }else Result.failure(jsonObject.getString("errorMsg"))
                }else Result.failure("自动过验证码失败，请重试！")
            }
            null -> {
                val cookie = OkHttpUtils.getCookie(response)
                val kuGoo = OkHttpUtils.getCookie(cookie, "KuGoo")
                val token = jsonObject.getString("token")
                val userid = jsonObject.getLong("userid")
                Result.success(KuGouEntity(token = token, userid = userid, kuGoo = kuGoo, mid = newMid))
            }
            30768 -> Result.failure("需要短信验证码！请直接使用短信验证码登录！<酷狗验证码 phone>")
            else -> Result.failure(jsonObject.getString("errorMsg"))
        }
    }

    override fun musicianSign(kuGouEntity: KuGouEntity): Result<Void> {
        // 1014
        // 1058
        val kuGoo = kuGouEntity.kuGoo ?: return Result.failure("请重新登陆酷狗！")
        val aId = MyUtils.regex("a_id=", "&", kuGoo)
        val time = System.currentTimeMillis().toString()
        val map = mutableMapOf("appid" to aId, "token" to kuGouEntity.token,
            "kugouid" to kuGouEntity.userid.toString(), "srcappid" to "2919", "clientver" to "20000",
            "clienttime" to time, "dfid" to "-",
            "mid" to time, "uuid" to time)
        val jsonObject =
            OkHttpUtils.postJson("https://h5activity.kugou.com/v1/musician/do_signed?${signature2(map)}", mapOf())
        return if (jsonObject.getInteger("errcode") == 0) Result.success("酷狗音乐人签到成功！", null)
        else Result.failure("酷狗音乐人签到失败！" + jsonObject.getString("errmsg"))
    }

}
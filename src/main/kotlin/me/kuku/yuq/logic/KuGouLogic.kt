package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.pojo.Result
import me.kuku.utils.MD5Utils
import me.kuku.utils.OkHttpUtils
import me.kuku.utils.StringUtils
import me.kuku.yuq.entity.KuGouEntity
import java.util.*

data class KuGouQrcode (
    var url: String,
    var qrcode: String,
    var mid: String
)

@AutoBind
interface KuGouLogic{
    fun getQrcode(): KuGouQrcode
    fun checkQrcode(kuGouQrcode: KuGouQrcode): Result<KuGouEntity>
    fun musicianSign(kuGouEntity: KuGouEntity): Result<Void>
    fun refresh(kuGouEntity: KuGouEntity): Result<KuGouEntity>
}

class KuGouLogicImpl: KuGouLogic{

    private fun e(): String {
        return Integer.toHexString(((65536 * (1 + Math.random())).toInt()))
    }

    private fun mid(): String{
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

    private fun signature2(map: MutableMap<String, String>): String{
        val list = LinkedList<String>()
        val sb = StringBuilder()
        for ((k, v) in map){
            list.add("$k=$v")
            sb.append("$k=$v&")
        }
        list.sort()
        list.addFirst("NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt")
        list.addLast("NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt")
        val s = StringUtils.join(list, "")
        val signature = MD5Utils.toMD5(s)
        sb.append("signature=$signature")
        return sb.toString()
    }

    override fun getQrcode(): KuGouQrcode {
        val mid = mid()
        val map = mutableMapOf(
            "appid" to "1014", "clientver" to "8131", "clienttime" to clientTime().toString(),
            "uuid" to mid, "mid" to mid, "type" to "1"
        )
        val jsonObject = OkHttpUtils.getJson("https://login-user.kugou.com/v1/qrcode?${signature(map)}")
        val qrcode = jsonObject.getJSONObject("data").getString("qrcode")
        return KuGouQrcode("https://h5.kugou.com/apps/loginQRCode/html/index.html?qrcode=$qrcode&appid=1014",
            qrcode, mid)
    }

    override fun checkQrcode(kuGouQrcode: KuGouQrcode): Result<KuGouEntity> {
        val map = mutableMapOf("appid" to "1014", "clientver" to "8131",
            "clienttime" to clientTime().toString(), "qrcode" to kuGouQrcode.qrcode, "dfid" to "-",
            "mid" to kuGouQrcode.mid, "plat" to "4", "uuid" to kuGouQrcode.mid)
        val jsonObject = OkHttpUtils.getJson(
            "https://login-user.kugou.com/v1/get_userinfo_qrcode?${signature(map)}"
        )
        val dataStatus = jsonObject.getJSONObject("data").getInteger("status")
        return if (dataStatus == 1 || dataStatus == 2)
            Result.failure(0, "二维码未被扫描或已被扫描")
        else if (dataStatus == 0)
            Result.failure("二维码已失效！")
        else if (dataStatus == 4) {
            val token = jsonObject.getJSONObject("data").getString("token")
            val userid = jsonObject.getJSONObject("data").getLong("userid")
            var kuGouEntity = KuGouEntity(
                token = token,
                userid = userid,
                mid = kuGouQrcode.mid
            )
            kuGouEntity = refresh(kuGouEntity).data
            Result.success(kuGouEntity)
        } else Result.failure("未知的错误代码：$dataStatus")
    }

    override fun musicianSign(kuGouEntity: KuGouEntity): Result<Void> {
        val time = System.currentTimeMillis().toString()
        val map = mutableMapOf("appid" to "1014", "token" to kuGouEntity.token,
            "kugouid" to kuGouEntity.userid.toString(), "srcappid" to "2919", "clientver" to "20000",
            "clienttime" to time, "dfid" to "-",
            "mid" to time, "uuid" to time)
        val jsonObject =
            OkHttpUtils.postJson("https://h5activity.kugou.com/v1/musician/do_signed?${signature2(map)}", mapOf())
        if (jsonObject.getInteger("errcode") == 0) return Result.success("酷狗音乐人签到成功！", null)
        else return Result.failure("酷狗音乐人签到失败！" + jsonObject.getString("errmsg"))
    }

    override fun refresh(kuGouEntity: KuGouEntity): Result<KuGouEntity> {
        val response =
            OkHttpUtils.get("https://login-user.kugou.com/v1/autologin?a_id=1014&userid=${kuGouEntity.userid}&t=${kuGouEntity.token}&ct=${clientTime()}&callback=qrcodeLoginCallback&domain=kugou.com&uuid=${kuGouEntity.mid}&mid=${kuGouEntity.mid}&plat=4&dfid=-&kguser_jv=180925")
        val cookie = OkHttpUtils.getCookie(response)
        val kuGoo = OkHttpUtils.getCookie(cookie, "KuGoo") ?: return Result.failure("token已失效！")
        kuGouEntity.kuGoo = kuGoo
        return Result.success(kuGouEntity)
    }
}
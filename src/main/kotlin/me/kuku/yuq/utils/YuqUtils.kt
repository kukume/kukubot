package me.kuku.yuq.utils

import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.utils.OkHttpUtils
import me.kuku.pojo.QqLoginPojo
import me.kuku.utils.OkHttpKtUtils
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.QqEntity

object YuqUtils {

    var web: OkHttpWebImpl? = null

    fun sendMessage(qqEntity: QqEntity, message: Message) {
        val groups = qqEntity.groups
        if (groups.size == 0) yuq.friends[qqEntity.qq]?.sendMessage(message)
        else {
            val groupEntity = groups.first()
            yuq.groups[groupEntity.group]?.get(qqEntity.qq)?.sendMessage(message)
        }
    }

    fun sendMessage(qqEntity: QqEntity, message: String) {
        sendMessage(qqEntity, message.toMessage())
    }

    fun sendMessage(groupEntity: GroupEntity, message: Message) {
        yuq.groups[groupEntity.group]?.sendMessage(message)
    }

    fun sendMessage(groupEntity: GroupEntity, message: String) {
        sendMessage(groupEntity, message.toMessage())
    }

    fun shortUrl(url: String): String {
        val newUrl = if (url.startsWith("http")) url else "http://$url"
        val jsonObject = OkHttpUtils.postJson("https://tool.jpa.cc/api/short_url/generate", mapOf("type" to "is_gd", "url" to newUrl))
        return if (jsonObject.getString("status") == "ok")
            jsonObject.getJSONObject("data").getString("short_url")
        else jsonObject.getString("message")
    }

    fun groupQqLoginPojo(): QqLoginPojo {
        val map = web!!.domainMap
        val qunMap = map["qun.qq.com"]!!
        val psKey = qunMap["p_skey"]!!.value
        val qqMap = map["qq.com"]!!
        val sKey = qqMap["skey"]!!.value
        val qq = yuq.botId
        return QqLoginPojo(qq, sKey, psKey)
    }

}

fun Boolean.openOrClose(): String {
    return if (this) "开启" else "关闭"
}

suspend fun hitokoto() = OkHttpKtUtils.getStr("https://v1.hitokoto.cn/?encode=text")
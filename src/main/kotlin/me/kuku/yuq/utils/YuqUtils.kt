package me.kuku.yuq.utils

import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.QqEntity

object YuqUtils {
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
        val jsonObject = OkHttpUtils.postJson("https://api.0n0.co/v1/domain/gen",
            OkHttpUtils.addJson("""
                {"urlCurrent":"$newUrl","validTime":"180"}
            """.trimIndent()))
        return if (jsonObject.getInteger("code") == 1000) jsonObject.getString("data")
        else url
    }
}


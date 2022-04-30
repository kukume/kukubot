package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.SendMessageInvalidEvent
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.yuq
import me.kuku.utils.JobManager
import me.kuku.utils.OkHttpUtils
import org.springframework.stereotype.Service

@EventListener
@Service
class MessageFailEvent {

    @Event
    fun ss(e: SendMessageInvalidEvent) {
        if (yuq.botInfo.canSendMessage()) {
            val message = e.message
            val sendTo = e.sendTo
            val ss = message.toCodeString()
            JobManager.now {
                val url = kotlin.runCatching {
                    val jsonObject = OkHttpUtils.postJson(
                        "https://api.kukuqaq.com/tool/paste",
                        mapOf("poster" to "kuku", "syntax" to "text", "content" to ss)
                    )
                    jsonObject.getJSONObject("data").getString("url")
                }.getOrDefault("Ubuntu paste url 生成失败")
                sendTo.sendMessage("消息发送失败，paste如下：\n$url")
            }
        }
    }

}
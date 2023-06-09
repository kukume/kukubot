package me.kuku.mirai.event

import io.ktor.client.request.*
import me.kuku.mirai.entity.GroupService
import me.kuku.utils.client
import me.kuku.utils.setJsonBody
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.emptyMessageChain
import org.springframework.stereotype.Component

@Component
class GroupEvent(
    private val groupService: GroupService
) {

    suspend fun GroupMessageEvent.forward() {
        val groupEntity = groupService.findByGroup(group.id)
        val chatId = groupEntity?.forward?.chatId ?: 0
        val messageThreadId = groupEntity?.forward?.messageThreadId
        if (chatId != 0L) {
            val pushBody = PushBody()
            pushBody.chatId = chatId
            pushBody.messageThreadId = messageThreadId
            val notImage = message.filterNot { it is Image }
            var newMessage = emptyMessageChain()
            notImage.forEach { newMessage = newMessage.plus(it) }
            val text = newMessage.serializeToMiraiCode()
            pushBody.message.add(PushBody.Message().also {
                it.type = PushBody.Type.TEXT
                it.content = """
                    #qq消息转发
                    群号：${group.id}
                    群名：${group.name}
                    扣扣：${sender.id}
                    名片：${sender.nameCardOrNick}
                    内容：
                    $text
                """.trimIndent()
            })
            val images = message.filterIsInstance<Image>()
            if (images.isNotEmpty()) {
                for (image in images) {
                    pushBody.message.add(PushBody.Message().also {
                        it.type = PushBody.Type.IMAGE
                        it.content = image.queryUrl()
                    })
                }
            }
            val files = message.filterIsInstance<FileMessage>()
            if (files.isNotEmpty()) {
                for (file in files) {
                    file.toAbsoluteFile(group)?.getUrl()?.let { url ->
                        pushBody.message.add(PushBody.Message().also {
                            it.type = PushBody.Type.FILE
                            it.content = url
                        })
                    }

                }
            }
            client.post("http://192.168.1.237:5461/push/chat") {
                setJsonBody(pushBody)
            }
        }
    }

}

class PushBody {
    var chatId: Long = 0
    var messageThreadId: Int? = 0
    var message: MutableList<Message> = mutableListOf()

    class Message {
        var type: Type = Type.TEXT
        var content: String = ""
    }

    enum class Type {
        TEXT, IMAGE, VIDEO, FILE
    }
}

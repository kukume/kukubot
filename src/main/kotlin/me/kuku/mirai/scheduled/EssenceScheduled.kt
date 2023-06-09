package me.kuku.mirai.scheduled

import io.ktor.client.request.*
import me.kuku.mirai.entity.EssenceService
import me.kuku.mirai.event.PushBody
import me.kuku.mirai.logic.QqLogic
import me.kuku.utils.DateTimeFormatterUtils
import me.kuku.utils.client
import me.kuku.utils.setJsonBody
import net.mamoe.mirai.Bot
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class EssenceScheduled(
    private val essenceService: EssenceService,
    private val bot: Bot
) {


    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    suspend fun syncEssence() {
        val queryList = essenceService.findAll()
        for (essenceEntity in queryList) {
            val messages = essenceEntity.messages
            val groupNo = essenceEntity.group
            val group = bot.getGroup(groupNo) ?: continue
            val essenceList = QqLogic.groupEssence(groupNo, 0, 50).reversed()
            for (essenceMessage in essenceList) {
                val findMessage = messages.find {
                    it.msgRandom == essenceMessage.msgRandom && it.msgSeq == essenceMessage.msgSeq
                            && it.group == essenceMessage.group
                }
                if (findMessage == null) {
                    val text = essenceMessage.text()
                    val picUrlList = essenceMessage.msgContent.filter { it.msgType == 3 }.map { it.imageUrl }
                    val pushBody = PushBody()
                    pushBody.chatId = essenceEntity.chatId
                    pushBody.messageThreadId = essenceEntity.messageThreadId
                    pushBody.message.add(PushBody.Message().also {
                        it.type = PushBody.Type.TEXT
                        it.content = """
                            #qq群精华消息同步
                            群号：$group
                            发送人qq：${essenceMessage.senderUin}
                            发送人昵称：${essenceMessage.senderNick}
                            发送时间：${DateTimeFormatterUtils.format((essenceMessage.senderTime.toString() + "000").toLong(), "yyyy-MM-dd HH:mm:ss")}
                            内容：
                            $text
                        """.trimIndent()
                    })
                    picUrlList.forEach { pic ->
                        pushBody.message.add(PushBody.Message().also {
                            it.type = PushBody.Type.IMAGE
                            it.content = pic
                        })
                    }
                    client.post("http://192.168.1.237:5461/push/chat") {
                        setJsonBody(pushBody)
                    }
                    messages.add(essenceMessage)
                }
            }
            essenceService.save(essenceEntity)
        }
    }


}

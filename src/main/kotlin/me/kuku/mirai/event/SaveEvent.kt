package me.kuku.mirai.event

import me.kuku.mirai.entity.MessageEntity
import me.kuku.mirai.entity.MessageService
import me.kuku.mirai.entity.RecallMessageEntity
import me.kuku.mirai.entity.RecallMessageService
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.ids
import org.springframework.stereotype.Component

@Component
class SaveEvent(
    private val messageService: MessageService,
    private val recallMessageService: RecallMessageService
) {

    suspend fun GroupMessageEvent.save() {
        val value = message.serializeToMiraiCode()
        val messageEntity = MessageEntity()
        val ids = message.ids
        if (ids.isEmpty()) return
        messageEntity.messageId = ids[0]
        messageEntity.group = group.id
        messageEntity.qq = sender.id
        messageEntity.value = value
        messageService.save(messageEntity)
    }

    suspend fun UserMessageEvent.save() {
        val value = message.serializeToMiraiCode()
        val messageEntity = MessageEntity()
        val ids = message.ids
        if (ids.isEmpty()) return
        messageEntity.messageId = ids[0]
        messageEntity.qq = sender.id
        messageEntity.value = value
        messageService.save(messageEntity)
    }

    suspend fun MessageRecallEvent.GroupRecall.save() {
        val ids = messageIds
        if (ids.isEmpty()) return
        for (messageId in messageIds) {
            val messageEntity = messageService.findByGroupAndMessageId(group.id, messageId) ?: continue
            val recallMessageEntity = RecallMessageEntity()
            recallMessageEntity.message = messageEntity
            recallMessageService.save(recallMessageEntity)
        }
    }

    suspend fun MessageRecallEvent.FriendRecall.save() {
        val ids = messageIds
        if (ids.isEmpty()) return
        for (messageId in messageIds) {
            val messageEntity = messageService.findByGroupAndMessageId(0, messageId) ?: continue
            val recallMessageEntity = RecallMessageEntity()
            recallMessageEntity.message = messageEntity
            recallMessageService.save(recallMessageEntity)
        }
    }

//    suspend fun GroupMessagePostSendEvent.save() {
//        val messageEntity = MessageEntity()
//        val ids = message.ids
//        if (ids.isEmpty()) return
//        val value = message.serializeToMiraiCode()
//        messageEntity.messageId = ids[0]
//        messageEntity.group = target.id
//        messageEntity.qq = bot.id
//        messageEntity.value = value
//        messageService.save(messageEntity)
//    }

//    suspend fun UserMessagePostSendEvent<*>.save() {
//        val messageEntity = MessageEntity()
//        val ids = message.ids
//        if (ids.isEmpty()) return
//        val value = message.serializeToMiraiCode()
//        messageEntity.messageId = ids[0]
//        messageEntity.group = 0
//        messageEntity.qq = bot.id
//        messageEntity.value = value
//        messageService.save(messageEntity)
//    }




}

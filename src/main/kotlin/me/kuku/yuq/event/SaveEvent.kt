package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.artqq.message.ArtGroupMessageSource
import com.icecreamqaq.yuq.entity.Friend
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.event.*
import com.icecreamqaq.yuq.message.FlashImage
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@EventListener
@Service
class Save (
    private val groupService: GroupService,
    private val qqService: QqService,
    private val messageService: MessageService,
    private val recallService: RecallService,
    private val privateMessageService: PrivateMessageService
) {

    @Event(weight = Event.Weight.highest)
    @Transactional
    fun savePeople(e: MessageEvent) {
        val qq = e.sender.id
        var isSave = false
        var qqEntity = qqService.findByQq(qq) ?: QqEntity().also {
            it.qq = qq
            isSave = true
        }
        if (e is GroupMessageEvent) {
            val group = e.group.id
            if (!qqEntity.groups.any { it.group == group }) {
                var groupEntity = groupService.findByGroup(group) ?: GroupEntity().also {
                    it.group = group
                    isSave = true
                }
                qqEntity = qqService.save(qqEntity)
                groupEntity = groupService.save(groupEntity)
                qqEntity.groups.add(groupEntity)
                groupEntity.qqs.add(qqEntity)
                isSave = true
            }
        }
        if (isSave) {
            qqService.save(qqEntity)
        }
    }

    @Event(weight = Event.Weight.high)
    fun saveMessage(e: GroupMessageEvent) {
        val groupEntity = groupService.findByGroup(e.group.id) ?: return
        val qqEntity = qqService.findByQq(e.sender.id) ?: return
        val ss = e.message.toCodeString()
        val messageEntity = MessageEntity()
        messageEntity.messageId = e.message.id ?: 0
        messageEntity.qqEntity = qqEntity
        messageEntity.groupEntity = groupEntity
        messageEntity.content = ss
        e.message.source
        val source = e.message.source as? ArtGroupMessageSource
        source?.let {
            messageEntity.messageSource = MessageSource(it.id, it.groupCode, it.rand)
        }
        messageService.save(messageEntity)
    }

    @Event(weight = Event.Weight.high)
    fun savePrivateMessage(e: PrivateMessageEvent) {
        val qq = e.sender.id
        val qqEntity = qqService.findByQq(qq) ?: return
        val message = e.message
        val messageId = message.source?.id ?: 0
        val ss = message.toCodeString()
        val messageEntity = PrivateMessageEntity()
        messageEntity.messageId = messageId
        messageEntity.qqEntity = qqEntity
        messageEntity.content = ss
        privateMessageService.save(messageEntity)
    }

    @Event(weight = Event.Weight.high)
    @Transactional
    fun saveBotMessage(e: SendMessageEvent.Post) {
        val messageId = e.messageSource.id
        val contact = e.sendTo
        if (contact is Group) {
            val groupEntity = groupService.findByGroup(contact.id) ?: return
            val botQq = yuq.botId
            var qqEntity = groupEntity.get(botQq)
            if (qqEntity == null) {
                qqEntity = qqService.findByQq(botQq)
                if (qqEntity == null) {
                    qqEntity = QqEntity().also { it.qq = botQq }
                }
                groupEntity.qqs.add(qqEntity)
                qqService.save(qqEntity)
                groupService.save(groupEntity)
            }
            val messageEntity = MessageEntity()
            messageEntity.messageId = messageId
            messageEntity.qqEntity = qqEntity
            messageEntity.groupEntity = groupEntity
            messageEntity.content = e.message.toCodeString()
            val source = e.messageSource as? ArtGroupMessageSource
            source?.let {
                messageEntity.messageSource = MessageSource(it.id, it.groupCode, it.rand)
            }
            messageService.save(messageEntity)
        }
    }

    @Event(weight = Event.Weight.high)
    fun savePrivateMessage(e: SendMessageEvent.Post) {
        val messageId = e.messageSource.id
        val contact = e.sendTo
        if (contact is Friend) {
            val qqEntity = qqService.findByQq(contact.id) ?: return
            val messageEntity = PrivateMessageEntity()
            messageEntity.messageId = messageId
            messageEntity.qqEntity = qqEntity
            messageEntity.content = e.message.toCodeString()
            messageEntity.type = PrivateMessageType.SEND
            privateMessageService.save(messageEntity)
        }
    }

    @Event
    fun saveRecallMessage(e: GroupRecallEvent) {
        val messageId = e.messageId
        val group = e.group
        val groupEntity = groupService.findByGroup(group.id) ?: return
        val messageEntity = messageService.findByMessageIdAndGroupEntity(messageId, groupEntity) ?: return
        val recallEntity = RecallEntity()
        recallEntity.messageEntity = messageEntity
        recallService.save(recallEntity)
        if (groupEntity.config.recallNotify == Status.ON) {
            group.sendMessage(mif.at(e.sender.id).plus("妄图撤回一条消息，消息内容为："))
            group.sendMessage(messageEntity.content.toMessageByRainCode())
        }
    }

    @Event
    fun flashImage(e: GroupMessageEvent) {
        val group = e.group
        val groupEntity = groupService.findByGroup(group.id) ?: return
        if (groupEntity.config.flashImageNotify == Status.ON) {
            val message = e.message
            val body = message.body
            for (messageItem in body) {
                if (messageItem is FlashImage) {
                    val image = messageItem.image
                    group.sendMessage(mif.at(e.sender.id).plus("妄图发送闪照：").plus(image))
                }
            }
        }
    }

}
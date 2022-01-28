package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.BotLeaveGroupEvent
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.event.GroupRecallEvent
import com.icecreamqaq.yuq.event.MessageEvent
import com.icecreamqaq.yuq.message.FlashImage
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.*
import me.kuku.yuq.transaction
import javax.inject.Inject

@EventListener
class Save @Inject constructor(
    private val groupService: GroupService,
    private val qqService: QqService,
    private val messageService: MessageService,
    private val recallService: RecallService
) {

    @Event(weight = Event.Weight.high)
    fun savePeople(e: MessageEvent) {
        transaction {
            val qq = e.sender.id
            var isSave = false
            val qqEntity = qqService.findByQq(qq) ?: QqEntity().also {
                it.qq = qq
                isSave = true
            }
            if (e is GroupMessageEvent) {
                val group = e.group.id
                if (!qqEntity.groups.any { it.group == group }) {
                    val groupEntity = groupService.findByGroup(group) ?: GroupEntity().also {
                        it.group = group
                        isSave = true
                    }
                    qqEntity.groups.add(groupEntity)
                    isSave = true
                }
            }
            if (isSave) qqService.save(qqEntity)
        }
    }

    @Event
    fun saveMessage(e: GroupMessageEvent) {
        val groupEntity = groupService.findByGroup(e.group.id) ?: return
        val qqEntity = qqService.findByQq(e.sender.id) ?: return
        val ss = e.message.toCodeString()
        val messageEntity = MessageEntity()
        messageEntity.messageId = e.message.id ?: 0
        messageEntity.qqEntity = qqEntity
        messageEntity.groupEntity = groupEntity
        messageEntity.content = ss
        messageService.save(messageEntity)
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

    @Event
    fun leave(e: BotLeaveGroupEvent) {
        val id = e.group.id
        groupService.deleteByGroup(id)
    }

}
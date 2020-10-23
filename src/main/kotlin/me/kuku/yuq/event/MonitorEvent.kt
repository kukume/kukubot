@file:Suppress("unused")

package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.event.GroupRecallEvent
import com.icecreamqaq.yuq.event.SendMessageEvent
import com.icecreamqaq.yuq.message.*
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.MessageEntity
import me.kuku.yuq.entity.RecallEntity
import me.kuku.yuq.logic.QQAILogic
import me.kuku.yuq.service.MessageService
import me.kuku.yuq.service.GroupService
import me.kuku.yuq.service.RecallService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.removeSuffixLine
import javax.inject.Inject

@EventListener
class MonitorEvent {

    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var messageService: MessageService
    @Inject
    private lateinit var recallService: RecallService
    @Inject
    private lateinit var qqaiLogic: QQAILogic

    @Event
    fun saveMessageGroup(e: GroupMessageEvent){
        messageService.save(MessageEntity(null, e.message.source.id, e.group.id, e.sender.id, BotUtils.messageToJsonArray(e.message).toString()))
    }

    @Event
    fun saveMessageMy(e: SendMessageEvent.Post){
        messageService.save(
                MessageEntity(null, e.messageSource.id, e.sendTo.id, yuq.botId, BotUtils.messageToJsonArray(e.message).toString())
        )
    }

    @Event
    fun readMessage(e: GroupMessageEvent) {
        val message = e.message
        val reply = message.reply
        val list = message.toPath()
        val lastPath = list[list.size - 1]
        if (reply != null && lastPath.endsWith("读消息")) {
            val messageEntity = messageService.findByMessageId(reply.id)
            val msg = if (messageEntity == null) {
                "找不到您当前回复的消息！！"
            } else {
                val jsonArray = messageEntity.contentJsonArray
                val sb = StringBuilder()
                for (i in jsonArray.indices) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    sb.appendLine("类型：${jsonObject.getString("type")}")
                    sb.appendLine("内容：${jsonObject.getString("content")}")
                    sb.appendLine("=================")
                }
                sb.removeSuffixLine().toString()
            }
            e.group.sendMessage(msg.toMessage())
        }
        val at = message.body[0]
        if (at is At && at.user == yuq.botId){
            val sb = StringBuilder()
            message.body.forEach {
                if (it is Text){
                    val text = it.text.trim()
                    if (text == "读消息") return
                    sb.append(text)
                }
            }
            val textChat = qqaiLogic.textChat(sb.toString(), e.sender.id.toString())
            e.group.sendMessage(textChat.toMessage())
        }
    }

    @Event
    fun recallEvent(e: GroupRecallEvent){
        val qq = e.sender.id
        val messageEntity = messageService.findByMessageId(e.messageId) ?: return
        val recallEntity = RecallEntity(null, qq, e.group.id, messageEntity)
        recallService.save(recallEntity)
        val groupEntity = groupService.findByGroup(e.group.id) ?: return
        if (groupEntity.recall) {
            if (e.sender != e.operator) return
            e.group.sendMessage(mif.text("群成员：").plus(mif.at(qq)).plus("\n妄图撤回一条消息。\n消息内容为：\n")
                    .plus(BotUtils.jsonArrayToMessage(messageEntity.contentJsonArray)))
        }
    }

    @Event
    fun flashNotify(e: GroupMessageEvent){
        val group = e.group.id
        val qqGroupEntity = groupService.findByGroup(group) ?: return
        if (qqGroupEntity.flashNotify) {
            val body = e.message.body
            val qq = e.sender.id
            for (item in body) {
                if (item is FlashImage) {
                    val line = System.getProperty("line.separator")
                    val msg = mif.text("群成员：").plus(mif.at(qq))
                            .plus("${line}妄图发送闪照：$line")
                            .plus(mif.imageByUrl(item.url))
                    e.group.sendMessage(msg)
                }
            }
        }
    }

}

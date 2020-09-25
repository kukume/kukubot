package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.event.GroupRecallEvent
import com.icecreamqaq.yuq.event.SendMessageEvent
import com.icecreamqaq.yuq.message.*
import com.icecreamqaq.yuq.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.MessageEntity
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.entity.RecallEntity
import me.kuku.yuq.service.MessageService
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.service.RecallService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.removeSuffixLine
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@EventListener
class MonitorEvent {

    @Inject
    @field:Named("MessageSaved")
    private lateinit var saves: EhcacheHelp<Message>
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var messageService: MessageService
    @Inject
    private lateinit var recallService: RecallService

    private var lock = true

    @Event
    @Synchronized
    fun saveMessageEvent(e: com.IceCreamQAQ.Yu.event.events.Event){
        if (e is GroupMessageEvent){
            lock = false
            val message = e.message
            val id = message.id
            saves[id.toString()] = message
            messageService.save(MessageEntity(null, id!!, e.group.id, e.sender.id, BotUtils.messageToJsonArray(message).toString(), Date()))
        }else if (e is SendMessageEvent.Post){
            if (lock) return
            val group = e.sendTo.id
            var messageId = messageService.findMaxMessageIdByGroup(group) ?: return
            val message = e.message
            val qq = yuq.botId
            messageService.save(MessageEntity(null, ++messageId, group, qq, BotUtils.messageToJsonArray(message).toString(), Date()))
        }
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
                    sb.appendln("类型：${jsonObject.getString("type")}")
                    sb.appendln("内容：${jsonObject.getString("content")}")
                    sb.appendln("=================")
                }
                sb.removeSuffixLine().toString()
            }
            e.group.sendMessage(msg.toMessage())
        }
    }

    @Event
    fun recallEvent(e: GroupRecallEvent){
        val qq = e.sender.id
        val rm = saves[e.messageId.toString()] ?: return
        val messageJsonArray = BotUtils.messageToJsonArray(rm)
        val recallEntity = RecallEntity(null, qq, e.group.id, messageJsonArray.toString())
        recallService.save(recallEntity)
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: QQGroupEntity(null, e.group.id)
        if (qqGroupEntity.recall == true) {
            if (e.sender != e.operator) return
            e.group.sendMessage(mif.text("群成员：").plus(mif.at(qq)).plus("\n妄图撤回一条消息。\n消息内容为：\n").plus(rm))
        }
    }

    @Event
    fun flashNotify(e: GroupMessageEvent){
        val group = e.group.id
        val qqGroupEntity = qqGroupService.findByGroup(group) ?: return
        if (qqGroupEntity.flashNotify == true) {
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

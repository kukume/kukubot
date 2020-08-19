package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.event.GroupRecallEvent
import com.icecreamqaq.yuq.message.*
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import java.util.*
import javax.inject.Inject
import javax.inject.Named

@EventListener
class RecallMonitor {

    @Inject
    @field:Named("MessageSaved")
    private lateinit var saves: EhcacheHelp<Message>
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var mif: MessageItemFactory

    @Event
    fun messageEvent(e: GroupMessageEvent){
        saves[e.message.id.toString()] = e.message
    }

    @Event
    fun recallEvent(e: GroupRecallEvent){
        val qq = e.sender.id
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: QQGroupEntity(null, e.group.id)
        val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
        if (qq.toString() in whiteJsonArray) return
        val rm = saves[e.messageId.toString()] ?: return
        val recallMessageJsonArray = qqGroupEntity.getRecallMessageJsonArray()
        val messageJsonArray = BotUtils.messageToJsonArray(rm)
        val jsonObject = JSONObject()
        jsonObject["qq"] = qq
        jsonObject["time"] = Date().time
        jsonObject["message"] = messageJsonArray
        recallMessageJsonArray.add(jsonObject)
        qqGroupEntity.recallMessage = recallMessageJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        if (qqGroupEntity.recall == true) {
            if (e.sender != e.operator) return
            e.group.sendMessage(mif.text("群成员：").plus(mif.at(qq)).plus("\n妄图撤回一条消息。\n消息内容为：\n").plus(rm))
        }
    }

}

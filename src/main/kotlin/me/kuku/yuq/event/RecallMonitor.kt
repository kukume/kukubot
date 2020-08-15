package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.event.GroupRecallEvent
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.service.QQGroupService
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
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id)
        if (qqGroupEntity?.recall == true) {
            val qq = e.sender.id
            val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
            if (e.sender != e.operator) return
            val rm = saves[e.messageId.toString()] ?: return
            if (qq.toString() in whiteJsonArray) return
            e.group.sendMessage(mif.text("群成员：").plus(mif.at(qq)).plus("\n妄图撤回一条消息。\n消息内容为：\n").plus(rm))
        }
    }

}

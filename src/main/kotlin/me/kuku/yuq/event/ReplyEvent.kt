package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.event.SendMessageEvent
import com.icecreamqaq.yuq.message.At
import com.icecreamqaq.yuq.message.ChainItem
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.message.Text
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.QaType
import org.springframework.stereotype.Service

@EventListener
@Service
class ReplyEvent (
    private val groupService: GroupService
) {

    @Event
    fun qa(e: GroupMessageEvent) {
        val group = e.group
        val groupEntity = groupService.findByGroup(group.id) ?: return
        val message = e.message
        val codeStr = message.toCodeString()
        val qaList = groupEntity.config.qaList
        for (qa in qaList) {
            if (qa.q == codeStr && qa.type == QaType.EXACT) {
                group.sendMessage(qa.a.toMessageByRainCode())
            }
            if (codeStr.contains(qa.q) && qa.type == QaType.FUZZY) {
                group.sendMessage(qa.a.toMessageByRainCode())
            }
        }
    }

    @Event
    fun updateMessage(e: SendMessageEvent.Per) {
        val message = e.message
        val body = message.body
        var first: ChainItem? = body::class.java.getDeclaredField("first").also { it.isAccessible = true }.get(body) as ChainItem
        while (first != null) {
            val next = first.next
            val item = first.item
            if (item is At) {
                val b = first.next == null || kotlin.run {
                    if (first!!.next?.item is Text) {
                        val text = (first!!.next!!.item as Text).text
                        !text.startsWith("\n")
                    } else true
                }
                if (b) {
                    val chainItem = ChainItem(mif.text("\n"), null, first)
                    chainItem.next = next
                    first.next = chainItem
                }
            }
            first = next
        }
    }

}
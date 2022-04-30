package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
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

}
package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.MessageService
import me.kuku.yuq.entity.QaType
import javax.inject.Inject

@EventListener
class ReplyEvent @Inject constructor(
    private val groupService: GroupService,
    private val messageService: MessageService
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
    fun recallMessage(e: GroupMessageEvent) {
        val group = e.group
        val qq = e.sender
        val message = e.message
        if (message.toPath().last() != "撤回") return
        val messageSource = message.reply ?: return
        val botId = yuq.botId
        val bot = group[botId]
        if ((!bot.isAdmin() && messageSource.sender != botId) || (bot.isAdmin() && qq.isOwner()))
            throw mif.at(qq.id).plus("撤回失败，机器人权限不足").toThrowable()
        val id = messageSource.id
        val messageEntity = messageService.findByMessageIdAndGroup(id, group.id)
            ?: throw mif.at(qq.id).plus("没有找到该条消息，撤回失败").toThrowable()
        val source = messageEntity.messageSource ?: throw mif.at(qq.id).plus("没有找到该条消息源，撤回失败").toThrowable()
        source.toArtGroupMessageSource().recall()
        message.recall()
    }


}
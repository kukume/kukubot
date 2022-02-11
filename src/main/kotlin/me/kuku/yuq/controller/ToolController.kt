package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.mif
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.MessageService
import me.kuku.yuq.entity.QaType
import me.kuku.yuq.logic.ToolLogic
import javax.inject.Inject

@GroupController
class ToolController @Inject constructor(
    private val messageService: MessageService
) {

    @Action("摸鱼日历")
    fun fishermanCalendar(group: Group) {
        val bytes = OkHttpUtils.getBytes("https://api.kukuqaq.com/tool/fishermanCalendar?preview")
        group.sendMessage(mif.imageByByteArray(bytes))
    }

    @Action("摸鱼日历搜狗")
    fun fishermanCalendarSoGou(group: Group) {
        val bytes = OkHttpUtils.getBytes("https://api.kukuqaq.com/tool/fishermanCalendar/sogou?preview")
        group.sendMessage(mif.imageByByteArray(bytes))
    }

    @Action("色图")
    fun color() =
        mif.imageByUrl(OkHttpUtils.get("https://api.kukuqaq.com/lolicon/random?preview").also { it.close() }.header("location")!!)

    @Action(value = "读消息", suffix = true)
    @QMsg(reply = true)
    fun readMessage(message: Message, group: Long): String? {
        val messageSource = message.reply ?: return null
        val id = messageSource.id
        val messageEntity = messageService.findByMessageIdAndGroup(id, group) ?: return "没有找到该消息"
        return messageEntity.content
    }

    @Action("\\.*\\")
    fun qa(group: Group, groupEntity: GroupEntity, message: Message) {
        message.recall()
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

    @Action("百科 {text}")
    @QMsg(reply = true)
    fun baiKe(text: String) = ToolLogic.baiKe(text)

}
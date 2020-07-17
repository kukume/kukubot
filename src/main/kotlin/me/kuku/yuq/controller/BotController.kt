package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mf
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import javax.inject.Inject

@GroupController
class BotController {
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String
    @Config("YuQ.Mirai.user.pwd")
    private lateinit var password: String
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var toolLogic: ToolLogic

    var qqEntity: QQEntity? = null

    @Before
    fun before(group: Long, qq: Long){
        if (qqEntity == null || !qqEntity!!.status) {
            yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为机器人登录账号中！！！请稍后！！"))
            val commonResult = QQPasswordLoginUtils.login(qq = this.qq, password = this.password)
            if (commonResult.code != 200) throw mif.text(commonResult.msg).toMessage()
            yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("机器人账号登录成功，将为您完成接下来的操作！"))
            qqEntity = QQUtils.convertQQEntity(commonResult.t)
            qqEntity!!.qq = this.qq.toLong()
            qqEntity!!.password = this.password
        }
    }

    @Action("赞我")
    fun like(qq : Long) = qqLogic.like(qqEntity!!, qq)

    @Action("公告")
    fun publishNotice(group: Long, qq: Long, session: ContextSession): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入需要发送的公告内容！"))
        val noticeMessage = session.waitNextMessage(30 * 1000)
        val notice = noticeMessage.body[0].toPath()
        return qqLogic.publishNotice(qqEntity!!, group, notice)
    }

    @Action("群链接")
    fun groupLink(group: Long) = qqLogic.getGroupLink(qqEntity!!, group);

    @Action("群活跃")
    fun groupActive(group: Long) = qqLogic.groupActive(qqEntity!!, group, 0)

    @Action("群文件")
    fun groupFile(@PathVar(1) fileName: String?, group: Long) =  qqLogic.groupFileUrl(qqEntity!!, group, fileName)

    @Action("全体禁言 {status}")
    fun allShutUp(group: Long, status: Boolean) = qqLogic.allShutUp(qqEntity!!, group, status)

    @Action("改 {member} {name}")
    fun changeName(member: Member, group: Long, name: String) = qqLogic.changeName(qqEntity!!, member.id, group, name)

    @Action("天气/{local}")
    fun weather(local: String): Message {
        val commonResult = toolLogic.weather(local, qqEntity!!.getCookie())
        return if (commonResult.code == 200) mif.xmlEx(146, commonResult.t).toMessage()
        else mif.text(commonResult.msg).toMessage()
    }

    @After
    fun finally(actionContext: BotActionContext){
        val msg = actionContext.reMessage?.body?.get(0).toString()
        if ("更新QQ" in msg){
            qqEntity?.status = false
            actionContext.reMessage = actionContext.reMessage!! + actionContext.reMessage!!.newMessage().plus("\n抱歉，机器人cookie已失效，请重新发送一遍指令！！")
        }
        actionContext.reMessage?.at = true
    }
}
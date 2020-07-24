package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.firstString
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mf
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.logic.QQGroupLogic
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.logic.QQZoneLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.utils.removeSuffixLine
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@GroupController
class BotController {
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var web: OkHttpWebImpl
    @Inject
    private lateinit var qqGroupLogic: QQGroupLogic
    @Inject
    private lateinit var qqZoneLogic: QQZoneLogic

    @Before
    fun before(qq: Long, message: Message, actionContext: BotActionContext) {
        val msgList = arrayOf("改", "公告", "全体禁言", "拉")
        if (msgList.contains(message.toPath()[0])) {
            if (qq.toString() != master) {
                throw mif.at(qq).plus("抱歉，您不是机器人主人，无法执行！！")
            }
        }
        val concurrentHashMap = web.domainMap
        val qunList = concurrentHashMap.getValue("qun.qq.com")
        val groupPsKey = qunList[0].value
        val qqList = concurrentHashMap.getValue("qq.com")
        val sKey = qqList[1].value
        val qZoneList = concurrentHashMap.getValue("qzone.qq.com")
        val psKey = qZoneList[0].value
        val vipList = concurrentHashMap.getValue("vip.qq.com")
        val vipPsKey = vipList[0].value
        val qqEntity = QQEntity(null, this.qq.toLong(), 0L, "", sKey, psKey, groupPsKey)
        actionContext["qqEntity"] = qqEntity
        actionContext["vipPsKey"] = vipPsKey
    }

    @Action("签个到 {img}")
    fun groupSign(qqEntity: QQEntity, img: Image, group: Long){
        qqLogic.groupSign(qqEntity, group, "你猜", "mirai在线签到！！！", "{\"category_id\":\"\",\"page\":\"\",\"pic_id\":\"\"}", img.url)
    }

    @Action("拉 {qqStr}")
    fun addGroupMember(qqStr: String, group: Long) =
            qqGroupLogic.addGroupMember(qqStr.toLong(), group)


    @Action("列出{day}天未发言")
    fun notSpeak(group: Long, day: String, session: ContextSession, qq: Long, qqEntity: QQEntity): String? {
        val commonResult = qqLogic.groupMemberInfo(qqEntity, group)
        if (commonResult.code == 200) {
            val list = commonResult.t!!
            val qqList = mutableListOf<Long>()
            val sb = StringBuilder("本群${day}天未发言的成员如下：\n")
            list.forEach {
                if ((Date().time - it.lastTime) / (1000 * 60 * 60 * 24) > day.toInt()) {
                    sb.appendln(it.qq)
                    qqList.add(it.qq)
                }
            }
            yuq.sendMessage(mf.newGroup(group).plus(sb.removeSuffixLine().toString()))
            val nextMessage = session.waitNextMessage(30 * 1000)
            return if (nextMessage.firstString() == "一键踢出" && qq.toString() == master) {
                qqList.forEach { yuq.groups[group]?.get(it)?.kick() }
                "踢出成功！！"
            } else null
        } else return commonResult.msg
    }

    @QMsg(at = true)
    @Action("赞我")
    fun like(qq: Long, qqEntity: QQEntity, vipPsKey: String) = qqLogic.like(qqEntity, qq, vipPsKey)

    @QMsg(at = true)
    @Action("公告")
    fun publishNotice(group: Long, qq: Long, session: ContextSession, qqEntity: QQEntity): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入需要发送的公告内容！"))
        val noticeMessage = session.waitNextMessage(30 * 1000)
        val notice = noticeMessage.body[0].toPath()
        return qqLogic.publishNotice(qqEntity, group, notice)
    }

    @Action("群链接")
    fun groupLink(group: Long, qqEntity: QQEntity) = qqLogic.getGroupLink(qqEntity, group);

    @Action("群活跃")
    fun groupActive(group: Long, qqEntity: QQEntity) = qqLogic.groupActive(qqEntity, group, 0)

    @Action("群文件")
    fun groupFile(@PathVar(1) fileName: String?, group: Long, qqEntity: QQEntity) = qqLogic.groupFileUrl(qqEntity, group, fileName)

    @Action("全体禁言 {status}")
    fun allShutUp(group: Long, status: Boolean, qqEntity: QQEntity) = qqLogic.allShutUp(qqEntity, group, status)

    @QMsg(at = true)
    @Action("改 {member} {name}")
    fun changeName(member: Member, group: Long, name: String, qqEntity: QQEntity) = qqLogic.changeName(qqEntity, member.id, group, name)

    @Action("天气/{local}")
    fun weather(local: String, qqEntity: QQEntity): Message {
        val commonResult = toolLogic.weather(local, qqEntity.getCookie())
        return if (commonResult.code == 200) mif.xmlEx(146, commonResult.t).toMessage()
        else mif.text(commonResult.msg).toMessage()
    }

    @Action("龙王")
    fun dragonKing(group: Long): Message{
        val commonResult = qqGroupLogic.groupDragonKing(group)
        return if (commonResult.code == 200){
            val urlArr = arrayOf(
                    "https://u.iheit.com/kuku/61f600415023300.jpg",
                    "https://u.iheit.com/kuku/449ab0415103619.jpg",
                    "https://u.iheit.com/kuku/51fe90415023311.jpg",
                    "https://u.iheit.com/kuku/1d12a0415023726.jpg",
                    "https://u.iheit.com/kuku/b04b30415023728.jpg",
                    "https://u.iheit.com/kuku/d21200415023730.jpg",
                    "https://u.iheit.com/kuku/55f0e0415023731.jpg",
                    "https://u.iheit.com/kuku/634cc0415023733.jpg",
                    "https://u.iheit.com/kuku/c044b0415023734.jpg",
                    "https://u.iheit.com/kuku/ce2270415023735.jpg",
                    "https://u.iheit.com/kuku/6e4b20415023737.jpg",
                    "https://u.iheit.com/kuku/5f7d70415023738.jpg",
                    "https://u.iheit.com/kuku/98d640415023739.jpg",
                    "https://u.iheit.com/kuku/26a1a0415023741.jpg",
                    "https://u.iheit.com/kuku/ddc810415023745.jpg",
                    "https://u.iheit.com/kuku/23ee20415023747.jpg",
                    "https://u.iheit.com/kuku/8c4a80415023748.jpg",
                    "https://u.iheit.com/kuku/bdb970415023750.jpg",
                    "https://u.iheit.com/images/2020/07/23/33609b326b5b30b0.jpg",
                    "https://u.iheit.com/images/2020/07/23/3e53644bd75c68e6.jpg"
            )
            val url = urlArr[Random.nextInt(urlArr.size)]
            val map = commonResult.t
            mif.at(map.getValue("qq")).plus(mif.image(url)).plus("龙王（已蝉联${map.getValue("day")}天）快喷水！")
        }else mif.text(commonResult.msg).toMessage()
    }

    @Action("加个好友 {qqStr}")
    fun addFriend(qqEntity: QQEntity, qqStr: String) =
        qqZoneLogic.addFriend(qqEntity, qqStr.toLong(), "机器人加你需要理由？？", null, null)

}
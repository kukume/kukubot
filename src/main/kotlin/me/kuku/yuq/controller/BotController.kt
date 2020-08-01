package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
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
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.removeSuffixLine
import java.text.SimpleDateFormat
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
    @Inject
    private lateinit var qqGroupService: QQGroupService

    @Before
    fun before(qq: Long, message: Message, actionContext: BotActionContext) {
        val msgList = arrayOf("改", "公告", "全体禁言", "拉", "发布作业", "群接龙", "群作业")
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

    @QMsg(at = true)
    @Action("拉 {qqNo}")
    fun addGroupMember(qqNo: Long, group: Long) =
            qqGroupLogic.addGroupMember(qqNo, group)

    @QMsg(at = true)
    @Action("发布作业")
    @Synonym(["群作业"])
    fun addHomeWork(group: Long, qq: Long, session: ContextSession): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入作业科目！！"))
        val nameMessage = session.waitNextMessage(30 * 1000)
        val name = nameMessage.firstString()
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入作业内容！！"))
        val contentMessage = session.waitNextMessage(30 * 1000)
        val content = contentMessage.firstString()
        return qqGroupLogic.addHomeWork(group, name, "作业", content, false)
    }

    @Action("查业务 {qqNo}")
    fun queryVip(qqNo: Long, qqEntity: QQEntity, vipPsKey: String) = qqLogic.queryFriendVip(qqEntity, qqNo, vipPsKey)

    @QMsg(at = true)
    @Action("群接龙")
    fun groupChain(group: Long, qq: Long, session: ContextSession): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入接龙内容"))
        val contentMessage = session.waitNextMessage(30 * 1000)
        val content = contentMessage.firstString()
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入到期日期（单位为天）！！"))
        val timeMessage = session.waitNextMessage(30 * 1000)
        val time = try {
            timeMessage.firstString().toInt()
        }catch (e: Exception){
            return "时间不为整型"
        }
        val newTime = (Date().time + (time * 1000 * 60 * 60 * 24)).toString().substring(0, 10)
        return qqGroupLogic.groupCharin(group, content, newTime.toLong())
    }

    @Action("群等级")
    fun groupLevel(group: Long): String{
        val commonResult = qqGroupLogic.groupLevel(group)
        val list = commonResult.t ?: return commonResult.msg
        val sb = StringBuilder()
        list.forEach {
            sb.appendln("@${it["name"]}：level-${it["level"]}；${it["tag"]}")
        }
        return sb.removeSuffixLine().toString()
    }

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
                val whiteList = qqGroupService.findByGroup(group)?.whiteList ?: "查询群失败，踢出失败！！"
                qqList.forEach {
                    if (it.toString() in whiteList) return@forEach
                    yuq.groups[group]?.get(it)?.kick()
                }
                "踢出成功！！"
            } else null
        } else return commonResult.msg
    }

    @Action("列出从未发言")
    fun neverSpeak(group: Long, session: ContextSession, qqEntity: QQEntity, qq: Long): String?{
        val commonResult = qqLogic.groupMemberInfo(qqEntity, group)
        val list = commonResult.t ?: return commonResult.msg
        val qqList = mutableListOf<Long>()
        val sb = StringBuilder("本群从未发言的成员如下：\n")
        list.forEach {
            if ((it.lastTime == it.joinTime || it.integral <= 1) && (Date().time - it.joinTime > 1000 * 60 * 60 * 24)) {
                sb.appendln(it.qq)
                qqList.add(it.qq)
            }
        }
        yuq.sendMessage(mf.newGroup(group).plus(sb.removeSuffixLine().toString()))
        val nextMessage = session.waitNextMessage(40 * 1000)
        return if (nextMessage.firstString() == "一键踢出" && qq.toString() == master) {
            val whiteList = qqGroupService.findByGroup(group)?.whiteList ?: "查询群失败，踢出失败！！"
            qqList.forEach {
                if (it.toString() in whiteList) return@forEach
                yuq.groups[group]?.get(it)?.kick()
            }
            qqList.forEach { yuq.groups[group]?.get(it)?.kick() }
            "踢出成功！！"
        } else null
    }

    @QMsg(at = true)
    @Action("公告")
    fun publishNotice(group: Long, qq: Long, session: ContextSession, qqEntity: QQEntity): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入需要发送的公告内容！"))
        val noticeMessage = session.waitNextMessage(30 * 1000)
        val notice = noticeMessage.body[0].toPath()
        return qqLogic.publishNotice(qqEntity, group, notice)
    }

    @Action("查询 {qqNo}")
    fun query(group: Long, qqNo: Long): String{
        val commonResult = qqGroupLogic.queryMemberInfo(group, qqNo)
        val groupMember = commonResult.t ?: return commonResult.msg
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val sb = StringBuilder()
        sb.appendln("qq：${groupMember.qq}")
        sb.appendln("昵称：${groupMember.groupCard}")
        sb.appendln("Q龄：${groupMember.age}")
        sb.appendln("入群时间：${sdf.format(Date(groupMember.joinTime))}")
        sb.append("最后发言时间：${sdf.format(Date(groupMember.lastTime))}")
        return sb.toString()
    }

    @QMsg(at = true)
    @Action("群链接")
    fun groupLink(group: Long, qqEntity: QQEntity) = qqLogic.getGroupLink(qqEntity, group);

    @Action("群活跃")
    fun groupActive(group: Long, qqEntity: QQEntity) = qqLogic.groupActive(qqEntity, group, 0)

    @Action("群文件")
    fun groupFile(@PathVar(1) fileName: String?, group: Long, qqEntity: QQEntity) = qqLogic.groupFileUrl(qqEntity, group, fileName)

    @QMsg(at = true)
    @Action("删群文件 {fileName}")
    fun delGroupFile(@PathVar(2) folderName: String?, fileName: String, group: Long, qqEntity: QQEntity) =
            qqLogic.removeGroupFile(qqEntity, group, fileName, folderName)

    @QMsg(at = true)
    @Action("全体禁言 {status}")
    fun allShutUp(group: Long, status: Boolean, qqEntity: QQEntity) = qqLogic.allShutUp(qqEntity, group, status)

    @QMsg(at = true)
    @Action("改 {qqNo} {name}")
    fun changeName(qqNo: Long, group: Long, name: String, qqEntity: QQEntity) = qqLogic.changeName(qqEntity, qqNo, group, name)

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

    @Action("加个好友 {qqNo}")
    fun addFriend(qqEntity: QQEntity, qqNo: Long) =
        qqZoneLogic.addFriend(qqEntity, qqNo, "机器人加你需要理由？？", null, null)

}
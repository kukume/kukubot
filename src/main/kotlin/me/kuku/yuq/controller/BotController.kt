package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.alibaba.fastjson.JSONArray
import com.icecreamqaq.yuq.*
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.MessageItem
import com.icecreamqaq.yuq.message.Text
import com.icecreamqaq.yuq.mirai.MiraiBot
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.logic.QQGroupLogic
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.logic.QQZoneLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.removeSuffixLine
import net.mamoe.mirai.contact.PermissionDeniedException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@GroupController
class BotController: QQController() {
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var miraiBot: MiraiBot
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
    fun before(qq: Long, message: Message, actionContext: BotActionContext, group: Long) {
        val msgList = arrayOf("改", "公告", "全体禁言", "拉", "发布作业", "群接龙", "群作业")
        val msg = message.toPath()[0]
        if (msgList.contains(msg)) {
            val qqGroupEntity = qqGroupService.findByGroup(group) ?: QQGroupEntity()
            val adminJsonArray = qqGroupEntity.getAdminJsonArray()
            val adminWhiteList = qqGroupEntity.getAllowedCommandsJsonArray()
            if (!adminJsonArray.contains(qq.toString()) || !adminWhiteList.contains(msg)) {
                if (qq.toString() != master) {
                    throw "抱歉，您的权限不足，无法执行！！".toMessage()
                }
            }
        }
        val qqEntity = BotUtils.toQQEntity(web, miraiBot)
        actionContext["qqEntity"] = qqEntity
    }

    @Action("签个到 {param}")
    fun groupSign(qqEntity: QQEntity, param: MessageItem, group: Long): Any?{
        val place = "你猜"
        val text = "mirai在线签到！！！"
        val str = when (param) {
            is Text -> {
                val commonResult = qqGroupLogic.groupSign(group, place, text, param.text, null, null)
                commonResult.t ?: return commonResult.msg
            }
            is Image -> {
                val commonResult = qqLogic.groupUploadImage(qqEntity, param.url)
                val map = commonResult.t ?: return commonResult.msg
                val signCommonResult = qqGroupLogic.groupSign(group, place, text, "自定义", map["picId"], map["picUrl"])
                signCommonResult.t ?: return commonResult.msg
            }
            else -> return null
        }
        return mif.jsonEx(str)
    }

    @Action("qq上传")
    fun groupUpload(qqEntity: QQEntity, qq: Long, session: ContextSession): String{
        reply(mif.at(qq).plus("请发送您需要上传的图片！！"))
        val nextMessage = session.waitNextMessage()
        val body = nextMessage.body
        val sb = StringBuilder().appendln("您上传的图片链接如下：")
        val i = 1
        for (item in body){
            if (item is Image){
                val commonResult = qqLogic.groupUploadImage(qqEntity, item.url)
                val url = commonResult.t?.getValue("picUrl") ?: commonResult.msg
                sb.appendln("$i、$url")
            }
        }
        return sb.removeSuffixLine().toString()
    }

    @QMsg(at = true)
    @Action("拉 {qqNo}")
    fun addGroupMember(qqNo: Long, group: Long) =
            qqGroupLogic.addGroupMember(qqNo, group)

    @QMsg(at = true)
    @Action("发布作业")
    @Synonym(["群作业"])
    fun addHomeWork(group: Long, qq: Long, session: ContextSession): String{
        reply(mif.at(qq).plus("请输入作业科目！！"))
        val nameMessage = session.waitNextMessage(30 * 1000)
        val name = nameMessage.firstString()
        reply(mif.at(qq).plus("请输入作业内容！！"))
        val contentMessage = session.waitNextMessage(30 * 1000)
        val content = contentMessage.firstString()
        return qqGroupLogic.addHomeWork(group, name, "作业", content, true)
    }

    @Action("查业务 {qqNo}")
    @QMsg(at = true, atNewLine = true)
    fun queryVip(qqNo: Long, qqEntity: QQEntity) = qqLogic.queryFriendVip(qqEntity, qqNo, null)

    @QMsg(at = true)
    @Action("群接龙")
    fun groupChain(group: Long, qq: Long, session: ContextSession): String{
        reply(mif.at(qq).plus("请输入接龙内容"))
        val contentMessage = session.waitNextMessage(30 * 1000)
        val content = contentMessage.firstString()
        reply(mif.at(qq).plus("请输入到期日期（单位为天）！！"))
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
    @QMsg(at = true, atNewLine = true)
    fun groupLevel(group: Long): String{
        val commonResult = qqGroupLogic.groupLevel(group)
        val list = commonResult.t ?: return commonResult.msg
        val sb = StringBuilder()
        list.forEach {
            sb.appendln("@${it["name"]}：level-${it["level"]}；${it["tag"]}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("列出{level}级以下")
    @QMsg(at = true, atNewLine = true)
    fun level(qqEntity: QQEntity, group: Long, level: String): String?{
        val members = yuq.groups[group]?.members ?: return "获取用户列表失败，请稍后再试！！"
        val levelNum = try {
            level.toInt()
        }catch (e: Exception){
            return "等级只能为整型！！"
        }
        val sb = StringBuilder().appendln("本群QQ等级在${level}以下的成员如下")
        val list = mutableListOf<Member>()
        for ((k, v) in members){
            if (k == this.qq.toLong()) continue
            val userLevel = qqLogic.queryLevel(qqEntity, k, null)
            if (userLevel.contains("更新QQ")) continue
            if (levelNum > userLevel.toInt()) {
                list.add(v)
                sb.appendln(k)
            }
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("列出{day}天未发言")
    @QMsg(at = true, atNewLine = true)
    fun notSpeak(group: Long, day: String, qqEntity: QQEntity): String? {
        val commonResult = qqLogic.groupMemberInfo(qqEntity, group)
        return if (commonResult.code == 200) {
            val list = commonResult.t!!
            val qqList = mutableListOf<Long>()
            val sb = StringBuilder().appendln("本群${day}天未发言的成员如下：")
            list.forEach {
                if ((Date().time - it.lastTime) / (1000 * 60 * 60 * 24) > day.toInt()) {
                    sb.appendln(it.qq)
                    qqList.add(it.qq)
                }
            }
            sb.removeSuffixLine().toString()
        } else return commonResult.msg
    }

    @Action("列出从未发言")
    @QMsg(at = true, atNewLine = true)
    fun neverSpeak(group: Long, qqEntity: QQEntity): String?{
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
        return sb.removeSuffixLine().toString()
    }

    @QMsg(at = true)
    @Action("公告")
    fun publishNotice(group: Long, qq: Long, session: ContextSession, qqEntity: QQEntity): String {
        reply(mif.at(qq).plus("请输入需要发送的公告内容！"))
        val noticeMessage = session.waitNextMessage(30 * 1000)
        val notice = noticeMessage.body[0].toPath()
        return qqLogic.publishNotice(qqEntity, group, notice)
    }

    @Action("查询 {qqNo}")
    @QMsg(at = true, atNewLine = true)
    fun query(group: Long, qqNo: Long, qqEntity: QQEntity): String{
        val commonResult = qqGroupLogic.queryMemberInfo(group, qqNo)
        val groupMember = commonResult.t
        val groupMember2 = qqZoneLogic.visitQZoneMobile(qqEntity, qqNo).t!!
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val sb = StringBuilder()
        sb.appendln("qq：$qqNo")
        sb.appendln("昵称：${groupMember2.nickName}")
        sb.appendln("年龄：${groupMember2.userAge}")
        sb.append("所在地区：${groupMember2.country}-${groupMember2.province}-${groupMember2.city}")
        if (groupMember != null) {
            sb.appendln()
            sb.appendln("群名片：${groupMember.groupCard}")
            sb.appendln("Q龄：${groupMember.age}")
            sb.appendln("入群时间：${sdf.format(Date(groupMember.joinTime))}")
            sb.append("最后发言时间：${sdf.format(Date(groupMember.lastTime))}")
        }
        return sb.toString()
    }

    @QMsg(at = true)
    @Action("群链接")
    fun groupLink(group: Long, qqEntity: QQEntity) = qqLogic.getGroupLink(qqEntity, group)

    @Action("群活跃")
    @QMsg(at = true, atNewLine = true)
    fun groupActive(group: Long, qqEntity: QQEntity) = qqLogic.groupActive(qqEntity, group, 0)

    @Action("群文件")
    @QMsg(at = true, atNewLine = true)
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

    @Action("天气 {local}")
    fun weather(local: String, qqEntity: QQEntity, qq: Long): Message {
        val commonResult = toolLogic.weather(local, qqEntity.getCookie())
        return mif.xmlEx(146, commonResult.t ?: return mif.at(qq).plus(commonResult.msg)).toMessage()
    }

    @Action("龙王")
    fun dragonKing(group: Long, qq: Member, @PathVar(value = 1, type = PathVar.Type.Integer) num: Int?): Message{
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.dragonKing == false) return mif.at(qq.id).plus("迫害龙王已关闭！！")
        val list = qqGroupLogic.groupHonor(group, "talkAtIve")
        if (list.isEmpty()) return mif.at(qq.id).plus("昨天没有龙王！！")
        if (num ?: 1 > list.size){
            return mif.at(qq.id).plus("历史龙王只有${list.size}位哦，超过范围了！！")
        }
        val map = list[(num ?: 1) - 1]
        val resultQQ = map.getValue("qq").toLong()
        if (resultQQ == this.qq.toLong()) return listOf("呼风唤雨", "84消毒", "巨星排面").random().toMessage()
        val jsonArray = qqGroupEntity?.getWhiteJsonArray() ?: JSONArray()
        if (jsonArray.contains(resultQQ.toString())){
            return try {
                qq.ban(60 * 5)
                mif.at(qq.id).plus("迫害白名单用户，您已被禁言！！")
            }catch (e: PermissionDeniedException){
                mif.at(qq.id).plus("禁止迫害白名单用户，禁言迫害者失败，权限不足！！")
            }
        }
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
                "https://u.iheit.com/images/2020/07/23/3e53644bd75c68e6.jpg",
                "https://u.iheit.com/images/2020/08/05/image.png",
                "https://u.iheit.com/images/2020/08/05/image4046ccd0c6179229.png"
        )
        val url = urlArr[Random.nextInt(urlArr.size)]
        return mif.at(resultQQ).plus(mif.imageByUrl(url)).plus("龙王，已蝉联${map.getValue("desc")}，快喷水！！")
    }

    @Action("群聊炽焰")
    @Synonym(["群聊之火", "冒尖小春笋", "快乐源泉"])
    fun legend(group: Long, qq: Long, @PathVar(0) str: String, @PathVar(value = 1, type = PathVar.Type.Integer) num: Int?): Message{
        val msg: String
        val list: List<Map<String, String>>
        when (str) {
            "群聊炽焰" -> {
                list = qqGroupLogic.groupHonor(group, "legend")
                msg = "快续火！！"
            }
            "群聊之火" -> {
                list = qqGroupLogic.groupHonor(group, "actor")
                msg = "快续火！！"
            }
            "冒尖小春笋" -> {
                list = qqGroupLogic.groupHonor(group, "strongNewBie")
                msg = "快......我也不知道快啥了！！"
            }
            "快乐源泉" -> {
                list = qqGroupLogic.groupHonor(group, "emotion")
                msg = "快发表情包！！"
            }
            else -> return mif.at(qq).plus("类型不匹配，查询失败！！")
        }
        if (list.isEmpty()) return mif.at(qq).plus("该群还没有${str}用户！！")
        if (num ?: 1 > list.size){
            return mif.at(qq).plus("${str}只有${list.size}位哦，超过范围了！！")
        }
        val map = list[(num ?: 1) - 1]
        return mif.at(map.getValue("qq").toLong()).plus(mif.imageByUrl(map.getValue("image"))).plus("$str，${map.getValue("desc")}，$msg")
    }

    @Action("群精华")
    @Synonym(["群精华消息"])
    fun essenceMessage(group: Long): String{
        val commonResult = qqGroupLogic.essenceMessage(group)
        val list = commonResult.t ?: return commonResult.msg
        return list[Random.nextInt(list.size)]
    }
}
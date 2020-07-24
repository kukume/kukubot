package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.*
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mirai.MiraiBot
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

@GroupController
class ManagerController {
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var miraiBot: MiraiBot

    @Before
    fun before(group: Long, qq: Long, actionContext: BotActionContext, message: Message){
        var qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity == null){
            qqGroupEntity = QQGroupEntity(null, group)
            qqGroupService.save(qqGroupEntity)
        }
        actionContext.session["qqGroupEntity"] = qqGroupEntity
        val whiteList = arrayOf("问答", "违规词", "黑名单", "白名单", "重启mirai")
        if (!whiteList.contains(message.toPath()[0])) {
            if (qq != master.toLong()) throw "抱歉，您不是机器人主人，无法执行！！".toMessage()
        }
    }

    @Action("机器人 {status}")
    fun switchGroup(qqGroupEntity: QQGroupEntity, status: Boolean): String?{
        qqGroupEntity.status = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "机器人开启成功" else "机器人关闭成功"
    }

    @Action("重启mirai")
    fun robot(){
        miraiBot.stop()
        miraiBot.init()
        miraiBot.start()
    }

    @Action("自动审核 {status}")
    fun autoReview(qqGroupEntity: QQGroupEntity, status: Boolean): String{
        qqGroupEntity.autoReview = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "自动审核开启成功" else "自动审核关闭成功"
    }

    @Action("#qq {status}")
    fun qqStatus(qqGroupEntity: QQGroupEntity, status: Boolean): String{
        qqGroupEntity.qqStatus = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "qq功能开启成功" else "qq功能关闭成功"
    }

    @Action("欢迎语 {status}")
    fun welcome(qqGroupEntity: QQGroupEntity, status: Boolean): String{
        qqGroupEntity.welcomeMsg = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "欢迎语开启成功" else "欢迎语关闭成功"
    }

    @Action("萌宠 {status}")
    fun superCute(qqGroupEntity: QQGroupEntity, status: Boolean): String{
        qqGroupEntity.superCute = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "萌宠功能开启成功" else "萌宠功能关闭成功"
    }

    @Action("通知")
    fun allNotice(group: Long, qq: Long, session: ContextSession): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入您要通知的内容！！"))
        val noticeMessage = session.waitNextMessage(30 * 1000)
        val members = yuq.groups[group]?.members
        thread {
            for (k in members!!) {
                TimeUnit.SECONDS.sleep(2)
                yuq.sendMessage(mf.newTemp(group, k.key).plus(noticeMessage))
            }
        }
        return "通知将在后台运行中，消息包含图片、At等可能会通知不成功！！"
    }

    @Action("退群拉黑 {status}")
    fun leaveGroupBlack(qqGroupEntity: QQGroupEntity, status: Boolean): String{
        qqGroupEntity.leaveGroupBlack = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "退群拉黑已开启！！" else "退群拉黑已关闭！！"
    }

    @Action("#嘴臭 {status}")
    fun mouthOdor(qqGroupEntity: QQGroupEntity, status: Boolean): String{
        qqGroupEntity.mouthOdor = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "嘴臭（祖安语录）已开启！！" else "嘴臭（祖安语录）已关闭！！"
    }

    @Action("鉴黄 {open}")
    fun pic(qqGroupEntity: QQGroupEntity, open: Boolean): String{
        qqGroupEntity.pic = open
        qqGroupService.save(qqGroupEntity)
        return if (open) "鉴黄已开启！！" else "鉴黄已关闭！！"
    }

    @Action("禁言 {member}")
    fun shutUp(group: Long, member: Member, @PathVar(2) timeStr: String?): String{
        val time = if (timeStr == null) 0
        else {
            val num = timeStr.substring(0, timeStr.length - 1).toInt()
            when (timeStr[timeStr.length - 1]) {
                's' -> num
                'm' -> num * 60
                'h' -> num * 60 * 60
                'd' -> num * 60 * 60 * 24
                else -> return "禁言时间格式不正确"
            }
        }
        yuq.groups[group]?.get(member.id)?.ban(time)
        return "禁言成功！！"
    }

    @Action("t {member}")
    fun kick(member: Member, group: Long): String{
        yuq.groups[group]?.get(member.id)?.kick()
        return "踢出成功！！"
    }

    @Action("{act}违规词/{key}")
    fun addKey(key: String, act: String, qqGroupEntity: QQGroupEntity): String?{
        val keywordJsonArray = qqGroupEntity.getKeywordJsonArray()
        val msg = when (act) {
            "加" -> {
                keywordJsonArray.add(key)
                "加违规词成功！！"
            }
            "去" -> {
                keywordJsonArray.remove(key)
                "去违规词成功！！"
            }
            else -> null
        }
        return if (msg != null) {
            qqGroupEntity.keyword = keywordJsonArray.toString()
            qqGroupService.save(qqGroupEntity)
            msg
        }else null
    }

    @Action("加黑 {member}")
    fun addBlack(member: Member, qqGroupEntity: QQGroupEntity, group: Long): String{
        val blackJsonArray = qqGroupEntity.getBlackJsonArray()
        blackJsonArray.add(member.id.toString())
        qqGroupEntity.blackList = blackJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        this.kick(member, group)
        return "加黑名单成功！！"
    }

    @Action("去黑 {qqStr}")
    fun delBlack(qqStr: String, qqGroupEntity: QQGroupEntity): String{
        val blackJsonArray = qqGroupEntity.getBlackJsonArray()
        blackJsonArray.remove(qqStr)
        qqGroupEntity.blackList = blackJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        return "删除黑名单成功！！"
    }

    @Action("黑名单")
    fun blackList(qqGroupEntity: QQGroupEntity): String{
        val blackJsonArray = qqGroupEntity.getBlackJsonArray()
        val sb = StringBuilder("本群黑名单如下：\r\n")
        blackJsonArray.forEach {
            sb.appendln(it)
        }
        return sb.removeSuffix("\r\n").toString()
    }

    @Action("加白 {member}")
    fun addWhite(member: Member, qqGroupEntity: QQGroupEntity): String{
        val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
        whiteJsonArray.add(member.id.toString())
        qqGroupEntity.whiteList = whiteJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        return "加白名单成功！！"
    }

    @Action("去白 {qqStr}")
    fun delWhite(qqStr: String, qqGroupEntity: QQGroupEntity): String{
        val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
        whiteJsonArray.remove(qqStr)
        qqGroupEntity.whiteList = whiteJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        return "删除白名单成功！！"
    }

    @Action("白名单")
    fun whiteList(qqGroupEntity: QQGroupEntity): String{
        val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
        val sb = StringBuilder("本群白名单如下：\r\n")
        whiteJsonArray.forEach {
            sb.appendln(it)
        }
        return sb.removeSuffix("\r\n").toString()
    }

    @Action("违规词")
    fun keywords(qqGroupEntity: QQGroupEntity): String{
        val keywordJsonArray = qqGroupEntity.getKeywordJsonArray()
        val sb = StringBuilder("本群违规词如下：\n")
        keywordJsonArray.forEach {
            sb.appendln(it)
        }
        return sb.removeSuffix("\r\n").toString()
    }

    @Action("#涩图 {status}")
    fun colorPicSwitch(status: Boolean, qqGroupEntity: QQGroupEntity): String?{
        qqGroupEntity.colorPic = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "涩图功能已开启！" else "涩图功能已关闭！"
    }

    @Action("清屏")
    fun clear(): String{
        val sb = StringBuilder()
        for (i in 0 until 1000) sb.appendln("\n")
        return sb.toString()
    }

    @Action("点歌切换 {type}")
    fun song(type: String, qqGroupEntity: QQGroupEntity): String?{
        var musicType = qqGroupEntity.musicType
        val msg = when (type){
            "qq" -> {
                musicType = "qq"
                "点歌切换为qq源成功"
            }
            "网易" -> {
                musicType = "163"
                "点歌切换为网易源成功"
            }
            else -> null
        }
        return if (msg != null){
            qqGroupEntity.musicType = musicType
            qqGroupService.save(qqGroupEntity)
            msg
        }else null
    }

    @Action("撤回通知 {b}")
    fun recall(qqGroupEntity: QQGroupEntity, b: Boolean): String?{
        qqGroupEntity.recall = b
        qqGroupService.save(qqGroupEntity)
        return if (b) "撤回通知已开启！！" else "撤回通知已关闭!!"
    }

    @Action("问")
    fun qa(session: ContextSession, qq: Long, group: Long, qqGroupEntity: QQGroupEntity): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入问题！！"))
        val q = session.waitNextMessage(300000)
        val qStr = q.firstString()
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请输入回答语句！！"))
        val a = session.waitNextMessage(300000)
        val aStr = a.firstString()
        val jsonArray = qqGroupEntity.getQaJsonArray()
        val jsonObject = JSONObject()
        jsonObject["q"] =  qStr
        jsonObject["a"] = aStr
        jsonArray.add(jsonObject)
        qqGroupEntity.qa = jsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        return "添加问答成功！！"
    }

    @Action("问答")
    fun qaList(qqGroupEntity: QQGroupEntity): String{
        val sb = StringBuilder("本群问答列表如下：\n")
        val qaJsonArray = qqGroupEntity.getQaJsonArray()
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            sb.appendln(jsonObject.getString("q"))
        }
        return sb.removeSuffix("\r\n").toString()
    }

    @Action("删问答/{q}")
    fun delQa(qqGroupEntity: QQGroupEntity, q: String): String{
        val qaJsonArray = qqGroupEntity.getQaJsonArray()
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            if (jsonObject.getString("q") == q){
                qaJsonArray.remove(jsonObject)
                qqGroupEntity.qa = qaJsonArray.toString()
                qqGroupService.save(qqGroupEntity)
                return "删除改问答成功！！"
            }
        }
        return "没有找到该问答，请检查！！！"
    }

    @After
    fun finally(actionContext: BotActionContext) = BotUtils.addAt(actionContext)
}
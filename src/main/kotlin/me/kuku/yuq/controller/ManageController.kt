@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.*
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.entity.DoNone
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.service.GroupService
import me.kuku.yuq.service.QQService
import me.kuku.yuq.service.RecallService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.removeSuffixLine
import java.text.SimpleDateFormat
import javax.inject.Inject
import javax.inject.Named

@GroupController
class BeforeController{
    @Inject
    @field:Named("CommandCountOnTime")
    private lateinit var eh: EhcacheHelp<Int>
    @Inject
    private lateinit var groupService: GroupService

    @Global
    @Before
    fun before(message: Message, group: Long, qq: Long){
        val groupEntity = groupService.findByGroup(group) ?: return
        val maxCount = groupEntity.maxCommandCountOnTime
        if (maxCount < 0) return
        val list = message.toPath()
        if (list.isEmpty()) return
        val command = list[0]
        val key = qq.toString() + command
        var num = eh[key] ?: 0
        if (num >= maxCount) throw DoNone()
        eh[key] = ++num
    }
}

@GroupController
class ManageNotController: QQController(){

    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var recallService: RecallService
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var qqService: QQService

    private val version = "v1.7.0"

    @Before
    fun before(group: Long): GroupEntity{
        return groupService.findByGroup(group) ?: GroupEntity(null, group)
    }

    @Action("查管")
    @Synonym(["查黑名单", "查白名单", "查违规词", "查拦截", "查微博监控", "查哔哩哔哩监控", "查问答"])
    @QMsg(at = true, atNewLine = true)
    fun query(groupEntity: GroupEntity, @PathVar(0) type: String): String?{
        val sb = StringBuilder()
        when (type){
            "查管" -> {
                sb.appendLine("本群管理员列表如下：")
                groupEntity.adminJsonArray.forEach { sb.appendLine(it) }
            }
            "查黑名单" -> {
                sb.appendLine("本群黑名单列表如下：")
                groupEntity.blackJsonArray.forEach { sb.appendLine(it) }
            }
            "查白名单" -> {
                sb.appendLine("本群白名单列表如下：")
                groupEntity.whiteJsonArray.forEach { sb.appendLine(it) }
            }
            "查违规词" -> {
                sb.appendLine("本群违规词列表如下：")
                groupEntity.violationJsonArray.forEach { sb.appendLine(it) }
            }
            "查拦截" -> {
                sb.appendLine("本群被拦截的指令列表如下：")
                groupEntity.interceptJsonArray.forEach { sb.appendLine(it) }
            }
            "查微博监控" -> {
                sb.appendLine("本群微博监控列表如下：")
                groupEntity.weiboJsonArray.forEach {
                    val jsonObject = it as JSONObject
                    sb.appendLine("${jsonObject.getString("id")}-${jsonObject.getString("name")}")
                }
            }
            "查哔哩哔哩监控" -> {
                sb.appendLine("本群哔哩哔哩监控列表如下：")
                groupEntity.biliBiliJsonArray.forEach {
                    val jsonObject = it as JSONObject
                    sb.appendLine("${jsonObject.getString("id")}-${jsonObject.getString("name")}")
                }
            }
            "查问答" -> {
                sb.appendLine("本群问答列表如下：")
                groupEntity.qaJsonArray.forEach {
                    val jsonObject = it as JSONObject
                    sb.appendLine(jsonObject.getString("q"))
                }
            }
            else -> return null
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("查撤回 {qqNo}")
    fun queryRecall(group: Long, qqNo: Long, qq: Long, @PathVar(value = 2, type = PathVar.Type.Integer) numParam: Int?): Message{
        val recallList = recallService.findByGroupAndQQ(group, qqNo)
        val all = recallList.size
        val num = numParam ?: 1
        if (num > all) return mif.at(qq).plus("您要查询的QQ只有${all}条撤回消息，越界了！！")
        val recallEntity = recallList[num - 1]
        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recallEntity.date)
        return mif.at(qq).plus("\n该消息撤回时间为${timeStr}\n消息内容为：\n")
                .plus(BotUtils.jsonArrayToMessage(recallEntity.messageEntity.contentJsonArray))
    }

    @Action("检查版本")
    fun checkUpdate(): String{
        val gitVersion = toolLogic.queryVersion()
        val sb = StringBuilder()
        sb.appendLine("当前程序版本：$version")
        sb.appendLine("最新程序版本：$gitVersion")
        return sb.toString()
    }


    @Action("开关")
    @QMsg(at = true, atNewLine = true)
    fun kai(groupEntity: GroupEntity): String{
        val sb = StringBuilder("本群开关情况如下：\n")
        sb.appendLine("色图：" + this.boolToStr(groupEntity.colorPic) + "、" + groupEntity.colorPicType)
        sb.appendLine("鉴黄：" + this.boolToStr(groupEntity.pic))
        sb.appendLine("欢迎语：" + this.boolToStr(groupEntity.welcomeMsg))
        sb.appendLine("退群拉黑：" + this.boolToStr(groupEntity.leaveGroupBlack))
        sb.appendLine("自动审核：" + this.boolToStr(groupEntity.autoReview))
        sb.appendLine("撤回通知：" + this.boolToStr(groupEntity.recall))
        sb.appendLine("整点报时：" + this.boolToStr(groupEntity.onTimeAlarm))
        sb.appendLine("闪照通知：" + this.boolToStr(groupEntity.flashNotify))
        val maxCommandCountOnTime = groupEntity.maxCommandCountOnTime
        sb.appendLine("指令限制：${if (maxCommandCountOnTime == -1) "无限制" else "$maxCommandCountOnTime"}")
        sb.append("最大违规次数：${groupEntity.maxViolationCount}")
        return sb.toString()
    }

    @Action("查询违规")
    @QMsg(at = true)
    fun queryVio(qq: Long, group: Long): String{
        val qqEntity = qqService.findByQQAndGroup(qq, group)
        return "您在本群违规次数为：" + (qqEntity?.violationCount ?: 0)
    }

    private fun boolToStr(b: Boolean?) = if (b == true) "开" else "关"
}

@Suppress("DuplicatedCode")
@GroupController
class ManageOwnerController: QQController() {
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic
    @Inject
    private lateinit var recallService: RecallService

    @Before
    fun before(group: Long, qq: Long): GroupEntity{
        val groupEntity = groupService.findByGroup(group) ?: GroupEntity(null, group)
        if (qq.toString() == master){
            return groupEntity
        }else throw mif.at(qq).plus("您的权限不足，无法执行！！").toThrowable()
    }

    @Action("加管 {content}")
    @Synonym(["加违规词 {content}", "加黑名单 {content}", "加白名单 {content}", "加拦截 {content}",
        "加微博监控 {content}", "加哔哩哔哩监控 {content}"])
    @QMsg(at = true)
    fun add(groupEntity: GroupEntity, @PathVar(0) type: String, content: String): String?{
        when (type){
            "加管" -> {
                groupEntity.adminList = groupEntity.adminJsonArray.fluentAdd(content).toString()
            }
            "加违规词" -> {
                groupEntity.violationList = groupEntity.violationJsonArray.fluentAdd(content).toString()
            }
            "加黑名单" -> {
                groupEntity.blackList = groupEntity.blackJsonArray.fluentAdd(content).toString()
            }
            "加白名单" -> {
                groupEntity.whiteList = groupEntity.whiteJsonArray.fluentAdd(content).toString()
            }
            "加拦截" -> {
                groupEntity.interceptList = groupEntity.interceptJsonArray.fluentAdd(content).toString()
            }
            "加微博监控" -> {
                val commonResult = weiboLogic.getIdByName(content)
                val weiboPojo = commonResult.t?.get(0) ?: return "该用户不存在！！"
                val jsonObject = JSONObject()
                jsonObject["id"] = weiboPojo.userId
                jsonObject["name"] = weiboPojo.name
                groupEntity.weiboList = groupEntity.weiboJsonArray.fluentAdd(jsonObject).toString()
            }
            "加哔哩哔哩监控" -> {
                val commonResult = biliBiliLogic.getIdByName(content)
                val biliBiliPojo = commonResult.t?.get(0) ?: return "该用户不存在！！"
                val jsonObject = JSONObject()
                jsonObject["id"] = biliBiliPojo.userId
                jsonObject["name"] = biliBiliPojo.name
                groupEntity.biliBiliList = groupEntity.biliBiliJsonArray.fluentAdd(jsonObject).toString()
            }
            else -> return null
        }
        groupService.save(groupEntity)
        return "${type}成功"
    }

    @Action("删管 {content}")
    @Synonym(["删违规词 {content}", "删黑名单 {content}", "删白名单 {content}", "删拦截 {content}",
        "删微博监控 {content}", "删哔哩哔哩监控 {content}"])
    @QMsg(at = true)
    fun del(groupEntity: GroupEntity, @PathVar(0) type: String, content: String): String?{
        when (type){
            "删管" -> {
                val adminJsonArray = groupEntity.adminJsonArray
                BotUtils.delManager(adminJsonArray, content)
                groupEntity.adminList = adminJsonArray.toString()
            }
            "删违规词" -> {
                val violationJsonArray = groupEntity.violationJsonArray
                BotUtils.delManager(violationJsonArray, content)
                groupEntity.violationList = violationJsonArray.toString()
            }
            "删黑名单" -> {
                val blackJsonArray = groupEntity.blackJsonArray
                BotUtils.delManager(blackJsonArray, content)
                groupEntity.blackList = blackJsonArray.toString()
            }
            "删白名单" -> {
                val whiteJsonArray = groupEntity.whiteJsonArray
                BotUtils.delManager(whiteJsonArray, content)
                groupEntity.whiteList = whiteJsonArray.toString()
            }
            "删拦截" -> {
                val interceptJsonArray = groupEntity.interceptJsonArray
                BotUtils.delManager(interceptJsonArray, content)
                groupEntity.interceptList = interceptJsonArray.toString()
            }
            "删微博监控" -> {
                val weiboJsonArray = groupEntity.weiboJsonArray
                BotUtils.delMonitorList(weiboJsonArray, content)
                groupEntity.weiboList = weiboJsonArray.toString()
            }
            "删哔哩哔哩监控" -> {
                val biliBiliJsonArray = groupEntity.biliBiliJsonArray
                BotUtils.delMonitorList(biliBiliJsonArray, content)
                groupEntity.biliBiliList = biliBiliJsonArray.toString()
            }
            else -> return null
        }
        groupService.save(groupEntity)
        return "${type}成功！！"
    }

    @Action("违规次数 {count}")
    @QMsg(at = true)
    fun maxViolationCount(groupEntity: GroupEntity, count: Int): String{
        groupEntity.maxViolationCount = count
        groupService.save(groupEntity)
        return "已设置本群最大违规次数为${count}次"
    }

    @Action("指令限制 {count}")
    @QMsg(at = true)
    fun maxCommandCount(groupEntity: GroupEntity, count: Int): String{
        groupEntity.maxCommandCountOnTime = count
        groupService.save(groupEntity)
        return "已设置本群单个指令每人每分钟最大触发次数为${count}次"
    }

    @Action("色图切换 {type}")
    @QMsg(at = true)
    fun colorPicType(groupEntity: GroupEntity, type: String): String?{
        val colorPicType: String
        when (type){
            "danbooru", "lolicon", "loliconR18" -> colorPicType = type
            else -> return "没有该类型，请重试！！"
        }
        groupEntity.colorPicType = colorPicType
        groupService.save(groupEntity)
        return "涩图切换成${type}成功"
    }

    @Action("加问答 {q}")
    @QMsg(at = true)
    fun qa(session: ContextSession, qq: Long, groupEntity: GroupEntity, q: String): String{
        reply(mif.at(qq).plus("请输入回答语句！！"))
        val a = session.waitNextMessage()
        val jsonObject = JSONObject()
        val aJsonArray = BotUtils.messageToJsonArray(a)
        jsonObject["q"] =  q
        jsonObject["a"] = aJsonArray
        jsonObject["type"] = "PARTIAL"
        val jsonArray = groupEntity.qaJsonArray
        jsonArray.add(jsonObject)
        groupEntity.qaList = jsonArray.toString()
        groupService.save(groupEntity)
        return "添加问答成功！！"
    }

    @Action("删问答 {q}")
    @QMsg(at = true)
    fun delQa(groupEntity: GroupEntity, q: String): String{
        val qaJsonArray = groupEntity.qaJsonArray
        val delList = mutableListOf<JSONObject>()
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            if (jsonObject.getString("q") == q){
                delList.add(jsonObject)
            }
        }
        delList.forEach { qaJsonArray.remove(it) }
        groupEntity.qaList = qaJsonArray.toString()
        groupService.save(groupEntity)
        return "删除问答成功！！"
    }
}

@GroupController
class ManageAdminController: QQController(){

    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var groupService: GroupService

    @Before
    fun before(qq: Long, group: Long): GroupEntity{
        val groupEntity = groupService.findByGroup(group) ?: GroupEntity(null, group)
        val adminJSONArray = groupEntity.adminJsonArray
        if (qq.toString() in adminJSONArray || qq == master.toLong()){
            return groupEntity
        }else throw mif.at(qq).plus("您的权限不足，无法执行！！").toThrowable()
    }

    @Action("清屏")
    fun clear(): String{
        val sb = StringBuilder()
        for (i in 0 until 1000) sb.appendLine()
        return sb.toString()
    }

    @Action("t {qqNo}")
    @QMsg(at = true)
    fun kick(qqNo: Long, group: Long): String{
        yuq.groups[group]?.get(qqNo)?.kick()
        return "踢出成功！！"
    }

    @Action("禁言 {qqNo}")
    @QMsg(at = true)
    fun shutUp(group: Long, qqNo: Long, @PathVar(2) timeStr: String?): String{
        val time = if (timeStr == null) 0
        else {
            if (timeStr.length == 1) return "未发现时间单位！！单位可为（s,m,h,d）"
            val num = timeStr.substring(0, timeStr.length - 1).toInt()
            when (timeStr[timeStr.length - 1]) {
                's' -> num
                'm' -> num * 60
                'h' -> num * 60 * 60
                'd' -> num * 60 * 60 * 24
                else -> return "禁言时间格式不正确"
            }
        }
        yuq.groups[group]?.get(qqNo)?.ban(time)
        return "禁言成功！！"
    }

    @Action("机器人 {status}")
    @Synonym(["loc监控 {status}", "整点报时 {status}", "自动审核 {status}",
        "欢迎语 {status}", "退群拉黑 {status}", "鉴黄 {status}", "色图 {status}",
        "撤回通知 {status}", "闪照通知 {status}"])
    @QMsg(at = true)
    fun onOrOff(groupEntity: GroupEntity, status: Boolean, @PathVar(0) op: String): String?{
        when (op){
            "机器人" -> groupEntity.status = status
            "loc监控" -> groupEntity.locMonitor = status
            "整点报时" -> groupEntity.onTimeAlarm = status
            "自动审核" -> groupEntity.autoReview = status
            "欢迎语" -> groupEntity.welcomeMsg = status
            "退群拉黑" -> groupEntity.leaveGroupBlack = status
            "鉴黄" -> groupEntity.pic = status
            "色图" -> groupEntity.colorPic = status
            "撤回通知" -> groupEntity.recall = status
            "闪照通知" -> groupEntity.flashNotify = status
            else -> return null
        }
        groupService.save(groupEntity)
        return if (status) "${op}开启成功" else "${op}关闭成功"
    }
}
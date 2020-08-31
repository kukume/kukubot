package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.*
import com.IceCreamQAQ.Yu.util.IO
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.*
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.removeSuffixLine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.system.exitProcess

@GroupController
class ManagerController: QQController() {
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic

    private val version = "v1.5.6"

    @Before
    fun before(group: Long, qq: Long, actionContext: BotActionContext, message: Message){
        var qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity == null){
            qqGroupEntity = QQGroupEntity(null, group)
            qqGroupService.save(qqGroupEntity)
        }
        actionContext.session["qqGroupEntity"] = qqGroupEntity
        val msg = message.toPath()[0]
        val whiteList = arrayOf("问答", "违规词", "黑名单", "查黑", "白名单", "查白", "开关", "查撤回", "查管", "查微博监控", "检查更新")
        val adminWhiteList = qqGroupEntity.getAllowedCommandsJsonArray()
        if (!whiteList.contains(msg)) {
            val adminJsonArray = qqGroupEntity.getAdminJsonArray()
            if (!adminJsonArray.contains(qq.toString()) || !adminWhiteList.contains(msg)) {
                if (qq != master.toLong()) throw mif.at(qq).plus("抱歉，您的权限不足，无法执行！！")
            }
        }
    }

    @Action("查{op}")
    @QMsg(at = true)
    fun query(qqGroupEntity: QQGroupEntity, op: String): String?{
        val jsonArray: JSONArray
        val msg: String
        when (op){
            "管" -> {
                jsonArray = qqGroupEntity.getAdminJsonArray()
                msg = "本群机器人的管理员如下："
            }
            "admin命令" -> {
                jsonArray = qqGroupEntity.getAllowedCommandsJsonArray()
                msg = "本群管理员拥有的管理命令使用权限如下："
            }
            "黑" -> {
                jsonArray = qqGroupEntity.getBlackJsonArray()
                msg = "本群黑名单如下："
            }
            "白" -> {
                jsonArray = qqGroupEntity.getWhiteJsonArray()
                msg = "本群白名单如下："
            }
            "违规词" -> {
                jsonArray = qqGroupEntity.getKeywordJsonArray()
                msg = "本群违规词如下："
            }
            "拦截" -> {
                jsonArray = qqGroupEntity.getInterceptJsonArray()
                msg = "本群以下指令不会被响应："
            }
            else -> return null
        }
        val sb = StringBuilder().appendln(msg)
        jsonArray.forEach { sb.appendln(it) }
        return sb.removeSuffixLine().toString()
    }

    @Action("加{op} {content}")
    @QMsg(at = true)
    fun add(qqGroupEntity: QQGroupEntity, op: String, content: String, group: Long): Message?{
        val msg = when (op){
            "管" -> {
                val adminJsonArray = qqGroupEntity.getAdminJsonArray()
                adminJsonArray.add(content)
                qqGroupEntity.adminList = adminJsonArray.toString()
                mif.text("设置").plus(mif.at(content.toLong())).plus("为管理员成功！！")
            }
            "admin命令" -> {
                val allowedCommandsJsonArray = qqGroupEntity.getAllowedCommandsJsonArray()
                allowedCommandsJsonArray.add(content)
                qqGroupEntity.allowedCommandsList = allowedCommandsJsonArray.toString()
                "添加管理员${content}的命令权限成功！！".toMessage()
            }
            "违规词" -> {
                val keywordJsonArray = qqGroupEntity.getKeywordJsonArray()
                keywordJsonArray.add(content)
                qqGroupEntity.keyword = keywordJsonArray.toString()
                "添加违规词[${content}]成功！！".toMessage()
            }
            "黑" -> {
                val blackJsonArray = qqGroupEntity.getBlackJsonArray()
                blackJsonArray.add(content)
                qqGroupEntity.blackList = blackJsonArray.toString()
                val members = yuq.groups[group]?.members
                val qqNo = content.toLong()
                if (members!!.containsKey(qqNo))
                    this.kick(qqNo, group)
                "添加黑名单成功！！".toMessage()
            }
            "白" -> {
                val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
                whiteJsonArray.add(content)
                qqGroupEntity.whiteList = whiteJsonArray.toString()
                "添加白名单成功！！".toMessage()
            }
            "拦截" -> {
                val interceptJsonArray = qqGroupEntity.getInterceptJsonArray()
                interceptJsonArray.add(content)
                qqGroupEntity.interceptList = interceptJsonArray.toString()
                "${content}指令将不会再响应".toMessage()
            }
            else -> return null
        }
        qqGroupService.save(qqGroupEntity)
        return msg
    }

    @Action("删{op} {content}")
    @QMsg(at = true)
    fun del(qqGroupEntity: QQGroupEntity, op: String, content: String): Message?{
        val msg = when (op){
            "管" -> {
                val adminJsonArray = qqGroupEntity.getAdminJsonArray()
                BotUtils.delManager(adminJsonArray, content)
                qqGroupEntity.adminList = adminJsonArray.toString()
                mif.text("取消").plus(mif.at(content.toLong())).plus("的管理员成功！！")
            }
            "admin命令" -> {
                val allowedCommandsJsonArray = qqGroupEntity.getAllowedCommandsJsonArray()
                BotUtils.delManager(allowedCommandsJsonArray, content)
                qqGroupEntity.allowedCommandsList = allowedCommandsJsonArray.toString()
                "删除管理员${content}的命令权限成功！！".toMessage()
            }
            "违规词" -> {
                val keywordJsonArray = qqGroupEntity.getKeywordJsonArray()
                BotUtils.delManager(keywordJsonArray, content)
                qqGroupEntity.keyword = keywordJsonArray.toString()
                "删除违规词[${content}]成功！！".toMessage()
            }
            "黑" -> {
                val blackJsonArray = qqGroupEntity.getBlackJsonArray()
                BotUtils.delManager(blackJsonArray, content)
                qqGroupEntity.blackList = blackJsonArray.toString()
                "删除黑名单成功！！".toMessage()
            }
            "白" -> {
                val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
                BotUtils.delManager(whiteJsonArray, content)
                qqGroupEntity.whiteList = whiteJsonArray.toString()
                "加白名单成功！！".toMessage()
            }
            "拦截" -> {
                val interceptJsonArray = qqGroupEntity.getInterceptJsonArray()
                BotUtils.delManager(interceptJsonArray, content)
                qqGroupEntity.interceptList = interceptJsonArray.toString()
                "${content}指令将不会再被拦截".toMessage()
            }
            else -> return null
        }
        qqGroupService.save(qqGroupEntity)
        return msg
    }

    @Action("机器人 {status}")
    @Synonym(["机器人 {status}", "loc监控 {status}", "复读 {status}", "整点报时 {status}", "自动审核 {status}",
        "#龙王 {status}", "#qq {status}", "欢迎语 {status}", "萌宠 {status}", "退群拉黑 {status}", "#嘴臭 {status}",
        "#祖安语录 {status}", "鉴黄 {status}", "#涩图 {status}", "撤回通知 {status}", "闪照通知 {status}"])
    @QMsg(at = true)
    fun onOrOff(qqGroupEntity: QQGroupEntity, status: Boolean, @PathVar(0) op: String): String?{
        when (op){
            "机器人" -> qqGroupEntity.status = status
            "loc监控" -> qqGroupEntity.locMonitor = status
            "复读" -> qqGroupEntity.repeat = status
            "整点报时" -> qqGroupEntity.onTimeAlarm = status
            "自动审核" -> qqGroupEntity.autoReview = status
            "#龙王" -> qqGroupEntity.dragonKing = status
            "#qq" -> qqGroupEntity.qqStatus = status
            "欢迎语" -> qqGroupEntity.welcomeMsg = status
            "萌宠" -> qqGroupEntity.superCute = status
            "退群拉黑" -> qqGroupEntity.leaveGroupBlack = status
            "#嘴臭", "#祖安语录" -> qqGroupEntity.mouthOdor = status
            "鉴黄" -> qqGroupEntity.pic = status
            "#涩图" -> qqGroupEntity.colorPic = status
            "撤回通知" -> qqGroupEntity.recall = status
            "闪照通知" -> qqGroupEntity.flashNotify = status
            else -> return null
        }
        qqGroupService.save(qqGroupEntity)
        return if (status) "${op}开启成功" else "${op}关闭成功"
    }

    @Action("{op}监控 {name}")
    @QMsg(at = true)
    fun addMonitor(qqGroupEntity: QQGroupEntity, op: String, name: String): String?{
        val jsonObject = JSONObject()
        when (op){
            "微博" -> {
                val commonResult = weiboLogic.getIdByName(name)
                val weiboPojo = commonResult.t?.get(0) ?: return commonResult.msg
                val weiboJsonArray = qqGroupEntity.getWeiboJsonArray()
                jsonObject["name"] = weiboPojo.name
                jsonObject["id"] = weiboPojo.userId
                weiboJsonArray.add(jsonObject)
                qqGroupEntity.weiboList = weiboJsonArray.toString()
            }
            "哔哩哔哩" -> {
                val commonResult = biliBiliLogic.getIdByName(name)
                val list = commonResult.t ?: return commonResult.msg
                val biliBiliPojo = list[0]
                jsonObject["name"] = biliBiliPojo.name
                jsonObject["id"] = biliBiliPojo.userId
                val biliBiliJsonArray = qqGroupEntity.getBiliBiliJsonArray()
                biliBiliJsonArray.add(jsonObject)
                qqGroupEntity.biliBiliList = biliBiliJsonArray.toString()
            }
            else -> return null
        }
        qqGroupService.save(qqGroupEntity)
        return "添加${op}的用户[${jsonObject["name"]}]的监控成功！！"
    }

    @Action("删微博监控 {name}")
    @Synonym(["删哔哩哔哩监控 {name}"])
    @QMsg(at = true)
    fun delMonitor(qqGroupEntity: QQGroupEntity, name: String, @PathVar(0) op: String): String?{
        when {
            "微博" in op -> {
                val weiboJsonArray = qqGroupEntity.getWeiboJsonArray()
                BotUtils.delMonitorList(weiboJsonArray, name)
                qqGroupEntity.weiboList = weiboJsonArray.toString()
            }
            "哔哩哔哩" in op -> {
                val biliBiliJsonArray = qqGroupEntity.getBiliBiliJsonArray()
                BotUtils.delMonitorList(biliBiliJsonArray, name)
                qqGroupEntity.biliBiliList = biliBiliJsonArray.toString()
            }
            else -> return null
        }
        qqGroupService.save(qqGroupEntity)
        return "删除用户[${name}]的监控成功！！"
    }

    @Action("查微博监控")
    @Synonym(["查哔哩哔哩监控"])
    @QMsg(at = true)
    fun queryMonitor(qqGroupEntity: QQGroupEntity, @PathVar(0) op: String): String?{
        val jsonArray: JSONArray
        val msg: String
        when {
            "微博" in op -> {
                msg = "该群微博监控如下："
                jsonArray = qqGroupEntity.getWeiboJsonArray()
            }
            "哔哩哔哩" in op -> {
                msg = "本群的哔哩哔哩用户监控如下："
                jsonArray = qqGroupEntity.getBiliBiliJsonArray()
            }
            else -> return null
        }
        val sb = StringBuilder().appendln(msg)
        for (i in jsonArray.indices){
            val jsonObject = jsonArray.getJSONObject(i)
            sb.appendln("${jsonObject.getString("name")}-${jsonObject.getString("id")}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("违规次数 {count}")
    @QMsg(at = true)
    fun maxViolationCount(qqGroupEntity: QQGroupEntity, count: Int): String{
        qqGroupEntity.maxViolationCount = count
        qqGroupService.save(qqGroupEntity)
        return "已设置本群最大违规次数为${count}次"
    }

    @Action("开关")
    @QMsg(at = true)
    fun kai(qqGroupEntity: QQGroupEntity): String{
        val sb = StringBuilder("本群开关情况如下：\n")
        sb.appendln("音乐：${qqGroupEntity.musicType}")
        sb.appendln("色图：" + this.boolToStr(qqGroupEntity.colorPic) + "、" + qqGroupEntity.colorPicType)
        sb.appendln("鉴黄：" + this.boolToStr(qqGroupEntity.pic))
        sb.appendln("嘴臭：" + this.boolToStr(qqGroupEntity.mouthOdor))
        sb.appendln("龙王：" + this.boolToStr(qqGroupEntity.dragonKing))
        sb.appendln("复读：" + this.boolToStr(qqGroupEntity.repeat))
        sb.appendln("欢迎语：" + this.boolToStr(qqGroupEntity.welcomeMsg))
        sb.appendln("qq功能：" + this.boolToStr(qqGroupEntity.qqStatus))
        sb.appendln("退群拉黑：" + this.boolToStr(qqGroupEntity.leaveGroupBlack))
        sb.appendln("萌宠功能：" + this.boolToStr(qqGroupEntity.superCute))
        sb.appendln("自动审核：" + this.boolToStr(qqGroupEntity.autoReview))
        sb.appendln("撤回通知：" + this.boolToStr(qqGroupEntity.recall))
        sb.appendln("整点报时：" + this.boolToStr(qqGroupEntity.onTimeAlarm))
        sb.appendln("闪照通知：" + this.boolToStr(qqGroupEntity.flashNotify))
        sb.append("最大违规次数：${qqGroupEntity.maxViolationCount ?: "5次"}")
        return sb.toString()
    }

    @Action("涩图切换 {type}")
    @QMsg(at = true)
    fun colorPicType(qqGroupEntity: QQGroupEntity, type: String): String?{
        var colorPicType = qqGroupEntity.colorPicType
        var status = true
        when (type){
            "native", "r-18", "danbooru" -> colorPicType = type
            else -> status = false
        }
        return if (status){
            qqGroupEntity.colorPicType = colorPicType
            qqGroupService.save(qqGroupEntity)
            "涩图切换${type}成功"
        }else {
            qqGroupEntity.colorPic = false
            qqGroupService.save(qqGroupEntity)
            "涩图关闭成功！！"
        }
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

    @Action("t {qqNo}")
    @QMsg(at = true)
    fun kick(qqNo: Long, group: Long): String{
        yuq.groups[group]?.get(qqNo)?.kick()
        return "踢出成功！！"
    }

    @Action("清屏")
    fun clear(): String{
        val sb = StringBuilder()
        for (i in 0 until 1000) sb.appendln()
        return sb.toString()
    }

    @Action("点歌切换 {type}")
    @QMsg(at = true)
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

    @Action("问")
    @QMsg(at = true)
    fun qa(session: ContextSession, qq: Long, qqGroupEntity: QQGroupEntity): String{
        reply(mif.at(qq).plus("请输入问题！！"))
        val q = session.waitNextMessage(300000)
        val qStr = q.firstString()
        reply(mif.at(qq).plus("请输入回答语句！！"))
        val a = session.waitNextMessage(300000)
        val jsonObject = JSONObject()
        val aJsonArray = BotUtils.messageToJsonArray(a)
        if (aJsonArray.size == 0) return "回答的语句暂只支持文本、图片、表情、xml消息、json消息！！"
        jsonObject["q"] =  qStr
        jsonObject["a"] = aJsonArray
        val jsonArray = qqGroupEntity.getQaJsonArray()
        jsonArray.add(jsonObject)
        qqGroupEntity.qa = jsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        return "添加问答成功！！"
    }

    @Action("问答")
    @Synonym(["查问答"])
    fun qaList(qqGroupEntity: QQGroupEntity): String{
        val sb = StringBuilder().appendln("本群问答列表如下：")
        val qaJsonArray = qqGroupEntity.getQaJsonArray()
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            sb.appendln(jsonObject.getString("q"))
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("删问答")
    @QMsg(at = true)
    fun delQa(qqGroupEntity: QQGroupEntity, message: Message): String{
        val msg = message.firstString()
        if (msg.length <= 4) return "请输入需要删除的问答！！"
        val q = msg.substring(4)
        val qaJsonArray = qqGroupEntity.getQaJsonArray()
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            if (jsonObject.getString("q") == q){
                qaJsonArray.remove(jsonObject)
                qqGroupEntity.qa = qaJsonArray.toString()
                qqGroupService.save(qqGroupEntity)
                return "删除问答成功！！"
            }
        }
        return "没有找到该问答，请检查！！！"
    }

    @Action("检查更新")
    fun checkUpdate(qq: Long): String{
        val gitVersion = toolLogic.queryVersion()
        val sb = StringBuilder()
        sb.appendln("当前程序版本：$version")
        sb.appendln("最新程序版本：$gitVersion")
        if (qq == master.toLong()) {
            if (gitVersion > version) {
                sb.appendln("更新日志：https://github.com/kukume/kuku-bot/releases/tag/$gitVersion")
                sb.append("发现程序可更新，正在下载中！！！")
                reply(sb.toString())
                val gitUrl = "https://github.com/kukume/kuku-bot/releases/download/$gitVersion/kukubot.jar"
                val response = OkHttpClientUtils.get(toolLogic.githubQuicken(gitUrl))
                val bytes = OkHttpClientUtils.getBytes(response)
                IO.writeFile(File("${System.getProperty("user.dir")}${File.separator}kukubot.jar"), bytes)
                reply("更新完成，请前往控制台手动启动程序！！")
                exitProcess(0)
            } else sb.append("暂未发现需要更新")
        }
        return sb.toString()
    }

    @Action("查撤回 {qqNo}")
    fun queryRecall(qqGroupEntity: QQGroupEntity, qqNo: Long, qq: Long, @PathVar(value = 2, type = PathVar.Type.Integer) numParam: Int?): Message{
        val recallMessageJsonArray = qqGroupEntity.getRecallMessageJsonArray()
        val list = mutableListOf<JSONObject>()
        var messageSize = recallMessageJsonArray.size - 1
        while (messageSize >= 0){
            val jsonObject = recallMessageJsonArray.getJSONObject(messageSize--)
            if (jsonObject.getLong("qq") == qqNo) list.add(jsonObject)
        }
        val all = list.size
        if (all == 0) return mif.at(qq).plus("该qq并没有撤回消息")
        val num = numParam ?: 1
        if (num > all) return mif.at(qq).plus("该qq一共有${all}条消息，超过范围了！！")
        val jsonObject = list[num - 1]
        val sendMessage = mif.text("群成员：").plus(mif.at(qqNo)).plus("共有${all}条撤回消息")
        val time = jsonObject.getLong("time")
        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(time))
        sendMessage.plus("\n这是第${num}条撤回消息！！撤回时间为$timeStr").plus("\n消息内容为：\n")
        val jsonArray = jsonObject.getJSONArray("message")
        val contentMessage = BotUtils.jsonArrayToMessage(jsonArray)
        return sendMessage.plus(contentMessage)
    }

    private fun boolToStr(b: Boolean?) = if (b == true) "开" else "关"
}
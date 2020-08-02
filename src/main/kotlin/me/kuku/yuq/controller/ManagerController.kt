package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.util.IO
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.*
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mirai.MiraiBot
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.logic.PiXivLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.removeSuffixLine
import java.io.File
import java.net.SocketException
import javax.inject.Inject
import kotlin.system.exitProcess

@GroupController
class ManagerController {
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var miraiBot: MiraiBot
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var piXivLogic: PiXivLogic
    @Config("YuQ.Mirai.bot.pCookie")
    private lateinit var pCookie:String


    private val version = "v1.4.1"

    @Before
    fun before(group: Long, qq: Long, actionContext: BotActionContext, message: Message){
        var qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity == null){
            qqGroupEntity = QQGroupEntity(null, group)
            qqGroupService.save(qqGroupEntity)
        }
        actionContext.session["qqGroupEntity"] = qqGroupEntity
        val whiteList = arrayOf("问答", "违规词", "黑名单", "白名单")
        if (!whiteList.contains(message.toPath()[0])) {
            if (qq != master.toLong()) throw mif.at(qq).plus("抱歉，您不是机器人主人，无法执行！！")
        }
    }

    @Action("机器人 {status}")
    fun switchGroup(qqGroupEntity: QQGroupEntity, status: Boolean): String?{
        qqGroupEntity.status = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "机器人开启成功" else "机器人关闭成功"
    }

    @Action("开关")
    fun kai(qqGroupEntity: QQGroupEntity): String{
        val sb = StringBuilder("本群开关情况如下：\n")
        sb.appendln("音乐：${qqGroupEntity.musicType}")
        sb.appendln("色图：" + this.boolToStr(qqGroupEntity.colorPic))
        sb.appendln("鉴黄：" + this.boolToStr(qqGroupEntity.pic))
        sb.appendln("嘴臭：" + this.boolToStr(qqGroupEntity.mouthOdor))
        sb.appendln("涩图：${if (qqGroupEntity.colorPicType == "local")  "本地" else "远程"}")
        sb.appendln("欢迎语：" + this.boolToStr(qqGroupEntity.welcomeMsg))
        sb.appendln("qq功能：" + this.boolToStr(qqGroupEntity.qqStatus))
        sb.appendln("退群拉黑：" + this.boolToStr(qqGroupEntity.leaveGroupBlack))
        sb.appendln("萌宠功能：" + this.boolToStr(qqGroupEntity.superCute))
        sb.appendln("自动审核：" + this.boolToStr(qqGroupEntity.autoReview))
        sb.appendln("撤回通知：" + this.boolToStr(qqGroupEntity.recall))
        sb.append("整点报时：" + this.boolToStr(qqGroupEntity.onTimeAlarm))
        return sb.toString()
    }

    @Action("重启mirai")
    fun robot(){
        miraiBot.stop()
        miraiBot.init()
        miraiBot.start()
    }

    @Action("r18 {status}")
    fun r18setting(status: Boolean, qqGroupEntity: QQGroupEntity): String{
        return when (qqGroupEntity.colorPicType){
            "remote" -> toolLogic.r18setting(pCookie, status)
            "local" -> {
                try {
                    piXivLogic.r18setting(pCookie, status)
                }catch (e: SocketException){
                    if (e.message == "Connection reset")
                        "抱歉，该服务器不能访问p站，请发送（涩图切换 远程）"
                    else "出现异常了，异常信息为：${e.message}"
                }
            }
            else -> toolLogic.r18setting(pCookie, status)
        }
    }

    @Action("涩图切换 {type}")
    fun colorPicType(qqGroupEntity: QQGroupEntity, type: String): String?{
        var colorPicType = qqGroupEntity.colorPicType
        var status = true
        when (type){
            "本地" -> colorPicType = "local"
            "远程" -> colorPicType = "remote"
            else -> status = false
        }
        return if (status){
            qqGroupEntity.colorPicType = colorPicType
            qqGroupService.save(qqGroupEntity)
            "涩图切换${type}成功"
        }else null
    }

    @Action("整点报时 {status}")
    fun onTimeAlarm(qqGroupEntity: QQGroupEntity, status: Boolean): String{
        qqGroupEntity.onTimeAlarm = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "整点报时开启成功" else "整点报时关闭成功"
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

    @Action("禁言 {qqNo}")
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
    fun kick(qqNo: Long, group: Long): String{
        yuq.groups[group]?.get(qqNo)?.kick()
        return "踢出成功！！"
    }

    @Action("{act}违规词")
    fun addKey(message: Message, act: String, qqGroupEntity: QQGroupEntity): String?{
        val keywordJsonArray = qqGroupEntity.getKeywordJsonArray()
        val list = message.toPath().toMutableList()
        list.removeAt(0)
        val msg = when (act) {
            "加" -> {
                list.forEach { keywordJsonArray.add(it) }

                "加违规词成功！！"
            }
            "去" -> {
                list.forEach { keywordJsonArray.remove(it) }
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

    @Action("加黑 {qqNo}")
    fun addBlack(qqNo: Long, qqGroupEntity: QQGroupEntity, group: Long): String{
        val blackJsonArray = qqGroupEntity.getBlackJsonArray()
        blackJsonArray.add(qqNo.toString())
        qqGroupEntity.blackList = blackJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        val members = yuq.groups[group]?.members
        if (members!!.containsKey(qqNo))
            this.kick(qqNo, group)
        return "加黑名单成功！！"
    }

    @Action("去黑 {qqNo}")
    fun delBlack(qqNo: Long, qqGroupEntity: QQGroupEntity): String{
        val blackJsonArray = qqGroupEntity.getBlackJsonArray()
        blackJsonArray.remove(qqNo.toString())
        qqGroupEntity.blackList = blackJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        return "删除黑名单成功！！"
    }

    @Action("黑名单")
    fun blackList(qqGroupEntity: QQGroupEntity): String{
        val blackJsonArray = qqGroupEntity.getBlackJsonArray()
        val sb = StringBuilder().appendln("本群黑名单如下：")
        blackJsonArray.forEach {
            sb.appendln(it)
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("加白 {qqNo}")
    fun addWhite(qqNo: Long, qqGroupEntity: QQGroupEntity): String{
        val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
        whiteJsonArray.add(qqNo.toString())
        qqGroupEntity.whiteList = whiteJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        return "加白名单成功！！"
    }

    @Action("去白 {qqNo}")
    fun delWhite(qqNo: Long, qqGroupEntity: QQGroupEntity): String{
        val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
        whiteJsonArray.remove(qqNo.toString())
        qqGroupEntity.whiteList = whiteJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        return "删除白名单成功！！"
    }

    @Action("白名单")
    fun whiteList(qqGroupEntity: QQGroupEntity): String{
        val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
        val sb = StringBuilder().appendln("本群白名单如下：")
        whiteJsonArray.forEach {
            sb.appendln(it)
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("违规词")
    fun keywords(qqGroupEntity: QQGroupEntity): String{
        val keywordJsonArray = qqGroupEntity.getKeywordJsonArray()
        val sb = StringBuilder().appendln("本群违规词如下：")
        keywordJsonArray.forEach {
            sb.appendln(it)
        }
        return sb.removeSuffixLine().toString()
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
        val sb = StringBuilder().appendln("本群问答列表如下：")
        val qaJsonArray = qqGroupEntity.getQaJsonArray()
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            sb.appendln(jsonObject.getString("q"))
        }
        return sb.removeSuffixLine().toString()
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

    @Action("检查更新")
    fun checkUpdate(group: Long): String{
        val gitVersion = toolLogic.queryVersion()
        val sb = StringBuilder()
        sb.appendln("当前程序版本：$version")
        sb.appendln("最新程序版本：$gitVersion")
        if (gitVersion > version){
            sb.appendln("更新日志：https://github.com/kukume/kuku-bot/releases/tag/$gitVersion")
            sb.append("发现程序可更新，正在下载中！！！")
            yuq.sendMessage(mf.newGroup(group).plus(sb.toString()))
            val response = OkHttpClientUtils.get("https://u.iheit.com/kuku/bot/kukubot.jar")
            val bytes = OkHttpClientUtils.getBytes(response)
            IO.writeFile(File("${System.getProperty("user.dir")}${File.separator}kukubot.jar"), bytes)
            yuq.sendMessage(mf.newGroup(group).plus("更新完成，请前往控制台手动启动程序！！"))
            exitProcess(0)
        }else sb.append("暂未发现需要更新")
        return sb.toString()
    }

    @After
    fun finally(actionContext: BotActionContext) = BotUtils.addAt(actionContext)

    private fun boolToStr(b: Boolean?) = if (b == true) "开" else "关"
}
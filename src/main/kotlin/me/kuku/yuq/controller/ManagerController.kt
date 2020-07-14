package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.*
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Member
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

@GroupController
class ManagerController {
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var qqGroupService: QQGroupService

    @Before
    fun before(group: Long, qq: Long, actionContext: BotActionContext){
        var qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity == null){
            qqGroupEntity = QQGroupEntity(null, group)
            qqGroupService.save(qqGroupEntity)
        }
        actionContext.session["qqGroupEntity"] = qqGroupEntity
        if (qq != master.toLong()) throw "抱歉，您不是机器人主人，无法执行！！".toMessage()
    }

    @Action("机器人 {status}")
    fun switchGroup(qqGroupEntity: QQGroupEntity, status: Boolean): String?{
        qqGroupEntity.status = status
        qqGroupService.save(qqGroupEntity)
        return if (status) "机器人开启成功" else "机器人关闭成功"
    }

    @Action("鉴黄 {open}")
    fun pic(qqGroupEntity: QQGroupEntity, open: Boolean): String{
        qqGroupEntity.pic = open
        qqGroupService.save(qqGroupEntity)
        return if (open) "鉴黄已开启！！" else "鉴黄已关闭！！"
    }

    @Action("禁言 {member} {time}")
    fun shutUp(group: Long, member: Member, time: Int): String{
        yuq.groups[group]?.get(member.id)?.ban(time * 60)
        return "禁言成功！！"
    }

    @Action("t {member}")
    fun kick(member: Member, group: Long): String{
        yuq.groups[group]?.get(member.id)?.kick()
        return "踢出成功！！"
    }

    @Action("{act}违规词/{key}")
    fun addKey(key: String, act: String, qqGroupEntity: QQGroupEntity): String?{
        var keyword = qqGroupEntity.keyword
        val msg = when (act) {
            "加" -> {
                keyword = this.add(keyword, key)
                "加违规词成功！！"
            }
            "去" -> {
                keyword = this.del(keyword, key)
                "去违规词成功！！"
            }
            else -> null
        }
        return if (msg != null) {
            qqGroupEntity.keyword = keyword
            qqGroupService.save(qqGroupEntity)
            msg
        }else null
    }

    @Action("{act}黑 {member}")
    fun black(member: Member, act: String, qqGroupEntity: QQGroupEntity, group: Long): String?{
        var keyword = qqGroupEntity.blackList
        val msg = when (act) {
            "加" -> {
                keyword = this.add(keyword, member.id.toString())
                this.kick(member, group)
                "加黑名单成功！！"
            }
            "去" -> {
                keyword = this.del(keyword, member.id.toString())
                "去黑名单成功"
            }
            else -> null
        }
        return if (msg != null) {
            qqGroupEntity.blackList = keyword
            qqGroupService.save(qqGroupEntity)
            msg
        }else null
    }

    @Action("黑名单")
    fun blackList(qqGroupEntity: QQGroupEntity): String{
        val keyword = qqGroupEntity.blackList.split("|")
        val sb = StringBuilder("本群黑名单如下：\n")
        keyword.forEach {
            if (it != ""){
                sb.appendln(it)
            }
        }
        return sb.removeSuffix("\r\n").toString()
    }

    @Action("违规词")
    fun keywords(qqGroupEntity: QQGroupEntity): String{
        val keyword = qqGroupEntity.keyword.split("|")
        val sb = StringBuilder("本群违规词如下：\n")
        keyword.forEach {
            if (it != ""){
                sb.appendln(it)
            }
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

    @Action("点歌切换{type}")
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

    @Action("撤回通知 {s}")
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

    /**
     * 修改参数
     */
    private fun add(oldKey: String,  key: String) = "$oldKey$key|"

    private fun del(oldKey: String,  key: String): String{
        val list = oldKey.split("|")
        for (k in list){
            if (k == key){
                return oldKey.replace("$k|", "")
            }
        }
        return oldKey
    }
}
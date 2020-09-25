package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.firstString
import com.icecreamqaq.yuq.mirai.MiraiBot
import com.icecreamqaq.yuq.toMessage
import me.kuku.yuq.entity.ConfigEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.service.ConfigService
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.service.WeiboService
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

@PrivateController
class PrivateSettingController: QQController() {
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var web: OkHttpWebImpl
    @Inject
    private lateinit var miraiBot: MiraiBot
    @Inject
    private lateinit var configService: ConfigService
    @Inject
    private lateinit var weiboService: WeiboService

    @Before
    fun before(qq: Long, actionContext: BotActionContext){
        if (qq != master.toLong()) throw "您不是机器人主人，无法执行！！".toMessage()
        actionContext["qqEntity"] = BotUtils.toQQEntity(web, miraiBot)
    }

    @Action("群开启 {groupNo}")
    @Synonym(["群关闭 {groupNo}"])
    fun groupOpen(groupNo: Long, @PathVar(0) str: String): String{
        val groups = yuq.groups
        return if (groups.containsKey(groupNo)){
            val qqGroupEntity = qqGroupService.findByGroup(groupNo) ?: QQGroupEntity(null, groupNo)
            qqGroupEntity.status = str.contains("开启")
            qqGroupService.save(qqGroupEntity)
            "机器人开启或者关闭成功！！"
        }else "机器人并没有加入这个群！！"
    }

    @Action("同意入群 {groupNo}")
    fun agreeAddGroup(qqEntity: QQEntity, groupNo: Long): String{
        val groupMsgList = qqLogic.getGroupMsgList(qqEntity)
        var map: Map<String, String>? = null
        groupMsgList.forEach {
            if (it["group"] == groupNo.toString()) {
                map = it
                return@forEach
            }
        }
        return qqLogic.operatingGroupMsg(qqEntity, "agree", map ?: return "没有找到这个群号", null)
    }

    @Action("退群 {groupNo}")
    fun leaveGroup(groupNo: Long): String{
        val groups = yuq.groups
        return if (groups.containsKey(groupNo)){
            groups[groupNo]?.leave()
            "退出群聊成功！！"
        }else "机器人并没有加入这个群！！"
    }

    @Action("设置qqAI")
    fun settingQQAI(session: ContextSession): String{
        reply("将设置QQAI的信息，请确保您的应用赋予了图片鉴黄、智能闲聊、通用OCR能力")
        reply("请输入ai.qq.com/v1的appId")
        val firstMessage = session.waitNextMessage()
        val appId = firstMessage.firstString()
        reply("请输入ai.qq.com/v1的appKey")
        val secondMessage = session.waitNextMessage()
        val appKey = secondMessage.firstString()
        val jsonObject = JSONObject()
        jsonObject["appId"] = appId
        jsonObject["appKey"] = appKey
        val configEntity = configService.findByType("qqAI") ?: ConfigEntity(null, "qqAI")
        configEntity.content = jsonObject.toString()
        configService.save(configEntity)
        return "绑定qqAI的信息成功！！"
    }

    @Action("设置lolicon {apiKey}")
    fun settingLoLiCon(apiKey: String): String{
        val configEntity = configService.findByType("loLiCon") ?:
                ConfigEntity(null, "loLiCon")
        configEntity.content = apiKey
        configService.save(configEntity)
        return "绑定loLiCon的apiKey成功！！"
    }

    @Action("设置wb")
    fun settingWeibo(qq: Long): String{
        val weiboEntity = weiboService.findByQQ(qq) ?: return "您还没有绑定微博账号！！"
        val configEntity = configService.findByType("wb") ?: ConfigEntity(null, "wb")
        configEntity.content = weiboEntity.mobileCookie
        configService.save(configEntity)
        return "设置微博信息成功！！"
    }

}
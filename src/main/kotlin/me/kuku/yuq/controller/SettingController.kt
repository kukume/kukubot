@file:Suppress("unused")

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
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.mirai.MiraiBot
import me.kuku.yuq.entity.ConfigEntity
import me.kuku.yuq.entity.QQLoginEntity
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.service.ConfigService
import me.kuku.yuq.service.GroupService
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

@PrivateController
class SettingController: QQController() {
    @Inject
    private lateinit var groupService: GroupService
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

    @Before
    fun before(qq: Long, actionContext: BotActionContext){
        if (qq != master.toLong()) throw "您不是机器人主人，无法执行！！".toMessage().toThrowable()
        actionContext["qqEntity"] = BotUtils.toQQEntity(web, miraiBot)
    }

    @Action("群开启 {groupNo}")
    @Synonym(["群关闭 {groupNo}"])
    fun groupOpen(groupNo: Long, @PathVar(0) str: String): String{
        val groups = yuq.groups
        return if (groups.containsKey(groupNo)){
            val qqGroupEntity = groupService.findByGroup(groupNo) ?: GroupEntity(null, groupNo)
            qqGroupEntity.status = str.contains("开启")
            groupService.save(qqGroupEntity)
            "机器人开启或者关闭成功！！"
        }else "机器人并没有加入这个群！！"
    }

    @Action("同意入群 {groupNo}")
    fun agreeAddGroup(qqLoginEntity: QQLoginEntity, groupNo: Long): String{
        val groupMsgList = qqLogic.getGroupMsgList(qqLoginEntity)
        var map: Map<String, String>? = null
        groupMsgList.forEach {
            if (it["group"] == groupNo.toString()) {
                map = it
                return@forEach
            }
        }
        return qqLogic.operatingGroupMsg(qqLoginEntity, "agree", map ?: return "没有找到这个群号", null)
    }

    @Action("退群 {groupNo}")
    fun leaveGroup(groupNo: Long): String{
        val groups = yuq.groups
        return if (groups.containsKey(groupNo)){
            groups[groupNo]?.leave()
            "退出群聊成功！！"
        }else "机器人并没有加入这个群！！"
    }

    @Action("qqai")
    fun settingQQAI(session: ContextSession): String{
        reply("将设置QQAI的信息，请确保您的应用赋予了图片鉴黄、智能闲聊、通用OCR能力")
        reply("请输入ai.qq.com/v1的appId")
        val firstMessage = session.waitNextMessage()
        val appId = firstMessage.firstString()
        reply("请输入ai.qq.com/v1的appKey")
        val secondMessage = session.waitNextMessage()
        val appKey = secondMessage.firstString()
        val configEntity1 = configService.findByType("qqAIAppId") ?: ConfigEntity(null, "qqAIAppId")
        val configEntity2 = configService.findByType("qqAIAppKey") ?: ConfigEntity(null, "qqAIAppKey")
        configEntity1.content = appId
        configEntity2.content = appKey
        configService.save(configEntity1)
        configService.save(configEntity2)
        return "绑定qqAI的信息成功！！"
    }

    @Action("lolicon {apiKey}")
    fun settingLoLiCon(apiKey: String): String{
        val configEntity = configService.findByType("loLiCon") ?:
                ConfigEntity(null, "loLiCon")
        configEntity.content = apiKey
        configService.save(configEntity)
        return "绑定loLiCon的apiKey成功！！"
    }
}
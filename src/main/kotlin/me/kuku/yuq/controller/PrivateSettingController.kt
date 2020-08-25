package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.mirai.MiraiBot
import com.icecreamqaq.yuq.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

@PrivateController
class PrivateSettingController {
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
    fun agreeAddGroup(qqEntity: QQEntity, groupNo: Long) =
            qqLogic.operatingGroupMsg(qqEntity, "agree", groupNo, null)

    @Action("退群 {groupNo}")
    fun leaveGroup(groupNo: Long): String{
        val groups = yuq.groups
        return if (groups.containsKey(groupNo)){
            groups[groupNo]?.leave()
            "退出群聊成功！！"
        }else "机器人并没有加入这个群！！"
    }

}
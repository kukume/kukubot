package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.QQController
import me.kuku.yuq.logic.SuperCuteLogic
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.service.SuperCuteService
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

@GroupController
class SuperCuteController: QQController() {
    @Inject
    private lateinit var superCuteLogic: SuperCuteLogic
    @Inject
    private lateinit var superCuteService: SuperCuteService
    @Inject
    private lateinit var qqGroupService: QQGroupService

    @Before
    fun checkBind(qq: Long, actionContext: BotActionContext, group: Long){
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.superCute == true) {
            val superCuteEntity = superCuteService.findByQQ(qq)
            if (superCuteEntity == null) throw mif.at(qq).plus("没有绑定超级萌宠的token，请先私聊机器人发送[萌宠]进行绑定")
            else {
                val commonResult = superCuteLogic.getInfo(superCuteEntity.token)
                if (commonResult.code == 200) {
                    actionContext.session["map"] = commonResult.t!!
                } else throw mif.at(qq).plus("超级萌宠的Token已过期，请重新绑定！")
            }
        }else throw mif.at(qq).plus("超级萌宠功能已关闭！！")
    }

    @Action("萌宠签到")
    @QMsg(at = true)
    fun dailySign(map: Map<String, String>) = superCuteLogic.dailySign(map)

    @Action("萌宠元气")
    @QMsg(at = true, atNewLine = true)
    fun dailyVitality(map: Map<String, String>, qq: Long): String{
        reply(mif.at(qq).plus("正在为您领取元气中~~~请稍后~~~"))
        val str1 = superCuteLogic.dailyVitality(map)
        val str2 = superCuteLogic.moreVitality(map)
        return str1 + "\n" + str2
    }

    @Action("萌宠喂食")
    @QMsg(at = true)
    fun feeding(map: Map<String, String>, qq: Long): String{
        reply(mif.at(qq).plus("正在为您的宠物喂食~~~请稍后~~~"))
        return superCuteLogic.feeding(map)
    }

    @Action("萌宠任务")
    @QMsg(at = true)
    fun finishTask(map: Map<String, String>, qq: Long): String {
        reply(mif.at(qq).plus("正在为您完成任务中~~~请稍后~~~"))
        return superCuteLogic.finishTask(map)
    }

    @Action("萌宠金币")
    @QMsg(at = true)
    fun receiveCoin(map: Map<String, String>) = "您收取了${superCuteLogic.receiveCoin(map)}金币"

    @Action("萌宠掠夺")
    @QMsg(at = true)
    fun steal(map: Map<String, String>, qq: Long): String {
        reply(mif.at(qq).plus("正在为您偷取金币和抓捕奴隶中~~~请稍后~~~"))
        return superCuteLogic.steal(map)
    }

    @Action("萌宠找回")
    @QMsg(at = true)
    fun findCute(map: Map<String, String>) = superCuteLogic.findCute(map)

    @Action("萌宠抽奖")
    @QMsg(at = true)
    fun dailyLottery(map: Map<String, String>, qq: Long): String {
        reply(mif.at(qq).plus("正在为您抽奖中~~~请稍后~~~"))
        return superCuteLogic.dailyLottery(map)
    }

    @Action("萌宠信息")
    @QMsg(at = true, atNewLine = true)
    fun profile(map: Map<String, String>) = superCuteLogic.getProfile(map)

    @Action("萌宠一键")
    @QMsg(at = true, atNewLine = true)
    @Synchronized fun all(map: Map<String, String>, qq: Long): String{
        reply(mif.at(qq).plus("正在为您的萌宠完成任务~~~时间会很长~~~请稍后~~~"))
        val str1 = superCuteLogic.findCute(map)
        val str2 = superCuteLogic.dailySign(map)
        val str3 = superCuteLogic.dailyVitality(map)
        val str4 = superCuteLogic.moreVitality(map)
        val str5 = superCuteLogic.feeding(map)
        val str6 = superCuteLogic.steal(map)
        val str7 = superCuteLogic.dailyLottery(map)
        val str8 = superCuteLogic.finishTask(map)
        return StringBuilder()
                .appendln("萌宠找回：$str1")
                .appendln("萌宠签到：$str2")
                .appendln("萌宠元气1：$str3")
                .appendln("萌宠元气2：$str4")
                .appendln("萌宠喂食：$str5")
                .appendln("萌宠掠夺：$str6")
                .appendln("萌宠抽奖：$str7")
                .append("萌宠任务：$str8")
                .toString()
    }
}
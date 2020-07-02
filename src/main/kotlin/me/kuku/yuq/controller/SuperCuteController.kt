package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.message.MessageFactory
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.SuperCuteService
import javax.inject.Inject

@GroupController
class SuperCuteController {
    @Inject
    private lateinit var superCuteService: SuperCuteService
    @Inject
    private lateinit var daoService: DaoService
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var mf: MessageFactory

    @Before
    fun checkBind(qq: Long, actionContext: BotActionContext){
        val superCuteEntity = daoService.findSuperCuteByQQ(qq)
        if (superCuteEntity == null) throw mif.text("没有绑定超级萌宠的token").toMessage()
        else {
            val commonResult = superCuteService.getInfo(superCuteEntity.token)
            if (commonResult.code == 200){
                actionContext.session["map"] = commonResult.t
            }else throw mif.text("超级萌宠的Token已过期，请重新绑定！").toMessage()
        }
    }

    @Action("萌宠签到")
    fun dailySign(map: Map<String, String>) = superCuteService.dailySign(map)

    @Action("萌宠元气")
    fun dailyVitality(map: Map<String, String>, group: Long, qq: Long): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您领取元气中~~~请稍后~~~"))
        val str1 = superCuteService.dailyVitality(map)
        val str2 = superCuteService.moreVitality(map)
        return str1 + "\n" + str2
    }

    @Action("萌宠喂食")
    fun feeding(map: Map<String, String>, group: Long, qq: Long): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您的宠物喂食~~~请稍后~~~"))
        return superCuteService.feeding(map)
    }

    @Action("萌宠任务")
    fun finishTask(map: Map<String, String>, group: Long, qq: Long): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您完成任务中~~~请稍后~~~"))
        return superCuteService.finishTask(map)
    }

    @Action("萌宠金币")
    fun receiveCoin(map: Map<String, String>) = "您收取了${superCuteService.receiveCoin(map)}金币"

    @Action("萌宠掠夺")
    fun steal(map: Map<String, String>, group: Long, qq: Long): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您偷取金币和抓捕奴隶中~~~请稍后~~~"))
        return superCuteService.steal(map)
    }

    @Action("萌宠找回")
    fun findCute(map: Map<String, String>) = superCuteService.findCute(map)

    @Action("萌宠抽奖")
    fun dailyLottery(map: Map<String, String>, group: Long, qq: Long): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您抽奖中~~~请稍后~~~"))
        return superCuteService.dailyLottery(map)
    }
    @Action("萌宠信息")
    fun profile(map: Map<String, String>) = superCuteService.getProfile(map)

    @Action("萌宠一键")
    fun all(map: Map<String, String>, group: Long, qq: Long): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您的萌宠完成任务~~~时间会很长~~~请稍后~~~"))
        val str1 = superCuteService.findCute(map)
        val str2 = superCuteService.dailySign(map)
        val str3 = superCuteService.dailyVitality(map)
        val str4 = superCuteService.moreVitality(map)
        val str5 = superCuteService.feeding(map)
        val str6 = superCuteService.steal(map)
        val str7 = superCuteService.dailyLottery(map)
        val str8 = superCuteService.finishTask(map)
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
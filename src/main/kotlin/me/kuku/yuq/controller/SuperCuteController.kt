package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.message.MessageFactory
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.dao.SuperCuteDao
import me.kuku.yuq.logic.SuperCuteLogic
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

@GroupController
class SuperCuteController {
    @Inject
    private lateinit var superCuteLogic: SuperCuteLogic
    @Inject
    private lateinit var superCuteDao: SuperCuteDao
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var mf: MessageFactory

    @Before
    fun checkBind(qq: Long, actionContext: BotActionContext){
        val superCuteEntity = superCuteDao.findByQQ(qq)
        if (superCuteEntity == null) throw mif.at(qq).plus("没有绑定超级萌宠的token，请先私聊机器人发送[萌宠]进行绑定")
        else {
            val commonResult = superCuteLogic.getInfo(superCuteEntity.token)
            if (commonResult.code == 200){
                actionContext.session["map"] = commonResult.t
            }else throw mif.at(qq).plus("超级萌宠的Token已过期，请重新绑定！")
        }
    }

    @Action("萌宠签到")
    fun dailySign(map: Map<String, String>) = superCuteLogic.dailySign(map)

    @Action("萌宠元气")
    fun dailyVitality(map: Map<String, String>, group: Long, qq: Long): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您领取元气中~~~请稍后~~~"))
        val str1 = superCuteLogic.dailyVitality(map)
        val str2 = superCuteLogic.moreVitality(map)
        return str1 + "\n" + str2
    }

    @Action("萌宠喂食")
    fun feeding(map: Map<String, String>, group: Long, qq: Long): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您的宠物喂食~~~请稍后~~~"))
        return superCuteLogic.feeding(map)
    }

    @Action("萌宠任务")
    fun finishTask(map: Map<String, String>, group: Long, qq: Long): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您完成任务中~~~请稍后~~~"))
        return superCuteLogic.finishTask(map)
    }

    @Action("萌宠金币")
    fun receiveCoin(map: Map<String, String>) = "您收取了${superCuteLogic.receiveCoin(map)}金币"

    @Action("萌宠掠夺")
    fun steal(map: Map<String, String>, group: Long, qq: Long): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您偷取金币和抓捕奴隶中~~~请稍后~~~"))
        return superCuteLogic.steal(map)
    }

    @Action("萌宠找回")
    fun findCute(map: Map<String, String>) = superCuteLogic.findCute(map)

    @Action("萌宠抽奖")
    fun dailyLottery(map: Map<String, String>, group: Long, qq: Long): String {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您抽奖中~~~请稍后~~~"))
        return superCuteLogic.dailyLottery(map)
    }
    @Action("萌宠信息")
    fun profile(map: Map<String, String>) = superCuteLogic.getProfile(map)

    @Action("萌宠一键")
    fun all(map: Map<String, String>, group: Long, qq: Long): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("正在为您的萌宠完成任务~~~时间会很长~~~请稍后~~~"))
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

    @After
    fun finally(actionContext: BotActionContext) = BotUtils.addAt(actionContext)

}
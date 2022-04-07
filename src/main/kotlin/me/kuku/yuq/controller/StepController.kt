package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.StepEntity
import me.kuku.yuq.entity.StepService
import me.kuku.yuq.logic.LeXinStepLogic
import me.kuku.yuq.logic.XiaomiStepLogic
import javax.inject.Inject

@GroupController
@PrivateController
class StepController @Inject constructor(
    private val stepService: StepService
): QQController() {

    @Action("乐心运动登录")
    suspend fun leXinLogin(qqEntity: QqEntity, session: ContextSession): String {
        reply(mif.at(qqEntity.qq).plus("请发送手机号").toMessage())
        val phone = session.waitNextMessage().firstString()
        reply(mif.at(qqEntity.qq).plus("请发送密码").toMessage())
        val password = session.waitNextMessage().firstString()
        val result = LeXinStepLogic.login(phone, password)
        return if (result.isSuccess) {
            val newStepEntity = result.data
            val stepEntity = stepService.findByQqEntity(qqEntity) ?: StepEntity().also {
                it.qqEntity = qqEntity
            }
            stepEntity.leXinCookie = newStepEntity.leXinCookie
            stepEntity.leXinUserid = newStepEntity.leXinUserid
            stepEntity.leXinAccessToken = newStepEntity.leXinAccessToken
            stepService.save(stepEntity)
            "绑定乐心运动成功"
        } else result.message
    }

    @Action("小米运动登录")
    suspend fun miLogin(qqEntity: QqEntity, session: ContextSession): String {
        reply(mif.at(qqEntity.qq).plus("请发送手机号").toMessage())
        val phone = session.waitNextMessage().firstString()
        reply(mif.at(qqEntity.qq).plus("请发送密码").toMessage())
        val password = session.waitNextMessage().firstString()
        val result = XiaomiStepLogic.login(phone, password)
        return if (result.isSuccess) {
            val newEntity = result.data
            val stepEntity = stepService.findByQqEntity(qqEntity) ?: StepEntity().also {
                it.qqEntity = qqEntity
            }
            stepEntity.miLoginToken = newEntity.miLoginToken
            stepService.save(stepEntity)
            "绑定小米运动成功"
        } else result.message
    }

    @Before(except = ["leXinLogin", "miLogin"])
    fun before(qqEntity: QqEntity): StepEntity {
        return stepService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定步数，操作失败").toThrowable()
    }

    @Action("乐心运动步数")
    suspend fun leXinStep(stepEntity: StepEntity, qq: Long, session: ContextSession): String {
        reply(mif.at(qq).plus("请发送需要修改的步数").toMessage())
        val s = session.waitNextMessage().firstString().toIntOrNull() ?: return "发送的步数有误"
        val result = LeXinStepLogic.modifyStepCount(stepEntity, s)
        return if (result.isSuccess) "修改步数成功"
        else "修改步数失败，${result.message}"
    }

    @Action("小米运动步数")
    suspend fun miStep(stepEntity: StepEntity, qq: Long, session: ContextSession): String {
        reply(mif.at(qq).plus("请发送需要修改的步数").toMessage())
        val s = session.waitNextMessage().firstString().toIntOrNull() ?: return "发送的步数有误"
        val result = XiaomiStepLogic.modifyStepCount(stepEntity, s)
        return if (result.isSuccess) "修改步数成功"
        else "修改步数失败，${result.message}"
    }

    @Action("步数自动修改")
    fun modifyStep(stepEntity: StepEntity, session: ContextSession, qq: Long): String {
        reply(mif.at(qq).plus("请发送需要每日自动修改的步数").toMessage())
        val s = session.waitNextMessage().firstString().toIntOrNull() ?: return "发送的步数有误"
        stepEntity.config.step = s
        stepService.save(stepEntity)
        return "步数自动修改设置成功"
    }

}
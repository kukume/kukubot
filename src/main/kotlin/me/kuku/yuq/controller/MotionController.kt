package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.QQController
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.logic.LeXinMotionLogic
import me.kuku.yuq.logic.XiaomiMotionLogic
import me.kuku.yuq.service.MotionService
import javax.inject.Inject

@GroupController
class MotionController: QQController() {
    @Inject
    private lateinit var motionService: MotionService
    @Inject
    private lateinit var leXinMotionLogic: LeXinMotionLogic
    @Inject
    private lateinit var xiaomiMotionLogic: XiaomiMotionLogic

    @Before
    fun before(qq: Long): MotionEntity{
        val motionEntity = motionService.findByQQ(qq)
        if (motionEntity == null){
            throw mif.at(qq).plus("您还没有绑定账号，无法操作步数！！")
        }else return motionEntity
    }

    @Action("步数 {step}")
    @QMsg(at = true)
    fun steps(motionEntity: MotionEntity, step: Int): String {
        if (motionEntity.accessToken == "") return "您还没有绑定乐心运动账号，如需绑定请私聊机器人发送<lexin 账号 密码>"
        var result = leXinMotionLogic.modifyStepCount(step, motionEntity)
        if (!result.contains("成功")){
            val commonResult = leXinMotionLogic.loginByPassword(motionEntity.phone, motionEntity.password)
            val loginMotionEntity = commonResult.t ?: return commonResult.msg
            motionEntity.cookie = loginMotionEntity.cookie
            motionEntity.accessToken = loginMotionEntity.accessToken
            motionService.save(motionEntity)
            result = leXinMotionLogic.modifyStepCount(step, motionEntity)
        }
        return result;
    }

    @Action("步数任务")
    @QMsg(at = true)
    fun stepTask(motionEntity: MotionEntity, @PathVar(value = 1, type = PathVar.Type.Integer) stepParam: Int?): String {
        val step = stepParam ?: 0
        motionEntity.step = step
        motionService.save(motionEntity)
        return "步数定时任务设置为${step}成功！！"
    }

    @Action("删除步数")
    @QMsg(at = true)
    fun del(qq: Long): String {
        motionService.delByQQ(qq)
        return "删除步数成功！！"
    }

    @Action("mi步数 {step}")
    @QMsg(at = true)
    fun xiaomiMotion(motionEntity: MotionEntity, step: Int): String {
        if (motionEntity.miLoginToken == "")
            return "您还没绑定小米账号，如需绑定请私聊机器人发送<mi 账号 密码>"
        var loginToken = motionEntity.miLoginToken
        var result = xiaomiMotionLogic.changeStep(loginToken, step)
        if (result.contains("登录已失效")) {
            val loginResult = xiaomiMotionLogic.login(motionEntity.miPhone, motionEntity.miPassword)
            loginToken = loginResult.t ?: return loginResult.msg
            motionEntity.miLoginToken = loginToken
            motionService.save(motionEntity)
            result = xiaomiMotionLogic.changeStep(loginToken, step)
        }
        return result
    }
}
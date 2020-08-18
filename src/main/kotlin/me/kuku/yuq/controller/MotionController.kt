package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.icecreamqaq.yuq.*
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.logic.LeXinMotionLogic
import me.kuku.yuq.logic.QQAILogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.service.MotionService
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

@GroupController
class MotionController: QQController() {
    @Inject
    private lateinit var motionService: MotionService
    @Inject
    private lateinit var leXinMotionLogic: LeXinMotionLogic
    @Inject
    private lateinit var qqAiLogic: QQAILogic

    @Action("步数")
    fun steps(qq: Long, session: ContextSession): String{
        val time = 30L * 1000
        val motionEntity = motionService.findByQQ(qq)
        val id = motionEntity?.id
        var step: Int? = null
        if (motionEntity != null){
            reply(mif.at(qq).plus("请输入需要修改的步数！！"))
            val stepMessage = session.waitNextMessage(1000 * 30)
            step = stepMessage.firstString().toInt()
            val msg =  leXinMotionLogic.modifyStepCount(step, motionEntity)
            if ("成功" in msg) return msg
            reply(mif.at(qq).plus("您的cookie已失效，将准备重新登录！！"))
        }
        val phone: String
        if (motionEntity == null || motionEntity.phone == "") {
            reply(mif.at(qq).plus("您未绑定手机号，请输入手机号进行绑定！！"))
            val phoneMessage = session.waitNextMessage(time)
            phoneMessage.recall()
            phone = phoneMessage.firstString()
            if (phone.length != 11) return "手机号格式不正确！！"
        }else phone = motionEntity.phone
        val commonResult = this.identifyImageCode(phone, qq)
        reply(mif.at(qq).plus(commonResult.t ?: return commonResult.msg))
        var newMotionEntity: MotionEntity? = null
        do {
            val codeMessage = session.waitNextMessage(1000 * 60 * 2)
            if (codeMessage.firstString().length != 6) return "您的验证码不符合规范，可能您不想登录了，已退出步数上下文！！"
            val loginCommonResult = leXinMotionLogic.loginByPhoneCaptcha(phone, codeMessage.firstString())
            when (loginCommonResult.code){
                412 -> {
                    reply(mif.at(qq).plus("验证码错误，请重新输入！！"))
                }
                200 -> {
                    newMotionEntity = loginCommonResult.t!!
                    newMotionEntity.id = id
                    newMotionEntity.phone = phone
                    newMotionEntity.qq = qq
                    motionService.save(newMotionEntity)
                }
                else -> return loginCommonResult.msg
            }
        }while (loginCommonResult.code == 412)
        if (step == null) {
            reply(mif.at(qq).plus("请输入需要修改的步数！！"))
            val stepMessage = session.waitNextMessage(1000 * 30)
            step = stepMessage.firstString().toInt()
        }
        return leXinMotionLogic.modifyStepCount(step, newMotionEntity!!)
    }

    private fun identifyImageCode(phone: String, qq: Long): CommonResult<String> {
        do {
            val commonResult: CommonResult<String>
            val captchaImage = leXinMotionLogic.getCaptchaImage(phone)
            val codeCommonResult = qqAiLogic.generalOCRToCaptcha(captchaImage)
            val code = codeCommonResult.t ?: return CommonResult(500, "", "OCR错误，${codeCommonResult.msg}")
            commonResult = leXinMotionLogic.getCaptchaCode(phone, code)
            when (commonResult.code) {
                200 -> return CommonResult(200, "", "验证码发送成功，请输入验证码！！")
                416 -> return CommonResult(500, "验证码已失效")
                412 -> reply(mif.at(qq).plus("验证码错误，正在为您重新识别中！！"))
                else -> return CommonResult(500, commonResult.msg)
            }
        } while (commonResult.code == 412)
        return CommonResult(500, "未知原因，请稍后再试！！")
    }

    @Action("步数任务")
    fun stepTask(qq: Long, session: ContextSession): String {
        val motionEntity = motionService.findByQQ(qq) ?: return "您还没有绑定lexin运动账号！如需要绑定，请发送<步数>"
        reply(mif.at(qq).plus("请输入需要定时修改的步数！！"))
        val taskMessage = session.waitNextMessage(30 * 1000)
        val step = taskMessage.firstString()
        motionEntity.step = step.toInt()
        motionService.save(motionEntity)
        return "添加修改步数定时任务成功！！"
    }

    @Action("删除步数")
    fun del(qq: Long): String{
        motionService.delByQQ(qq)
        return "删除步数成功！！"
    }

    @After
    fun finally(actionContext: BotActionContext) = BotUtils.addAt(actionContext)
}
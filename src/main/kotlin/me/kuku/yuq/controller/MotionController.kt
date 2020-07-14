package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.NextActionContext
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.MessageFactory
import com.icecreamqaq.yuq.message.MessageItemFactory
import com.icecreamqaq.yuq.toMessage
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.logic.LeXinMotionLogic
import me.kuku.yuq.service.MotionService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.QQAIUtils
import javax.inject.Inject

@GroupController
@ContextController
class MotionController {
    @Inject
    private lateinit var motionService: MotionService
    @Inject
    private lateinit var leXinMotionLogic: LeXinMotionLogic
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var mf: MessageFactory

    @Action("步数")
    fun login(qq: Long, actionContext: BotActionContext){
        val motionEntity = motionService.findByQQ(qq)
        if (motionEntity == null || motionEntity.phone == ""){
            throw NextActionContext("bindPhone")
        }else {
            actionContext.session["motionEntity"] = motionEntity
            throw NextActionContext("step")
        }
    }

    @Action("bindPhone")
    @ContextTip("检测到您未绑定手机号，请输入手机号进行绑定！！")
    fun bindPhone(@Save @PathVar(0) phone: String, message: Message, group: Long, qq: Long, actionContext: BotActionContext): Any {
        message.recall()
        if (phone.length == 11){
            return this.identifyImageCode(phone, group, qq, actionContext)
        }else{
            throw mif.text("手机号格式不正确").toMessage()
        }
    }

    private fun identifyImageCode(phone: String, group: Long, qq: Long, actionContext: BotActionContext){
        actionContext.session["phone"] = phone
        do {
            val commonResult: CommonResult<String>
            val captchaImage = leXinMotionLogic.getCaptchaImage(phone)
            val codeCommonResult = QQAIUtils.generalOCRToMotion(captchaImage)
            if (codeCommonResult.code == 200) {
                commonResult = leXinMotionLogic.getCaptchaCode(phone, codeCommonResult.t)
                when (commonResult.code) {
                    200 -> {
                        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("验证码发送成功，请输入验证码！！"))
                        throw NextActionContext("identifyCode")
                    }
                    416 -> throw mif.text("验证码已失效").toMessage()
                    412 -> yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("验证码错误，正在为您重新识别中！！"))
                    else -> throw mif.at(qq).plus(commonResult.msg)
                }
            } else throw mif.at(qq).plus(codeCommonResult.msg + "，请稍后再试！")
        } while (commonResult.code == 412)
        throw "gg了，请稍后再试".toMessage()
    }

    @Action("identifyCode")
    @NextContext("step")
    fun identifyCode(@PathVar(0) code: String, phone: String, qq: Long, motionEntity: MotionEntity?, actionContext: BotActionContext){
        val commonResult = leXinMotionLogic.loginByPhoneCaptcha(phone, code)
        if (commonResult.code == 200){
            val map = commonResult.t
            val motion = MotionEntity(null, qq, phone, map.getValue("cookie"), map.getValue("userId"), map.getValue("accessToken"))
            if (motionEntity != null) motion.id = motionEntity.id
            motionService.save(motion)
            actionContext.session["motionEntity"] = motion
        }else throw mif.text(commonResult.msg).toMessage()
    }

    @Action("step")
    @ContextTip("请输入需要修改的步数")
    fun step(motionEntity: MotionEntity?, qq: Long, @PathVar(0) step: String, actionContext: BotActionContext, group: Long): String?{
        val motion = motionEntity ?: motionService.findByQQ(qq)
        val msg = leXinMotionLogic.modifyStepCount(step.toInt(), motion!!)
        return if ("成功" in msg) msg
        else {
            yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("您的cookie已失效，将准备重新登录！！"))
            this.identifyImageCode(motionEntity!!.phone, group, qq, actionContext)
            null
        }
    }

    @Action("步数任务")
    @NextContext("nextStepTask")
    fun stepTask(qq: Long, actionContext: BotActionContext) {
        val motionEntity = motionService.findByQQ(qq)
        if (motionEntity != null) {
            actionContext.session["motionEntity"] = motionEntity
        }else throw mif.text("您还没有绑定乐心运动账号！如需要绑定，请发送<步数>").toMessage()
    }

    @Action("nextStepTask")
    @ContextTip("请输入需要每天定时修改的步数")
    fun nextStepTask(@PathVar(0) step: String, motionEntity: MotionEntity): String {
        motionEntity.step = step.toInt()
        motionService.save(motionEntity)
        return "添加修改步数定时任务成功！！"
    }

    @After
    fun finally(actionContext: BotActionContext) = BotUtils.addAt(actionContext)
}
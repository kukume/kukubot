package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.NextActionContext
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.MessageFactory
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.LeXinMotionService
import me.kuku.yuq.utils.image
import javax.inject.Inject

@GroupController
@ContextController
class MotionController {
    @Inject
    private lateinit var daoService: DaoService
    @Inject
    private lateinit var motionService: LeXinMotionService
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var mf: MessageFactory

    @Action("步数")
    fun login(qq: Long, actionContext: BotActionContext){
        val motionEntity = daoService.findMotionByQQ(qq)
        if (motionEntity == null){
            throw NextActionContext("bindPhone")
        }else {
            actionContext.session["motionEntity"] = motionEntity
            throw NextActionContext("step")
        }
    }

    @Action("bindPhone")
    @ContextTip("检测到您未绑定手机号，请输入手机号进行绑定")
    @NextContext("identifyImage")
    fun bindPhone(@Save @PathVar(0) phone: String, message: Message): Message{
        message.recall()
        return if (phone.length == 11){
            val captchaImage = motionService.getCaptchaImage(phone)
            mif.image(captchaImage).plus("请输入图片验证码，如需更换验证码")
        }else{
            throw mif.text("手机号格式不正确").toMessage()
        }
    }

    @Action("identifyImage")
    @NextContext("identifyCode")
    fun identifyImage(@PathVar(0) imageCode: String, phone: String): String{
        val captchaCode = motionService.getCaptchaCode(phone, imageCode)
        return when (captchaCode.code) {
            200 -> {
                "验证码发送成功，请输入验证码"
            }
            416 -> throw mif.text("验证码已失效。").toMessage()
            else -> throw mif.text(captchaCode.msg).toMessage()
        }
    }

    @Action("identifyCode")
    @NextContext("step")
    fun identifyCode(@PathVar(0) code: String, phone: String, qq: Long, motionEntity: MotionEntity?){
        val commonResult = motionService.loginByPhoneCaptcha(phone, code)
        if (commonResult.code == 200){
            val map = commonResult.t
            val motion = MotionEntity(null, qq, phone, map.getValue("cookie"), map.getValue("userId"), map.getValue("accessToken"))
            if (motionEntity != null) motion.id = motionEntity.id
            daoService.saveOrUpdateMotion(motion)
        }else throw mif.text(commonResult.msg).toMessage()
    }

    @Action("step")
    @ContextTip("请输入需要修改的步数")
    fun step(motionEntity: MotionEntity?, qq: Long, @PathVar(0) step: String, actionContext: BotActionContext, group: Long): String{
        val motion = motionEntity ?: daoService.findMotionByQQ(qq)
        val msg = motionService.modifyStepCount(step.toInt(), motion!!)
        return if ("成功" in msg) msg
        else {
            actionContext.session["step"] = step
            actionContext.session["motionEntity"] = motion
            actionContext.session["phone"] = motion.phone
            val captchaImage = motionService.getCaptchaImage(motion.phone)
            yuq.sendMessage(mf.newGroup(group).plus(mif.image(captchaImage)).plus("检测到您的cookie已失效，请输入图片验证码重新绑定"))
            throw NextActionContext("identifyImage")
        }
    }

    @Action("步数任务")
    @NextContext("nextStepTask")
    fun stepTask(qq: Long, actionContext: BotActionContext) {
        val motionEntity = daoService.findMotionByQQ(qq)
        if (motionEntity != null) {
            actionContext.session["motionEntity"] = motionEntity
        }else throw mif.text("您还没有绑定乐心运动账号！如需要绑定，请发送<步数>").toMessage()
    }

    @Action("nextStepTask")
    @ContextTip("请输入需要每天定时修改的步数")
    fun nextStepTask(@PathVar(0) step: String, motionEntity: MotionEntity): String {
        motionEntity.step = step.toInt()
        daoService.saveOrUpdateMotion(motionEntity)
        return "添加修改步数定时任务成功！！"
    }
}
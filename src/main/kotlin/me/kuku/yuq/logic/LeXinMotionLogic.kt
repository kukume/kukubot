package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface LeXinMotionLogic {

    fun getCaptchaImage(phone: String): ByteArray

    fun getCaptchaCode(phone: String, captchaImageCode: String): CommonResult<String>

    fun loginByPassword(phone: String, password: String): CommonResult<MotionEntity>

    fun loginByPhoneCaptcha(phone: String, captchaPhoneCode: String): CommonResult<MotionEntity>

    fun loginByQQ(qqEntity: QQEntity): CommonResult<MotionEntity>

    fun modifyStepCount(step: Int, motionEntity: MotionEntity): String

}
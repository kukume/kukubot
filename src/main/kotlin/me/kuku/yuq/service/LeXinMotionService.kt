package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface LeXinMotionService {

    fun getCaptchaImage(phone: String): ByteArray

    fun getCaptchaCode(phone: String, captchaImageCode: String): CommonResult<String>

    fun loginByPhoneCaptcha(phone: String, captchaPhoneCode: String): CommonResult<Map<String, String>>

    fun modifyStepCount(step: Int, motionEntity: MotionEntity): String

}
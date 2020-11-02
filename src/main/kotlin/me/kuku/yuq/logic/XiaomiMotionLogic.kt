package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface XiaomiMotionLogic {
    fun login(phone: String, password: String): CommonResult<String>

    fun changeStep(token: String, step: Int): String
}
package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import me.kuku.yuq.entity.KuGouService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.KuGouLogic
import javax.inject.Inject

@GroupController
class KuGouController @Inject constructor(
    private val kuGouLogic: KuGouLogic
) {

    @Action("酷狗登陆")
    fun kuGouLogin(group: Group, session: ContextSession): String {
        group.sendMessage("清输入手机号")
        val phone = session.waitNextMessage().firstString().toLongOrNull() ?: return "输入的手机号有误"
//        val kuGouEntity = kuGouService.findByQqEntity(qqEntity)
        val mid = kuGouLogic.mid()
        val result = kuGouLogic.sendMobileCode(phone.toString(), mid)
        if (result.isSuccess) {
            group.sendMessage("请输入短信验证码")
            val code = session.waitNextMessage().firstString()
            val verifyResult = kuGouLogic.verifyCode(phone.toString(), code, mid)
            println(verifyResult)
        }
        return "HEH"
    }

}
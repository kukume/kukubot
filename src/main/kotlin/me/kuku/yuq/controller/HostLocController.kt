package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.HostLocEntity
import me.kuku.yuq.entity.HostLocService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.HostLocLogic
import javax.inject.Inject

@GroupController
class HostLocController @Inject constructor(
    private val hostLocService: HostLocService
){

    @Action("loc登录")
    fun locLogin(qqEntity: QqEntity, qq: Contact, session: ContextSession): String {
        qq.sendMessage("请输入账号")
        val account = session.waitNextMessage().firstString()
        qq.sendMessage("请输入密码")
        val password = session.waitNextMessage().firstString()
        val res = HostLocLogic.login(account, password)
        return if (res.isSuccess) {
            val cookie = res.data
            val hostLocEntity = hostLocService.findByQqEntity(qqEntity) ?: HostLocEntity().also {
                it.qqEntity = qqEntity
            }
            hostLocEntity.cookie = cookie
            hostLocService.save(hostLocEntity)
            "绑定HostLoc成功"
        } else res.message
    }

    @Action("loc签到")
    fun locSign(qqEntity: QqEntity, qq: Long): String {
        val hostLocEntity = hostLocService.findByQqEntity(qqEntity) ?: throw mif.at(qq).plus("你未绑定HostLoc，签到失败").toThrowable()
        val cookie = hostLocEntity.cookie
        HostLocLogic.sign(cookie)
        return "loc签到成功"
    }

}
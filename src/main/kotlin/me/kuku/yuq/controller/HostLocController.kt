package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import me.kuku.yuq.entity.HostLocEntity
import me.kuku.yuq.entity.HostLocService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.HostLocLogic
import javax.inject.Inject

@GroupController
class HostLocController @Inject constructor(
    private val hostLocService: HostLocService
): QQController() {

    @Action("loc登录")
    fun locLogin(qqEntity: QqEntity, session: ContextSession): String {
        reply(mif.at(qqEntity.qq).plus("请发送账号").toMessage())
        val account = session.waitNextMessage().firstString()
        reply(mif.at(qqEntity.qq).plus("请发送密码").toMessage())
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

    @Before(except = ["locLogin"])
    fun before(qqEntity: QqEntity, qq: Long): HostLocEntity {
        return hostLocService.findByQqEntity(qqEntity) ?: throw mif.at(qq).plus("你未绑定HostLoc，签到失败").toThrowable()
    }

    @Action("loc签到")
    fun locSign(hostLocEntity: HostLocEntity): String {
        val cookie = hostLocEntity.cookie
        HostLocLogic.sign(cookie)
        return "loc签到成功"
    }

    @Action("loc推送 {status}")
    fun push(status: Boolean, hostLocEntity: HostLocEntity): String {
        hostLocEntity.config.push = status.toStatus()
        hostLocService.save(hostLocEntity)
        return "loc推送${if (status) "开启" else "关闭"}成功"
    }

}
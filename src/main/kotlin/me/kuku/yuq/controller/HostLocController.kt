package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.HostLocEntity
import me.kuku.yuq.entity.HostLocService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.HostLocLogic
import me.kuku.yuq.utils.openOrClose
import org.springframework.stereotype.Component

@GroupController
@PrivateController
@Component
class HostLocController (
    private val hostLocService: HostLocService
) {

    @Action("loc登录")
    suspend fun locLogin(qqEntity: QqEntity, session: ContextSession, context: BotActionContext): String {
        context.source.sendMessage(mif.at(qqEntity.qq).plus("请发送账号").toMessage())
        val account = session.waitNextMessage().firstString()
        context.source.sendMessage(mif.at(qqEntity.qq).plus("请发送密码").toMessage())
        val password = session.waitNextMessage().firstString()
        val res = HostLocLogic.login(account, password)
        return if (res.success()) {
            val cookie = res.data()
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

    @Action("loc自动签到 {status}")
    fun locAutoSign(hostLocEntity: HostLocEntity, status: Boolean): String {
        hostLocEntity.config.sign = status.toStatus()
        hostLocService.save(hostLocEntity)
        return "loc自动签到${status.openOrClose()}成功"
    }

}
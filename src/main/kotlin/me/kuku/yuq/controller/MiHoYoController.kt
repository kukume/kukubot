package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.MiHoYoEntity
import me.kuku.yuq.entity.MiHoYoService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.MiHoYoLogic
import org.springframework.stereotype.Component

@GroupController
@PrivateController
@Component
class MiHoYoController (
    private val miHoYoService: MiHoYoService
) {

    @Action("米哈游登录")
    suspend fun login(session: ContextSession, qqEntity: QqEntity, context: BotActionContext): String {
        context.source.sendMessage("请发送手机号")
        val phone = session.waitNextMessage().firstString()
        context.source.sendMessage("请发送密码")
        val password = session.waitNextMessage().firstString()
        val result = MiHoYoLogic.login(phone, password)
        return if (result.success()) {
            val miHoYoEntity = result.data()
            val newEntity = miHoYoService.findByQqEntity(qqEntity) ?: MiHoYoEntity().also {
                it.qqEntity = qqEntity
            }
            newEntity.cookie = miHoYoEntity.cookie
            miHoYoService.save(newEntity)
            "绑定米哈游账号成功"
        } else result.message
    }

    @Before(except = ["login"])
    fun before(qqEntity: QqEntity): MiHoYoEntity {
        return miHoYoService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定米哈游账号，操作失败").toThrowable()
    }

    @Action("原神签到")
    suspend fun sign(miHoYoEntity: MiHoYoEntity): String {
        val result = MiHoYoLogic.sign(miHoYoEntity)
        return if (result.success()) "原神签到成功"
        else "原神签到失败，${result.message}"
    }

    @Action("原神自动签到 {status}")
    fun open(status: Boolean, miHoYoEntity: MiHoYoEntity): String {
        miHoYoEntity.config.sign = status.toStatus()
        miHoYoService.save(miHoYoEntity)
        return "原神自动签到${if (status) "开启" else "关闭"}成功"
    }



}
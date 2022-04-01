package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import me.kuku.yuq.entity.MiHoYoEntity
import me.kuku.yuq.entity.MiHoYoService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.MiHoYoLogic
import javax.inject.Inject

@GroupController
class MiHoYoController @Inject constructor(
    private val miHoYoService: MiHoYoService
): QQController() {

    @Action("米哈游登录")
    fun login(session: ContextSession, qqEntity: QqEntity): String {
        reply("请发送手机号")
        val phone = session.waitNextMessage().firstString()
        reply("请发送密码")
        val password = session.waitNextMessage().firstString()
        val result = MiHoYoLogic.login(phone, password)
        return if (result.isSuccess) {
            val miHoYoEntity = result.data
            val newEntity = miHoYoService.findByQqEntity(qqEntity) ?: MiHoYoEntity().also {
                it.qqEntity = qqEntity
            }
            newEntity.cookie = miHoYoEntity.cookie
            miHoYoService.save(newEntity)
            "绑定米哈游账号成功"
        } else result.message
    }

    @Before
    fun before(qqEntity: QqEntity): MiHoYoEntity {
        return miHoYoService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定米哈游账号，操作失败").toThrowable()
    }

    @Action("原神签到")
    fun sign(miHoYoEntity: MiHoYoEntity): String {
        val result = MiHoYoLogic.sign(miHoYoEntity)
        return if (result.isSuccess) "原神签到成功"
        else "原神签到失败，${result.message}"
    }

    @Action("原神自动签到 {status}")
    fun open(status: Boolean, miHoYoEntity: MiHoYoEntity): String {
        miHoYoEntity.config.sign = status.toStatus()
        miHoYoService.save(miHoYoEntity)
        return "原神自动签到${if (status) "开启" else "关闭"}成功"
    }



}
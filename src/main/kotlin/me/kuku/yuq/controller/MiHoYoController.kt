package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
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

}
package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import me.kuku.yuq.entity.NetEaseEntity
import me.kuku.yuq.entity.NetEaseService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.NetEaseLogic
import javax.inject.Inject

@GroupController
class NetEaseController @Inject constructor(
    private val netEaseService: NetEaseService
) {

    @Action("网易登录")
    fun login(qqEntity: QqEntity, qq: Contact, session: ContextSession): String {
        qq.sendMessage("请输入手机号")
        val phone = session.waitNextMessage().firstString()
        qq.sendMessage("请输入密码")
        val password = session.waitNextMessage().firstString()
        val result = NetEaseLogic.login(phone, password)
        return if (result.isSuccess) {
            val netEaseEntity = result.data
            val newEntity = netEaseService.findByQqEntity(qqEntity) ?: NetEaseEntity().also {
                it.qqEntity = qqEntity
            }
            newEntity.csrf = netEaseEntity.csrf
            newEntity.musicU = netEaseEntity.musicU
            netEaseService.save(newEntity)
            "绑定网易云音乐成功"
        } else result.message
    }

}
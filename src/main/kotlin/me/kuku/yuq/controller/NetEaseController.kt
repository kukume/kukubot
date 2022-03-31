package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
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

    @Before(except = ["login"])
    fun before(qqEntity: QqEntity): NetEaseEntity {
        val netEaseEntity = netEaseService.findByQqEntity(qqEntity)
        return netEaseEntity ?: throw mif.at(qqEntity.qq).plus("您没有绑定网易云音乐账号，操作失败").toThrowable()
    }

    @Action("网易签到")
    fun sign(netEaseEntity: NetEaseEntity): String {
        val result = NetEaseLogic.sign(netEaseEntity)
        return if (result.isSuccess) "网易云音乐签到成功"
        else "网易云音乐签到失败，${result.message}"
    }

    @Action("网易听歌")
    fun listenMusic(netEaseEntity: NetEaseEntity): String {
        val result = NetEaseLogic.listenMusic(netEaseEntity)
        return if (result.isSuccess) "网易云音乐听歌成功"
        else "网易云音乐听歌失败，${result.message}"
    }

    @Action("网易音乐人签到")
    fun musicianSign(netEaseEntity: NetEaseEntity) {

    }
}
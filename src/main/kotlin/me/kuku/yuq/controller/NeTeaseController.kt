@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.NeTeaseEntity
import me.kuku.yuq.logic.NeTeaseLogic
import me.kuku.yuq.service.NeTeaseService
import javax.inject.Inject

@GroupController
class NeTeaseController {

    @Inject
    private lateinit var neTeaseLogic: NeTeaseLogic
    @Inject
    private lateinit var neTeaseService: NeTeaseService

    @Before
    fun before(qq: Long): NeTeaseEntity{
        val neTeaseEntity = neTeaseService.findByQQ(qq)
        if (neTeaseEntity == null) throw mif.at(qq).plus("您还未绑定网易账号！如需绑定，请私聊机器人发送网易，或者群聊发送网易（确保您的网易账号绑定了您的QQ）！").toThrowable()
        else return neTeaseEntity
    }

    @Action("网易加速")
    @QMsg(at = true)
    fun listeningVolume(neTeaseEntity: NeTeaseEntity): String{
        val signResult = neTeaseLogic.sign(neTeaseEntity)
        val listeningVolume = neTeaseLogic.listeningVolume(neTeaseEntity)
        return "网易音乐签到：$signResult\n听歌量：$listeningVolume"
    }
}

@PrivateController
class BindNeTeaseController: QQController(){
    @Inject
    private lateinit var neTeaseLogic: NeTeaseLogic
    @Inject
    private lateinit var neTeaseService: NeTeaseService

    @Action("网易")
    fun bindNeTease(qq: Long, session: ContextSession): String{
        reply("请输入网易云音乐账号！！")
        val accountMessage = session.waitNextMessage(30 * 1000)
        val account = accountMessage.firstString()
        reply("请输入网易云音乐密码，密码必须为32位md5，不可以传入明文")
        reply("md5在线加密网站：https://md5jiami.51240.com/，请使用32位小写！！")
        val pwdMessage = session.waitNextMessage(60 * 1000 * 2)
        val password = pwdMessage.firstString()
        val commonResult = neTeaseLogic.loginByPhone(account, password)
        val neTeaseEntity = neTeaseService.findByQQ(qq) ?: NeTeaseEntity(null, qq)
        val newNeTeaseEntity = commonResult.t ?: return "绑定失败！！${commonResult.msg}"
        newNeTeaseEntity.id = neTeaseEntity.id
        newNeTeaseEntity.qq = qq
        neTeaseService.save(newNeTeaseEntity)
        return "绑定成功！！"
    }
}
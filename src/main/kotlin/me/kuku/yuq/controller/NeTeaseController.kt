package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
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
        if (neTeaseEntity == null) throw mif.at(qq).plus("您还未绑定网易账号！如需绑定，请私聊机器人发送网易，或者群聊发送网易（确保您的网易账号绑定了您的QQ）！")
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
package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.QqService
import javax.inject.Inject

@GroupController
class QqController {

    @Inject
    private lateinit var qqService: QqService

    @Action("loc监控 {status}")
    @QMsg(at = true)
    fun locMonitor(status: Boolean, qqEntity: QqEntity): String{
        qqEntity.locMonitor = status
        qqService.save(qqEntity)
        return "loc个人监${if (status) "开启" else "关闭"}成功"
    }

}
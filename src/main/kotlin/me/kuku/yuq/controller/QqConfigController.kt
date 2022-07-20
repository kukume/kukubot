package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.PrivateController
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.QqService
import org.springframework.stereotype.Component

@PrivateController
@Component
class QqConfigController(
    private val qqService: QqService
) {

    @Action("妖火推送 {status}")
    fun yaoHuoPush(status: Boolean, qqEntity: QqEntity): String {
        qqEntity.config.yaoHuoPush = status.toStatus()
        qqService.save(qqEntity)
        return "妖火推送${if (status) "开启" else "关闭"}成功"
    }

}
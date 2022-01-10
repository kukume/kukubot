package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.mif
import me.kuku.utils.OkHttpUtils

@GroupController
class ToolController {

    @Action("摸鱼日历")
    fun fishermanCalendar(group: Group) {
        val bytes = OkHttpUtils.getBytes("https://api.kukuqaq.com/tool/fishermanCalendar?preview")
        group.sendMessage(mif.imageByByteArray(bytes))
    }


}
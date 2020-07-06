package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.message.At
import com.icecreamqaq.yuq.message.Message
import javax.inject.Inject

@GroupController
class ManagerController {

    @Inject
    private lateinit var yuq: YuQ

    @Before
    fun before(group: Long, qq: Long){
        if (!yuq.groups[group]?.get(qq)?.isAdmin()!!){
            throw yuq.messageItemFactory.text("权限不足！！！").toMessage()
        }
    }


    @Action("禁言")
    fun shutUp(group: Long, message: Message): String{
        val body = message.body
        val at = body.getOrNull(1)
        return if (at is At){
            val time = body.getOrNull(2)?.toPath()?.trim()?.toIntOrNull()
            yuq.groups[group]?.get(at.user)?.ban(if (time != null) time * 60 else 0)
            "禁言成功！！"
        }else "没有发现qq！！"
    }

    @Action("t")
    fun kick(message: Message, group: Long): String{
        val body = message.body
        val at = body.getOrNull(1)
        return if (at is At){
            yuq.groups[group]?.get(at.user)?.kick()
            "踢出成功！！"
        }else "没有发现qq！！"
    }


}
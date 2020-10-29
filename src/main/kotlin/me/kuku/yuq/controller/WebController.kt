@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.YuWeb.annotation.WebController
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.yuq

@WebController
class WebController {

    @Action("groupMessage")
    fun groupMessage(group: String, msg: String): String {
        val g =  yuq.groups[group.toLong()] ?: return "Error: Group Not Found!"
        g.sendMessage(msg.toMessage())
        return "OK!"
    }

    @Action("privateMessage")
    fun privateMessage(qq: String, msg: String): String {
        val f = yuq.friends[qq.toLong()] ?: return "Error: Friend Not Found!"
        f.sendMessage(msg.toMessage())
        return "OK!"
    }

    @Action("tempMessage")
    fun tempMessage(qq: String, group: String, msg: String): String {
        val g = yuq.groups[group.toLong()] ?: return "Error: Group Not Found!"
        val m = g.members[qq.toLong()] ?: return "Error: Member Not Found!"
        m.sendMessage(msg.toMessage())
        return "OK!"
    }
}

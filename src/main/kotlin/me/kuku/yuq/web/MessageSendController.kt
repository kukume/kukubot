@file:Suppress("unused")

package me.kuku.yuq.web

import com.icecreamqaq.yuq.yuq
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import me.kuku.pojo.CommonResult
import org.springframework.stereotype.Component

@Component
class MessageSendController {

    init {
        println("ss")
    }

    fun Routing.messageSend() {

        get("send") {
            val queryParameters = call.request.queryParameters
            val msg = queryParameters.getOrFail("msg")
            val group = queryParameters["group"]
            val qq = queryParameters["qq"]
            if (group != null && qq == null) yuq.groups[group]?.sendMessage(msg)
            else if (qq != null && group == null) yuq.friends[qq]?.sendMessage(msg)
            else if (group != null && qq != null) yuq.groups[group]?.get(qq.toLong())?.sendMessage(msg)
            else call.respond(CommonResult.failure("参数不正确", null))
            call.respond(CommonResult.success(null, "ok"))
        }

    }


}
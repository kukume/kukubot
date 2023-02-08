package me.kuku.mirai.controller

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.kuku.ktor.plugins.getOrFail
import me.kuku.ktor.plugins.receiveJsonNode
import net.mamoe.mirai.Bot
import org.springframework.stereotype.Component

@Component
class PushController(
    private val bot: Bot
) {

    fun Routing.push() {

        route("push") {

            post("qq") {
                val jsonNode = call.receiveJsonNode()
                val qq = jsonNode.getOrFail("qq").asLong()
                val text = jsonNode.getOrFail("message").asText()
                bot.friends.getOrFail(qq).sendMessage(text)
                call.respond(mapOf("qq" to qq, "message" to text))
            }

            post("group") {
                val jsonNode = call.receiveJsonNode()
                val group = jsonNode.getOrFail("group").asLong()
                val text = jsonNode.getOrFail("message").asText()
                bot.getGroupOrFail(group).sendMessage(text)
                call.respond(mapOf("group" to group, "message" to text))
            }

        }


    }

}

package me.kuku.mirai.controller

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.kuku.mirai.utils.MiraiUtils
import org.springframework.stereotype.Component

@Component
class AuthController {

    fun Routing.auth() {

        route("auth") {
            get {
                val domain = call.request.queryParameters["domain"]
                val auth = MiraiUtils.auth()
                if (domain != null)
                    auth.psKey = auth.psKey.filter { it.key == domain }.toMutableMap()
                call.respond(auth)
            }
        }

    }

}

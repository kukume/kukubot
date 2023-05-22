package me.kuku.mirai.controller

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import me.kuku.mirai.utils.MiraiUtils
import org.springframework.stereotype.Component

@Component
class AuthController {

    fun Routing.auth() {

        route("auth") {
            get {
                val domain = call.request.queryParameters.getOrFail("domain")
                val auth = MiraiUtils.auth(domain)
                call.respond(auth)
            }
        }

    }

}

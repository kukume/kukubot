package me.kuku.mirai.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.contains
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.kuku.utils.client
import me.kuku.utils.setFormDataContent
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.StandardCharImageLoginSolver
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.info

class KuKuLoginSolver: LoginSolver() {

    private val standardCharImageLoginSolver = StandardCharImageLoginSolver()

    override val isSliderCaptchaSupported: Boolean
        get() = true

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray) =
        standardCharImageLoginSolver.onSolvePicCaptcha(bot, data)

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        val logger = bot.logger
        logger.info { "[SliderCaptcha] Captcha link: $url" }
        for (i in 0..3) {
            logger.info{ "[SliderCaptcha] Try to automatically recognize the verification code." }
            val jsonNode = client.post("https://api.kukuqaq.com/captcha") {
                setFormDataContent {
                    append("url", url)
                }
            }.body<JsonNode>()
            if (jsonNode.contains("ticket")) {
                val ticket = jsonNode["ticket"].asText()
                logger.info { "[SliderCaptcha] Ticket: $ticket" }
                return ticket
            }
        }
        logger.error { "[SliderCaptcha] Recognition failure" }
        return standardCharImageLoginSolver.onSolveSliderCaptcha(bot, url)
    }



}

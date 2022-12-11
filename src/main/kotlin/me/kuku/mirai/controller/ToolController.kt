package me.kuku.mirai.controller

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.kuku.mirai.utils.GroupMessageSubscribers
import me.kuku.utils.client
import me.kuku.utils.toUrlEncode
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class ToolController {

    suspend fun GroupMessageSubscribers.tool() {
        startsWith("chat") {
            val jsonNode = client.get("https://api.kukuqaq.com/chat?text=${it.trim().toUrlEncode()}").body<JsonNode>()
            subject.sendMessage(message.quote() + jsonNode["choices"][0]["text"].asText().trimStart())
        }

        "色图" reply {
            val jsonNode = client.get("https://api.kukuqaq.com/lolicon/random").body<JsonNode>()
            val quickUrl = jsonNode[0]["quickUrl"].asText()
            client.get(quickUrl).body<InputStream>().toExternalResource().use { er ->
                subject.uploadImage(er)
            }
        }

    }

}

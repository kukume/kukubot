package me.kuku.mirai.subscribe

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.kuku.mirai.utils.GroupMessageSubscribe
import me.kuku.utils.client
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class ToolSubscribe {

    suspend fun GroupMessageSubscribe.tool() {

        "色图" reply {
            val jsonNode = client.get("https://api.kukuqaq.com/lolicon/random").body<JsonNode>()
            val quickUrl = jsonNode[0]["quickUrl"].asText()
            client.get(quickUrl).body<InputStream>().toExternalResource().use { er ->
                subject.uploadImage(er)
            }
        }
    }

}

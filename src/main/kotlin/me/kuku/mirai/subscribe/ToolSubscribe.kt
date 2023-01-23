package me.kuku.mirai.subscribe

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.kuku.mirai.utils.firstArg
import me.kuku.utils.client
import me.kuku.utils.setFormDataContent
import me.kuku.utils.toUrlEncode
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class ToolSubscribe {

    suspend fun GroupMessageSubscribersBuilder.tool() {
        startsWith("chat ") {
            val jsonNode = client.post("https://api.jpa.cc/chat"){
                setFormDataContent {
                    append("text", it.trim().toUrlEncode())
                }
            }.body<JsonNode>()
            subject.sendMessage(message.quote() + jsonNode["choices"][0]["text"].asText().trimStart())
        }

        "色图" reply {
            val jsonNode = client.get("https://api.kukuqaq.com/lolicon/random").body<JsonNode>()
            val quickUrl = jsonNode[0]["quickUrl"].asText()
            client.get(quickUrl).body<InputStream>().toExternalResource().use { er ->
                subject.uploadImage(er)
            }
        }

        Regex("oracle \\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$").matching {
            val email = firstArg<PlainText>().content
            val jsonNode = client.get("https://api.kukuqaq.com/oracle/promotion?email=$email").body<JsonNode>()
            val msg = if (jsonNode["items"].size() != 0) "有资格啦" else "没有资格哦"
            subject.sendMessage(message.quote() + msg)
        }

    }

}

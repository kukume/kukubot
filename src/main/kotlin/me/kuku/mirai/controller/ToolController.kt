package me.kuku.mirai.controller

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.kuku.mirai.utils.firstArg
import me.kuku.utils.client
import me.kuku.utils.toUrlEncode
import net.mamoe.mirai.event.GroupMessageSubscribersBuilder
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class ToolController {

    suspend fun GroupMessageSubscribersBuilder.tool() {
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

        Regex("oracle \\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$").matching {
            val email = firstArg<PlainText>().content
            val jsonNode = client.get("https://api.kukuqaq.com/oracle/promotion?email=$email").body<JsonNode>()
            val msg = if (jsonNode["items"].size() != 0) "有资格啦" else "没有资格哦"
            subject.sendMessage(message.quote() + msg)
        }

        "oracle event" {
            val jsonNode = client.get("https://api.kukuqaq.com/oracle/event").body<JsonNode>()
            val sb = StringBuilder("###################\n")
            for (node in jsonNode) {
                sb.appendLine("title:${node["title"].asText()}")
                sb.appendLine("people:${node["people"].asText()}")
                sb.appendLine("url:${node["url"].asText()}")
                sb.appendLine("id:${node["id"].asText()}")
                sb.appendLine("time:${node["time"].asText()}")
                sb.appendLine("###################")
            }
            subject.sendMessage(sb.toString())
        }

        Regex("oracle event register \\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)* [0-9]*\$").matching {
            val arr = it.split(" ")
            val email = arr[3]
            val id = arr[4].toInt()
            client.get("https://api.kukuqaq.com/oracle/event/register?id=$id&email=$email").body<JsonNode>()
            subject.sendMessage(message.quote() + "直播注册成功（以收到邮件为准）")
        }

    }

}

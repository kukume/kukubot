package me.kuku.mirai.subscribe

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import me.kuku.mirai.logic.ToolLogic
import me.kuku.mirai.logic.YgoLogic
import me.kuku.mirai.utils.*
import me.kuku.utils.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.Objects

@Component
class ToolSubscribe {

    private val chatCache = mutableMapOf<Long, String>()

    suspend fun GroupMessageSubscribe.tool() {

        "色图" reply {
            val jsonNode = client.get("https://api.kukuqaq.com/lolicon/random").body<JsonNode>()
            val quickUrl = jsonNode[0]["quickUrl"].asText()
            client.get(quickUrl).body<InputStream>().toExternalResource().use { er ->
                subject.uploadImage(er)
            }
        }

        regex("ygo \\S*\$") atReply {
            val cardList = YgoLogic.search(firstArg<PlainText>().content)
            val sb = StringBuilder("请发送查询卡片选项\n")
            cardList.forEachIndexed { index, card -> sb.appendLine("${index + 1}、${card.chineseName}") }
            sb.removeSuffix("\n")
            subject.sendMessage(atNewLine() + sb.toString())
            val nextMessage = nextMessage(30000) {
                val ii = this.message.contentToString().toIntOrNull()
                Objects.nonNull(ii) && ii!! <= cardList.size && ii > 0
            }
            val index = nextMessage.content.toInt()
            val card = cardList[index - 1]
            val text =
                "中文名：${card.chineseName}\n日文名：${card.japaneseName}\n英文名：${card.englishName}\n效果：\n${card.effect}"
            client.get(card.imageUrl).body<InputStream>().toExternalResource().use {
                subject.uploadImage(it) + text
            }
        }

        "tts" reply {
            subject.sendMessage(at() + "请发送生成的日语语音文字")
            val text = nextMessage().contentToString()
            val jsonNode = OkHttpKtUtils.postJson("https://innnky-vits-nyaru.hf.space/api/queue/push/", OkUtils.json("""
                {"fn_index":0,"data":["$text"],"action":"predict","session_hash":""}
            """.trimIndent()))
            val hash = jsonNode["hash"].asText()
            withTimeout(1000 * 20) {
                while (true) {
                    delay(1000)
                    val statusJsonNode = OkHttpKtUtils.postJson("https://innnky-vits-nyaru.hf.space/api/queue/status/",
                        OkUtils.json("""{"hash":"$hash"}"""))
                    if (statusJsonNode["status"].asText() == "QUEUED") continue
                    val data = statusJsonNode["data"]["data"] ?: continue
                    val status = data[0].asText()
                    if (status != "Success") error(status)
                    val base = data[1].asText().substring(22)
                    val sendMessage = base.base64Decode().inputStream().toExternalResource().use {
                        subject.uploadAudio(it)
                    }
                    subject.sendMessage(sendMessage)
                    break
                }
            }
        }

        "摸鱼日历" reply {
            val jsonNode = client.get("https://api.kukuqaq.com/fishermanCalendar").body<JsonNode>()
            val url = jsonNode["url"].asText()
            client.get(url).body<InputStream>().toExternalResource().use {
                subject.uploadImage(it)
            }
        }

        regex("百科 \\S*\$") atReply {
            ToolLogic.baiKe(firstArg<PlainText>().content)
        }

        regex("chat .*") quoteReply  {
            val text = message.contentToString().substring(5)
            if (text == "clear") return@quoteReply null
            val qq = sender.id
            var msg = if (chatCache.containsKey(qq)) {
                val mm = chatCache[qq]
                "$mm\nHuman: $text\nAI:"
            } else "Human: $text\nAI:"
            msg = msg.replace("\n", "\\n").replace("\"", "”")
            val jsonNode = client.post("https://api.kukuqaq.com/chat") {
                setFormDataContent {
                    append("text", msg)
                }
            }.body<JsonNode>()
            if (jsonNode.has("error")) {
                val message = jsonNode["error"]["message"].asText()
                if (message.contains("This model's maximum context length is 4097 tokens")) {
                    chatCache.remove(qq)
                }
                message
            } else {
                val result = jsonNode["choices"][0]["text"].asText()
                val newMsg = "$msg$result"
                chatCache[qq] = newMsg
                result.trim()
            }
        }

        "chat clear" atReply {
            chatCache.remove(sender.id)
            "清除缓存成功"
        }
    }

}

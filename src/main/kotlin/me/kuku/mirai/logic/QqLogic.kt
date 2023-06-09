package me.kuku.mirai.logic

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.kuku.mirai.utils.MiraiUtils
import me.kuku.utils.*
import java.util.function.Consumer

object QqLogic {

    private fun entity(url: String): QqPojo {
        val domain = MyUtils.regexOrFail("\\w*.qq.com", url)
        val auth = MiraiUtils.auth(domain)
        val qq = auth.qq
        val sKey = auth.sKey
        val psKey = auth.psKey
        val pojo = QqPojo()
        pojo.cookie = "uin=o${qq}; skey=${sKey}; p_uin=o${qq}; p_skey=${psKey};"
        return pojo
    }

    suspend fun groupEssence(group: Long, page: Int = 0, size: Int = 20): List<QqGroupEssenceMessage> {
        val entity = entity("qun.qq.com")
        val gtk = entity.getSKeyGtk()
        val jsonNode = client.get("https://qun.qq.com/cgi-bin/group_digest/digest_list?bkn=$gtk&bkn=$gtk&group_code=$group&page_start=$page&page_limit=$size") {
            headers {
                cookieString(entity.cookie)
            }
        }.body<JsonNode>()
        if (jsonNode["retcode"].asInt() != 0) error(jsonNode["retmsg"].asText())
        return jsonNode["data"]["msg_list"]?.convertValue() ?: listOf()
    }


}

class QqGroupEssenceMessage {
    @JsonProperty("group_code")
    var group: Long = 0
    @JsonProperty("msg_seq")
    var msgSeq: Int = 0
    @JsonProperty("msg_random")
    var msgRandom: Long = 0
    @JsonProperty("sender_uin")
    var senderUin: Long = 0
    @JsonProperty("sender_nick")
    var senderNick: String = ""
    @JsonProperty("sender_time")
    var senderTime: Long = 0
    @JsonProperty("add_digest_uin")
    var addUin: Long = 0
    @JsonProperty("add_digest_nick")
    var addNick: String = ""
    @JsonProperty("add_digest_time")
    var addTime: Long = 0
    @JsonProperty("msg_content")
    var msgContent: MutableList<MsgContent> = mutableListOf()

    fun str(): String {
        val sb = StringBuilder()
        for (msg in msgContent) {
            if (msg.msgType == 1) sb.append(msg.text)
            else if (msg.msgType == 3) sb.append("[图片]")
        }
        return sb.toString()
    }

    fun text(): String {
        val sb = StringBuilder()
        for (msg in msgContent) {
            if (msg.msgType == 1) sb.append("${msg.text} ")
        }
        return sb.toString()
    }

    class MsgContent {
        @JsonProperty("msg_type")
        var msgType: Int = 0
        var text: String = ""
        @JsonProperty("image_url")
        var imageUrl: String = ""
    }

}

class QqPojo {
    var cookie: String = ""
        set(value) {
            val sKey = MyUtils.regex("skey=", ";", value) ?: error("cookie无效")
            this.sKey = sKey
            val psKey = MyUtils.regex("p_skey=", ";", value) ?: error("cookie无效")
            this.psKey = psKey
            field = value
        }
    var domain: String = ""
    var sKey: String = ""
    var psKey: String = ""

    private fun getGtk(key: String): Long {
        val len = key.length
        var hash = 5381L
        for (i in 0 until len) {
            hash += (hash shl 5 and 2147483647) + key[i].code and 2147483647
            hash = hash and 2147483647
        }
        return hash and 2147483647
    }

    private fun getGtk2(key: String): String {
        var salt: Long = 5381
        val md5key = "tencentQQVIP123443safde&!%^%1282"
        val hash: MutableList<Long> = ArrayList()
        hash.add(salt shl 5)
        val len = key.length
        for (i in 0 until len) {
            val ascCode = Integer.toHexString(key[i].code)
            val code = Integer.valueOf(ascCode, 16).toLong()
            hash.add((salt shl 5) + code)
            salt = code
        }
        val sb = StringBuilder()
        hash.forEach(Consumer { obj: Long? -> sb.append(obj) })
        return (sb.toString() + md5key).md5()
    }

    fun getSKeyGtk() = getGtk(sKey)
    fun getSKeyGtk2() = getGtk2(sKey)
    fun getPsKeyGtk() = getGtk(psKey)
    fun getPsKeyGtk2() = getGtk2(psKey)

}

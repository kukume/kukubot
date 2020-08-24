package me.kuku.yuq.utils

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.*
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.toMessage
import java.net.URLEncoder
import kotlin.random.Random

object BotUtils {

    fun shortUrl(url: String): String{
        val newUrl = if (url.startsWith("http")) url
        else "http://$url"
        val response = OkHttpClientUtils.get("https://uxy.me/api.php?url=${URLEncoder.encode(newUrl, "utf-8")}")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val shortUrl = jsonObject.getString("shorturl")
        return shortUrl ?: "生成失败！！！"
    }

    fun randomStr(len: Int): String{
        val str = "abcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder()
        for (i in (0 until len))
            sb.append(str[Random.nextInt(0, str.length)])
        return sb.toString()
    }

    fun randomNum(len: Int): String{
        val sb = StringBuilder()
        for (i in (0 until len)){
            sb.append(Random.nextInt(10))
        }
        return sb.toString()
    }

    fun regex(regex: String, text: String): String? {
        val r = Regex(regex)
        val find = r.find(text)
        return find?.value
    }

    fun regex(first: String, last: String , text: String): String? {
        val regex = "(?<=$first).*?(?=$last)"
        return this.regex(regex, text)
    }

    fun addAt(actionContext: BotActionContext){
        actionContext.reMessage!!.at=true
    }

    fun getGroupId(qq: Contact): Long{
        return if (qq is Member) qq.group.id
        else 0L
    }

    fun messageToJsonArray(rm: Message): JSONArray{
        val body = rm.body
        val aJsonArray = JSONArray()
        for (messageItem in body){
            val aJsonObject = JSONObject()
            when (messageItem) {
                is Text -> {
                    aJsonObject["type"] = "text"
                    aJsonObject["content"] = messageItem.text
                }
                is Image -> {
                    aJsonObject["type"] = "image"
                    aJsonObject["content"] = messageItem.url
                }
                is Face -> {
                    aJsonObject["type"] = "face"
                    aJsonObject["content"] = messageItem.faceId
                }
                is At -> {
                    aJsonObject["type"] = "at"
                    aJsonObject["content"] = messageItem.user
                }
            }
            if (aJsonObject.size != 0)
                aJsonArray.add(aJsonObject)
        }
        return aJsonArray
    }

    fun jsonArrayToMessage(jsonArray: JSONArray): Message{
        val msg = "".toMessage()
        for (j in jsonArray.indices){
            val aJsonObject = jsonArray.getJSONObject(j)
            when (aJsonObject.getString("type")){
                "text" -> msg.plus(aJsonObject.getString("content"))
                "image" -> msg.plus(mif.image(aJsonObject.getString("content")))
                "face" -> msg.plus(mif.face(aJsonObject.getInteger("content")))
                "at" -> msg.plus(mif.at(aJsonObject.getLong("content")))
            }
        }
        return msg
    }

    fun delMonitorList(jsonArray: JSONArray, username: String): List<JSONObject>{
        val list = mutableListOf<JSONObject>()
        jsonArray.forEach {
            val jsonObject = it as JSONObject
            if (jsonObject.getString("name") == username) list.add(jsonObject)
        }
        return list
    }
}
package me.kuku.yuq.utils

import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.*
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.mirai.MiraiBot
import com.icecreamqaq.yuq.toMessage
import me.kuku.yuq.entity.QQEntity
import java.net.URLEncoder
import kotlin.random.Random

object BotUtils {

    fun shortUrl(url: String): String{
        val newUrl = if (url.startsWith("http")) url
        else "http://$url"
        return if (url.contains("iheit.com") || url.contains("kuku.me")) {
            val response = OkHttpClientUtils.get("https://uxy.me/api.php?url=${URLEncoder.encode(newUrl, "utf-8")}")
            val jsonObject = OkHttpClientUtils.getJson(response)
            val shortUrl = jsonObject.getString("shorturl")
            shortUrl ?: "生成失败！！！"
        }else {
            val response = OkHttpClientUtils.get("https://api.kuku.me/tool/shorturl?url=${URLEncoder.encode(newUrl, "utf-8")}")
            OkHttpClientUtils.getStr(response)
        }
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
                is XmlEx -> {
                    aJsonObject["type"] = "xml"
                    aJsonObject["content"] = messageItem.value
                    aJsonObject["serviceId"] = messageItem.serviceId
                }
                is JsonEx -> {
                    aJsonObject["type"] = "json"
                    aJsonObject["content"] = messageItem.value
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
                "image" -> msg.plus(mif.imageByUrl(aJsonObject.getString("content")))
                "face" -> msg.plus(mif.face(aJsonObject.getInteger("content")))
                "at" -> msg.plus(mif.at(aJsonObject.getLong("content")))
                "xml" -> msg.plus(mif.xmlEx(aJsonObject.getInteger("serviceId"), aJsonObject.getString("content")))
                "json" -> msg.plus(mif.jsonEx(aJsonObject.getString("content")))
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

    fun toQQEntity(web: OkHttpWebImpl, miraiBot: MiraiBot): QQEntity{
        val concurrentHashMap = web.domainMap
        val qunMap = concurrentHashMap.getValue("qun.qq.com")
        val groupPsKey = qunMap.getValue("p_skey").value
        val qqMap = concurrentHashMap.getValue("qq.com")
        val sKey = qqMap.getValue("skey").value
        val qq = Regex("[1-9][0-9]*").find(qqMap.getValue("uin").value)?.value!!
        val qZoneMap = concurrentHashMap.getValue("qzone.qq.com")
        val psKey = qZoneMap.getValue("p_skey").value
        return QQEntity(null, qq.toLong(), 0L, "", sKey, psKey, groupPsKey, miraiBot.superKey, QQUtils.getToken(miraiBot.superKey).toString())
    }

    fun delAuto(jsonArray: JSONArray, username: String): JSONArray{
        val delList = mutableListOf<JSONObject>()
        for (i in jsonArray.indices){
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getString("name") == username) delList.add(jsonObject)
        }
        delList.forEach { jsonArray.remove(it) }
        return jsonArray
    }
}
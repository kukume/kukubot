package me.kuku.yuq.utils

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.Face
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Text
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.toMessage
import kotlin.random.Random

object BotUtils {

    fun shortUrl(url: String): String{
        if (!url.contains("qq.com") && !url.contains("iheit.com") && !url.contains("baidu.com")) return "不支持的url"
        val newUrl = if (url.startsWith("http")) url
        else "http://$url"
        val response = OkHttpClientUtils.post("https://s.iheit.com/api.php", OkHttpClientUtils.addForms(
                "url", newUrl
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200) jsonObject.getString("shorturl")
        else "生成失败！！！"
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
            }
        }
        return msg
    }
}
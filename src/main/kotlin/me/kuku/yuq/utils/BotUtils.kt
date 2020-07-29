package me.kuku.yuq.utils

import com.icecreamqaq.yuq.controller.BotActionContext
import kotlin.random.Random

object BotUtils {

    fun shortUrl(url: String): String{
        if (!url.contains("qq.com") && !url.contains("iheit.com") && !url.contains("baidu.com")) return "不支持的url"
        val newUrl = if (url.startsWith("http")) url
        else "http://$url"
        val response = OkHttpClientUtils.post("https://s.iheit.com/api.php", OkHttpClientUtils.addForms(
                "d", newUrl
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
}
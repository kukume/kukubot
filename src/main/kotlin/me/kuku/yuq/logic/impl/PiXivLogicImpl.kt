package me.kuku.yuq.logic.impl

import com.alibaba.fastjson.JSON
import me.kuku.yuq.logic.PiXivLogic
import me.kuku.yuq.utils.OkHttpClientUtils
import kotlin.random.Random

class PiXivLogicImpl: PiXivLogic {

    override fun getImage(url: String): ByteArray {
        val response = OkHttpClientUtils.get(url, OkHttpClientUtils.addReferer("https://www.pixiv.net/artworks"))
        return OkHttpClientUtils.getBytes(response)
    }

    private fun getUrlById(id: String): String{
        val response = OkHttpClientUtils.get("https://www.pixiv.net/artworks/$id")
        val html = OkHttpClientUtils.getStr(response)
        val jsonStr = Regex("(?<=id=\"meta-preload-data\" content=').*?(?='>)").find(html)?.value
        val jsonObject = JSON.parseObject(jsonStr)
        val urlJsonObject = jsonObject.getJSONObject("illust").getJSONObject(id).getJSONObject("urls")
        return urlJsonObject.getString("regular")
    }

    override fun searchTag(tag: String): String {
        val randomNum = Random.nextInt(1, 5)
        val response = OkHttpClientUtils.get("https://www.pixiv.net/ajax/search/illustrations/$tag?word=$tag&order=date_d&mode=r18&p=$randomNum&s_mode=s_tag&type=illust_and_ugoira&lang=zh")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val jsonArray = jsonObject.getJSONObject("body").getJSONObject("illust").getJSONArray("data")
        val singleJsonObject = jsonArray.getJSONObject(Random.nextInt(jsonArray.size))
        val id =  singleJsonObject.getString("illustId")
        return this.getUrlById(id)
    }

    override fun bookMarks(id: String, cookie: String): String {
        val cookieHeader = OkHttpClientUtils.addCookie("PHPSESSID=$cookie;")
        val response = OkHttpClientUtils.get("https://www.pixiv.net/ajax/user/$id/illusts/bookmarks?tag=&offset=0&limit=48&rest=show&lang=zh",
                cookieHeader)
        val jsonObject = OkHttpClientUtils.getJson(response)
        val count = jsonObject.getJSONObject("body").getInteger("total")
        val page = Random.nextInt(count / 48)
        val secondResponse = OkHttpClientUtils.get("https://www.pixiv.net/ajax/user/$id/illusts/bookmarks?tag=&offset=${page * 48}&limit=48&rest=show&lang=zh",
                cookieHeader)
        val secondJsonObject = OkHttpClientUtils.getJson(secondResponse)
        val jsonArray = secondJsonObject.getJSONObject("body").getJSONArray("works")
        val singleJsonObject = jsonArray.getJSONObject(Random.nextInt(jsonArray.size))
        val picId = singleJsonObject.getString("id")
        return this.getUrlById(picId)
    }
}
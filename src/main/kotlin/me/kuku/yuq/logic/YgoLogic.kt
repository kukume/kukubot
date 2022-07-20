package me.kuku.yuq.logic

import me.kuku.utils.OkHttpKtUtils
import me.kuku.utils.toUrlEncode
import org.jsoup.Jsoup
import org.springframework.stereotype.Service

@Service
class YgoLogic {

    suspend fun search(name: String): List<Card> {
        val str = OkHttpKtUtils.getStr("https://ygocdb.com/?search=${name.toUrlEncode()}")
        val elements = Jsoup.parse(str).select(".card")
        val list = mutableListOf<Card>()
        for (element in elements) {
            val spans = element.select("span")
            val chineseName = spans[0].text()
            val japaneseName = spans[1].text()
            val englishName = spans[2].text()
            val cardPassword = spans[3].text()
            val a = element.select(".cardimg a").first()!!
            val url = "https://ygocdb.com" + a.attr("href")
            val imgUrl = a.select("img").first()!!.attr("data-original").replace("!half", "")
            val desc = element.select(".desc").first()!!
            val nameHtml = desc.select(".name").toString()
            val effect = desc.removeClass("name").html().replace(nameHtml, "").replace("<hr>", "\n").replace("<br>", "\n").replace("\n\n", "\n")
            list.add(Card(chineseName, japaneseName, englishName, cardPassword, effect, url, imgUrl))
        }
        return list
    }


}

data class Card(
    val chineseName: String,
    val japaneseName: String,
    val englishName: String,
    val cardPassword: String,
    val effect: String,
    val url: String,
    val imageUrl: String
)
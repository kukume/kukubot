@file:Suppress("SpellCheckingInspection")

package me.kuku.yuq.logic

import me.kuku.pojo.CommonResult
import me.kuku.utils.*
import me.kuku.yuq.config.SauceNaoConfig
import me.kuku.yuq.config.VerificationFailureException
import me.kuku.yuq.utils.YuqUtils
import org.jsoup.Jsoup
import org.springframework.stereotype.Service

@Service
class ToolLogic(
    private val sauceNao: SauceNaoConfig
) {

    private suspend fun baiKeByUrl(url: String): CommonResult<String> {
        var response = OkHttpKtUtils.get(url)
        while (response.code == 302) {
            response.close()
            val location = response.header("location")!!
            if (location.startsWith("//baike.baidu.com/search/none")) return CommonResult.failure("")
            val resultUrl = if (location.startsWith("//")) "https:$location"
            else "https://baike.baidu.com$location"
            response = OkHttpKtUtils.get(resultUrl)
        }
        val html = OkUtils.str(response)
        val doc = Jsoup.parse(html)
        val result = doc.select(".lemma-summary .para").first()?.text()
            ?: return CommonResult.failure(code = 210, message = "", data = "https://baike.baidu.com" + doc.select("li[class=list-dot list-dot-paddingleft]").first()?.getElementsByTag("a")?.first()?.attr("href"))
        return CommonResult.success(result)
    }

    suspend fun baiKe(text: String): String {
        val encodeText = text.toUrlEncode()
        val url = "https://baike.baidu.com/search/word?word=$encodeText"
        val result = baiKeByUrl(url)
        return if (result.success())
            """
                ${result.data}
                查看详情： ${YuqUtils.shortUrl(url)}
            """.trimIndent()
        else if (result.code == 210) {
            val resultUrl = result.data()
            """
                ${baiKeByUrl(resultUrl).data}
                查看详情：${YuqUtils.shortUrl(resultUrl)}
            """.trimIndent()
        } else """
            抱歉，没有找到与"$text"相关的百科结果
        """.trimIndent()
    }

    suspend fun saucenao(url: String): List<SaucenaoResult> {
        val urlJsonObject = OkHttpKtUtils.getJson("https://saucenao.com/search.php?output_type=2&numres=16&url=${url.toUrlEncode()}&api_key=${sauceNao.key}")
        if (urlJsonObject.get("header").getInteger("status") != 0) throw VerificationFailureException(urlJsonObject.get("header").getString("message"))
        val jsonList = urlJsonObject.get("results")
        val list = mutableListOf<SaucenaoResult>()
        for (jsonObject in jsonList) {
            val header = jsonObject.get("header")
            val data = jsonObject.get("data")
            val similarity = header.getString("similarity")
            val thumbnail = header.getString("thumbnail")
            val indexName = header.getString("index_name")
            val extUrls = data.get("ext_urls")?.let {
                val letList = mutableListOf<String>()
                it.forEach { k -> letList.add(k.asText()) }
                letList
            } ?: listOf()
            val author = data.get("creator_name")?.asText() ?: data.get("member_name")?.asText() ?: data.get("author_name")?.asText() ?: ""
            val title = data.get("title")?.asText() ?: data.get("jp_name")?.asText() ?: ""
            val authorUrl = data.get("author_url")?.asText() ?: ""
            list.add(SaucenaoResult(similarity, thumbnail, indexName, extUrls, title, author, authorUrl).also {
                it.daId = data.getLong("da_id")
                it.pixivId = data.getLong("pixiv_id")
                it.faId = data.getLong("fa_id")
                it.tweetId = data.getLong("tweet_id")
            })
        }
        return list
    }

}

data class SaucenaoResult(
    val similarity: String, val thumbnail: String, val indexName: String, val extUrls: List<String>, val title: String, val author: String, val authUrl: String,
    var daId: Long? = null, var pixivId: Long? = null, var faId: Long? = null, var tweetId: Long? = null
)
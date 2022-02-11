package me.kuku.yuq.logic

import me.kuku.utils.OkHttpUtils
import me.kuku.pojo.Result
import me.kuku.yuq.utils.YuqUtils
import org.jsoup.Jsoup
import java.net.URLEncoder

object ToolLogic {

    private fun baiKeByUrl(url: String): Result<String> {
        var response = OkHttpUtils.get(url)
        while (response.code == 302) {
            response.close()
            val location = response.header("location")!!
            if (location.startsWith("//baike.baidu.com/search/none")) return Result.failure("")
            val resultUrl = if (location.startsWith("//")) "https:$location"
            else "https://baike.baidu.com$location"
            response = OkHttpUtils.get(resultUrl)
        }
        val html = OkHttpUtils.str(response)
        val doc = Jsoup.parse(html)
        val result = doc.select(".lemma-summary .para").first()?.text()
            ?: return Result.failure(210, "", "https://baike.baidu.com" + doc.select("li[class=list-dot list-dot-paddingleft]").first()?.getElementsByTag("a")?.first()?.attr("href"))
        return Result.success(result)
    }

    fun baiKe(text: String): String {
        val encodeText = URLEncoder.encode(text, "utf-8")
        val url = "https://baike.baidu.com/search/word?word=$encodeText"
        val result = baiKeByUrl(url)
        return if (result.isSuccess)
            """
                ${result.data}
                查看详情： ${YuqUtils.shortUrl(url)}
            """.trimIndent()
        else if (result.code == 210) {
            val resultUrl = result.data
            """
                ${baiKeByUrl(resultUrl).data}
                查看详情：${YuqUtils.shortUrl(resultUrl)}
            """.trimIndent()
        } else """
            抱歉，没有找到与"$text"相关的百科结果
        """.trimIndent()
    }

}
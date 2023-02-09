package me.kuku.mirai.logic

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.kuku.mirai.utils.ffmpeg
import me.kuku.pojo.CommonResult
import me.kuku.utils.*
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

object ToolLogic {

    suspend fun baiKe(text: String): String {

        suspend fun baiKeByUrl(url: String): CommonResult<String> {
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

        val encodeText = text.toUrlEncode()
        val url = "https://baike.baidu.com/search/word?word=$encodeText"
        val result = baiKeByUrl(url)
        return if (result.success())
            """
                ${result.data}
                查看详情： $url
            """.trimIndent()
        else if (result.code == 210) {
            val resultUrl = result.data()
            """
                ${baiKeByUrl(resultUrl).data}
                查看详情：$resultUrl
            """.trimIndent()
        } else """
            抱歉，没有找到与"$text"相关的百科结果
        """.trimIndent()
    }

    suspend fun saucenao(url: String): List<SaucenaoResult> {
        val urlJsonNode = OkHttpKtUtils.getJson("https://saucenao.com/search.php?output_type=2&numres=16&url=${url.toUrlEncode()}&api_key=${"TW1GbE5qUTVNalF3TkRObVltVmtOemxrTkRVM1lUUm1OVEUzTmpZNE5XRXdOR1UyWlRRM1lnPT0=".base64Decode().toString(Charset.defaultCharset()).base64Decode().toString(Charset.defaultCharset())}")
        if (urlJsonNode.get("header").getInteger("status") != 0) error(urlJsonNode.get("header").getString("message"))
        val jsonList = urlJsonNode.get("results")
        val list = mutableListOf<SaucenaoResult>()
        for (jsonNode in jsonList) {
            val header = jsonNode.get("header")
            val data = jsonNode.get("data")
            val similarity = header["similarity"]?.asText() ?: ""
            val thumbnail = header["thumbnail"]?.asText() ?: ""
            val indexName = header["index_name"]?.asText() ?: ""
            val extUrls = data.get("ext_urls")?.let {
                val letList = mutableListOf<String>()
                it.forEach { k -> letList.add(k.asText()) }
                letList
            } ?: listOf()
            val author = data.get("creator_name")?.asText() ?: data.get("member_name")?.asText() ?: data.get("author_name")?.asText() ?: ""
            val title = data.get("title")?.asText() ?: data.get("jp_name")?.asText() ?: ""
            val authorUrl = data.get("author_url")?.asText() ?: ""
            list.add(SaucenaoResult(similarity, thumbnail, indexName, extUrls, title, author, authorUrl).also {
                it.daId = data["da_id"]?.asLong() ?: 0
                it.pixivId = data["pixiv_id"]?.asLong() ?: 0
                it.faId = data["fa_id"]?.asLong() ?: 0
                it.tweetId = data["tweet_id"]?.asLong() ?: 0
            })
        }
        return list
    }

    suspend fun positiveEnergy(date: String): File {
        DateTimeFormatterUtils.parseToLocalDate(date, "yyyyMMdd")
        val html = client.get("http://tv.cctv.com/lm/xwlb/day/$date.shtml").bodyAsText()
        val url =
            Jsoup.parse(html).getElementsByTag("li").first()?.getElementsByTag("a")?.last()?.attr("href") ?: error("未找到新闻联播链接")
        val nextHtml = client.get(url).bodyAsText()
        val guid = MyUtils.regex("guid = \"", "\";", nextHtml) ?: error("没有找到guid")
        val tsp = System.currentTimeMillis().toString().substring(0, 10)
        val vc = "${tsp}204947899B86370B879139C08EA3B5E88267BF11E55294143CAE692F250517A4C10C".md5().uppercase()
        val jsonNode =
            client.get("https://vdn.apps.cntv.cn/api/getHttpVideoInfo.do?pid=$guid&client=flash&im=0&tsp=$tsp&vn=2049&vc=$vc&uid=BF11E55294143CAE692F250517A4C10C&wlan=")
                .bodyAsText().toJsonNode()
        val urlList = jsonNode["video"]["chapters4"].map { it["url"].asText() }
        val list = mutableListOf<File>()
        for (i in urlList.indices) {
            client.get(urlList[i]).bodyAsChannel().toInputStream().use { iis ->
                val file = IOUtils.writeTmpFile("$date-$i.mp4", iis)
                list.add(file)
            }
        }
        val sb = StringBuilder()
        for (file in list) {
            sb.appendLine("file ${file.absolutePath.replace("\\", "/")}")
        }
        sb.removeSuffix("\n")
        val txtFile = File("$date.txt")
        val txtFos = withContext(Dispatchers.IO) {
            FileOutputStream(txtFile)
        }
        IOUtils.write(sb.toString().byteInputStream(), txtFos)
        val newPath = list[0].absolutePath.replace("$date-0.mp4", "$date-output.mp4")
        ffmpeg("ffmpeg -f concat -safe 0 -i $date.txt -c copy $newPath")
        list.forEach { it.delete() }
        txtFile.delete()
        return File(newPath)
    }

}

data class SaucenaoResult(
    val similarity: String, val thumbnail: String, val indexName: String, val extUrls: List<String>, val title: String, val author: String, val authUrl: String,
    var daId: Long? = null, var pixivId: Long? = null, var faId: Long? = null, var tweetId: Long? = null
)

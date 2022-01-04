package me.kuku.yuq.logic

import me.kuku.pojo.UA.PC
import me.kuku.utils.MyUtils
import me.kuku.utils.OkHttpUtils
import org.jsoup.Jsoup

object HostLocLogic {

    fun post(): List<HostLocPost> {
        val list = mutableListOf<HostLocPost>()
        val html = kotlin.runCatching {
            OkHttpUtils.getStr("https://hostloc.com/forum.php?mod=forumdisplay&fid=45&filter=author&orderby=dateline",
                OkHttpUtils.addHeaders("", "https://hostloc.com/forum.php", PC))
        }.onFailure {
            return list
        }
        val elements = Jsoup.parse(html.getOrThrow()).getElementsByTag("tbody")
        for (ele in elements) {
            if (!ele.attr("id").startsWith("normalth")) continue
            val s = ele.getElementsByClass("s").first()!!
            val title = s.text()
            val url = "https://hostloc.com/${s.attr("href")}"
            val time = ele.select("em a span").first()?.text() ?: ""
            val name = ele.select("cite a").first()?.text() ?: ""
            val id = MyUtils.regex("tid=", "&", url)?.toInt() ?: 0
            val hostLocPost = HostLocPost(id, name, time, title, url)
            list.add(hostLocPost)
        }
        return list
    }

    fun postContent(url: String): String {
        val str = OkHttpUtils.getStr(url, OkHttpUtils.addUA(PC))
        val pct = Jsoup.parse(str).getElementsByClass("pct")
        return pct.first()?.text() ?: "未获取到内容，需要权限查看"
    }

}


data class HostLocPost(
    val id: Int,
    val name: String,
    val time: String,
    val title: String,
    val url: String
)
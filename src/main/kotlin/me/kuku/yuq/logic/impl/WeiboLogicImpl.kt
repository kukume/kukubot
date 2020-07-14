package me.kuku.yuq.logic.impl

import me.kuku.yuq.logic.WeiboLogic
import org.jsoup.Jsoup

class WeiboLogicImpl: WeiboLogic {
    override fun hotSearch(): String {
        val doc = Jsoup.connect("https://s.weibo.com/top/summary").get()
        val elements = doc.getElementById("pl_top_realtimehot").getElementsByTag("tbody").first()
                .getElementsByTag("tr")
        val sb = StringBuilder()
        for (ele in elements){
            var text: String = ele.getElementsByClass("td-01").first().text()
            text = if (text == "") "顶" else text
            val title: String = ele.getElementsByClass("td-02").first().getElementsByTag("a").first().text()
            sb.appendln("$text、$title")
        }
        return sb.toString()
    }
}
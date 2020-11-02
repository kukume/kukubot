package me.kuku.yuq.logic.impl

import com.IceCreamQAQ.Yu.util.IO
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.removeSuffixLine
import org.jsoup.Jsoup
import java.io.File
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class ToolLogicImpl: ToolLogic {
    private val url = "https://www.mxnzp.com/api"
    private val appId = "ghpgtsokjvkjdmlk"
    private val appSecret = "N2hNMC93empxb0twUW1jd1FRbVVtQT09"
    private val params = "&app_id=$appId&app_secret=$appSecret"
    private val myApi = "https://api.kuku.me"

    override fun dogLicking() : String {
        val response = OkHttpClientUtils.get("http://api.yyhy.me/tg.php?type=api")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 1)
            "${jsonObject.getString("date")}\n${jsonObject.getString("content")}"
        else "获取失败"
    }

    override fun baiKe(text: String): String {
        val encodeText = URLEncoder.encode(text, "utf-8")
        val url = "https://baike.baidu.com/search/word?word=$encodeText"
        val commonResult = this.baiKeByUrl(url)
        return when (commonResult.code) {
            200 -> {
                commonResult.t + "\n查看详情：" + BotUtils.shortUrl(url)
            }
            210 -> {
                val resultUrl = commonResult.t!!
                baiKeByUrl(resultUrl).t + "\n查看详情：" + BotUtils.shortUrl(resultUrl)
            }
            else -> "抱歉，没有找到与“$text”相关的百科结果。"
        }
    }

    private fun baiKeByUrl(url: String): CommonResult<String>{
        var response = OkHttpClientUtils.get(url)
        while (response.code == 302){
            response.close()
            val location = response.header("Location")!!
            if (location.startsWith("//baike.baidu.com/search/none")) return CommonResult(500, "")
            val resultUrl = if (location.startsWith("//")) "https:$location" else "https://baike.baidu.com$location"
            response = OkHttpClientUtils.get(resultUrl)
        }
        val html = OkHttpClientUtils.getStr(response)
        val doc = Jsoup.parse(html)
        val result = doc.select(".lemma-summary .para")?.first()?.text()
        return if (result != null) CommonResult(200, "", result)
        else {
            CommonResult(210, "", "https://baike.baidu.com" + doc.select("li[class=list-dot list-dot-paddingleft]").first().getElementsByTag("a").first().attr("href"))
        }
    }

    override fun mouthOdor(): String {
        val response = OkHttpClientUtils.get("https://s.nmsl8.club/getloveword?type=2")
        return OkHttpClientUtils.getJson(response).getString("content")
    }

    override fun mouthSweet(): String {
        val response = OkHttpClientUtils.get("https://s.nmsl8.club/getloveword?type=1")
        return OkHttpClientUtils.getJson(response).getString("content")
    }

    override fun poisonousChickenSoup(): String {
        val response = OkHttpClientUtils.get("https://v1.alapi.cn/api/soul")
        return if (response.code == 200)
            OkHttpClientUtils.getJson(response).getJSONObject("data").getString("title")
        else "获取失败"
    }

    override fun loveWords(): String =
            OkHttpClientUtils.getStr(OkHttpClientUtils.get("https://v1.alapi.cn/api/qinghua?format=text"))

    override fun saying(): String {
        val response = OkHttpClientUtils.get("https://v1.alapi.cn/api/mingyan")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200){
            val data = jsonObject.getJSONObject("data")
            "${data.getString("content")}-----${data.getString("author")}"
        }else "获取失败"
    }

    override fun queryIp(ip: String): String {
        val response = OkHttpClientUtils.get("http://ipaddr.cz88.net/data.php?ip=$ip",
                OkHttpClientUtils.addUA(OkHttpClientUtils.PC_UA))
        val str = OkHttpClientUtils.getStr(response)
        val list = str.split("'")
        // 1-ip  3 - addr 5 - ua
        return list[3]
    }

    override fun queryWhois(domain: String): String {
        val response = OkHttpClientUtils.get("https://api.devopsclub.cn/api/whoisquery?domain=$domain&standard=true")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val data = jsonObject.getJSONObject("data").getJSONObject("data")
        return if (data.size == 0) "未找到该域名的whois信息"
        else {
            val sb = StringBuilder()
            sb.appendLine("域名：${data.getString("domainName")}")
                    .appendLine("域名状态：${data.getString("domainStatus")}")
                    .appendLine("联系人：${data.getString("registrant")}")
                    .appendLine("联系邮箱：${data.getString("contactEmail")}")
                    .appendLine("注册商：${data.getString("registrar")}")
                    .appendLine("DNS：${data.getString("dnsNameServer")}")
                    .appendLine("创建时间：${data.getString("registrationTime")}")
                    .append("过期时间：${data.getString("expirationTime")}")
            sb.toString()
        }
    }

    override fun queryIcp(domain: String): String {
        val response = OkHttpClientUtils.get("https://api.devopsclub.cn/api/icpquery?url=$domain")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val data = jsonObject.getJSONObject("data")
        return if (data.size == 0) "未找到该域名的备案信息"
        else{
            val sb = StringBuilder()
            sb.appendLine("主办单位名称：${data.getString("organizer_name")}")
                    .appendLine("主办单位性质：${data.getString("organizer_nature")}")
                    .appendLine("网站备案/许可证号：${data.getString("recording_license_number")}")
                    .appendLine("网站名称：${data.getString("site_name")}")
                    .appendLine("网站首页网址：${data.getString("site_index_url")}")
                    .append("审核时间：${data.getString("review_time")}")
            sb.toString()
        }
    }

    override fun zhiHuDaily(): String {
        val response = OkHttpClientUtils.get("https://v1.alapi.cn/api/zhihu/latest")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200){
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("stories")
            val sb = StringBuilder()
            jsonArray.forEach {
                val json = it as JSONObject
                sb.appendLine("标题：${json.getString("title")}")
                        .appendLine("链接：${json.getString("url")}").appendLine(" -------------- ")
            }
            sb.toString()
        }else "获取失败${jsonObject.getString("msg")}"
    }

    override fun qqGodLock(qq: Long): String {
        val doc = Jsoup.connect("http://qq.link114.cn/$qq").get()
        val ele = doc.getElementById("main").getElementsByClass("listpage_content").first()
        val elements = ele.getElementsByTag("dl")
        val sb = StringBuilder()
        for (element in elements){
            sb.append(element.getElementsByTag("dt").first().text()).appendLine(element.getElementsByTag("dd").text())
        }
        return sb.toString()
    }

    override fun convertPinYin(word: String): String {
        val response = OkHttpClientUtils.get("https://v1.alapi.cn/api/pinyin?word=$word&tone=1")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200)
            jsonObject.getJSONObject("data").getString("pinyin")
        else "转换失败"
    }

    private fun convertUrl(path: String) = "$url$path?$params"

    override fun jokes(): String {
        val response = OkHttpClientUtils.get(this.convertUrl("/jokes/list/random"))
        val jsonObject = OkHttpClientUtils.getJson(response)
        val data = jsonObject.getJSONArray("data")
        return data.getJSONObject(Random.nextInt(data.size - 1)).getString("content")
    }

    override fun rubbish(name: String): String {
        val response = OkHttpClientUtils.post(this.convertUrl("/rubbish/type"), OkHttpClientUtils.addForms("name", name))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0) "没有这个垃圾"
        else{
            val sb = StringBuilder()
            val data = jsonObject.getJSONObject("data")
            val aim = data.getJSONObject("aim")
            if (aim != null)
                sb.appendLine("${aim.getString("goodsName")}；${aim.getString("goodsType")}")
            val recommendList = data.getJSONArray("recommendList")
            for (i in (0 until recommendList.size)){
                val obj = recommendList.getJSONObject(i)
                sb.appendLine("${obj.getString("goodsName")}；${obj.getString("goodsType")}")
            }
            sb.toString()
        }
    }

    override fun historyToday(): String {
        val response = OkHttpClientUtils.get(this.convertUrl("/history/today"))
        val data = OkHttpClientUtils.getJson(response).getJSONArray("data")
        val sb = StringBuilder()
        for (i in data){
            val jsonObject = i as JSONObject
            sb.appendLine("${jsonObject.getString("year")}年${jsonObject.getInteger("month")}月" +
                    "${jsonObject.getInteger("day")}日，${jsonObject.getString("title")}")
        }
        return sb.toString()
    }

    override fun convertZh(content: String, type: Int): String {
        val response = OkHttpClientUtils.post(this.convertUrl("/convert/zh"),
                OkHttpClientUtils.addForms("content", content, "type", type.toString()))
        return OkHttpClientUtils.getJson(response).getJSONObject("data").getString("convertContent")
    }

    override fun convertTranslate(content: String, from: String, to: String): String {
        val response = OkHttpClientUtils.post(this.convertUrl("/convert/translate"),
                OkHttpClientUtils.addForms("content", content, "from", from, "to", to))
        val jsonObject = OkHttpClientUtils.getJson(response)
        val data = jsonObject.getJSONObject("data")
        return if (data == null) jsonObject.getString("msg")
        else data.getString("result")
    }

    override fun parseVideo(url: String): String {
        val response = OkHttpClientUtils.post("https://api.devopsclub.cn/api/svp",
                OkHttpClientUtils.addForms("url", url))
        val jsonObject = OkHttpClientUtils.getJson(response)
        val data = jsonObject.getJSONObject("data")
        return if (data.size != 0){
            "描述：${data.getString("desc")}\n图片：${BotUtils.shortUrl(data.getString("pic"))}\n视频：${BotUtils.shortUrl(data.getString("video"))}\n音乐：${BotUtils.shortUrl(data.getString("music"))}"
        }else "没有找到该视频"
    }

    override fun restoreShortUrl(url: String): String {
        val afterUrl: String = if (!url.startsWith("http")) "http://$url" else url
        val response = OkHttpClientUtils.get(afterUrl)
        response.close()
        return response.header("Location") ?: "该链接不能再跳转了！"
    }

    override fun weather(local: String, cookie: String): CommonResult<String> {
        var code: String? = null
        var id: String? = null
        val cityResponse = OkHttpClientUtils.get("https://qq-web.cdn-go.cn/city-selector/41c008e0/app/index/dist/cdn/index.bundle.js")
        val jsStr = OkHttpClientUtils.getStr(cityResponse)
        val jsonStr = BotUtils.regex("var y=c\\(\"[0-9a-z]{20}\"\\),p=", ",s=\\{name", jsStr)
        val cityJsonArray = JSON.parseArray(jsonStr)
        for (i in cityJsonArray.indices){
            val jsonObject = cityJsonArray.getJSONObject(i)
            if (jsonObject.getString("district") == local){
                id = jsonObject.getString("areaid")
                code = jsonObject.getString("adcode")
                break
            }
        }
        return if (code != null) {
            val url = "https://weather.mp.qq.com/?city=${URLEncoder.encode(local, "utf-8")}&areaid=$id&adcode=$code&star=8"
            val response = OkHttpClientUtils.get(url, OkHttpClientUtils.addHeaders(
                    "cookie", cookie,
                    "user-agent", OkHttpClientUtils.QQ_UA2
            ))
            if (response.code == 302) return CommonResult(500, "Cookie已失效！！")
            val html = OkHttpClientUtils.getStr(response)
            val doc = Jsoup.parse(html)
            val city = doc.getElementById("s_city").text()
            val temperature = doc.select(".cur-weather-info .date span").first().text()
            val air = doc.select("._val").first().text()
            val xmlStr = "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"146\" templateID=\"1\" action=\"web\" brief=\"[分享] $city  \" sourcePublicUin=\"2658655094\" sourceMsgId=\"0\" url=\"https://weather.mp.qq.com/?city=${URLEncoder.encode(city, "utf-8")}&amp;areaid=$id&amp;adcode=$code&amp;st=0&amp;_wv=1\" flag=\"0\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"2\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://imgcache.qq.com/ac/qqweather/image/share_icon/cloud.png\" w=\"0\" h=\"0\" /><title>$city  </title><summary>$temperature\n" +
                    "空气质量:$air</summary></item><source name=\"QQ天气\" icon=\"https://url.cn/JS8oE7\" action=\"plugin\" a_actionData=\"mqqapi://app/action?pkg=com.tencent.mobileqq&amp;cmp=com.tencent.biz.pubaccount.AccountDetailActivity&amp;uin=2658655094\" i_actionData=\"mqqapi://card/show_pslcard?src_type=internal&amp;card_type=public_account&amp;uin=2658655094&amp;version=1\" appid=\"-1\" /></msg>"
            CommonResult(200, "", xmlStr)
        }else CommonResult(500, "没有找到这个城市")
    }

    override fun ping(domain: String): String {
        val runtime = Runtime.getRuntime()
        val os = System.getProperty("os.name")
        val pingStr = if (os.contains("Windows")) "ping $domain -n 1"
        else "ping $domain -c 1"
        val process = runtime.exec(pingStr)
        if (process != null){
            val bytes = IO.read(process.inputStream)
            val result = if (os.contains("Windows")) String(bytes, Charset.forName("gbk"))
            else String(bytes, Charset.forName("utf-8"))
            if (result.contains("找不到主机") || result.contains("Name or service not known")) return "域名解析失败！！"
            val ip = BotUtils.regex("\\[", "\\]", result)?.trim() ?: BotUtils.regex("\\(", "\\)", result)?.trim() ?: return "域名解析失败！！！"
            val time = BotUtils.regex("时间=", "ms", result)?.trim() ?: BotUtils.regex("time=", "ms", result)?.trim() ?: "请求超时"
            val ipInfo = this.queryIp(ip)
            val sb = StringBuilder("====查询结果====\n")
            sb.appendLine("域名/IP：$domain")
            sb.appendLine("IP：：$ip")
            sb.appendLine("延迟：${time}ms")
            sb.append("位置：$ipInfo")
            return sb.toString()
        }
        return "ping失败，请稍后再试！！"
    }

    override fun colorPicByLoLiCon(apiKey: String, isR18: Boolean): CommonResult<Map<String, String>> {
        val response = OkHttpClientUtils.get("https://api.lolicon.app/setu/?apikey=$apiKey&r18=${if (isR18) 1 else 0}")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("code")){
            0 -> {
                val dataJsonObject = jsonObject.getJSONArray("data").getJSONObject(0)
                CommonResult(200, "", mapOf(
                        "count" to jsonObject.getString("quota"),
                        "time" to jsonObject.getString("quota_min_ttl"),
                        "url" to dataJsonObject.getString("url"),
                        "title" to dataJsonObject.getString("title"),
                        "pid" to dataJsonObject.getString("pid"),
                        "uid" to dataJsonObject.getString("uid")
                ))
            }
            401 -> CommonResult(500, "APIKEY 不存在或被封禁")
            429 -> CommonResult(500, "达到调用额度限制，距离下一次恢复额度时间：${jsonObject.getLong("quota_min_ttl")}秒")
            else -> CommonResult(500, jsonObject.getString("msg"))
        }
    }

    override fun piXivPicProxy(url: String): ByteArray {
        val response = OkHttpClientUtils.get("$myApi/pixiv/picbyurl?url=${URLEncoder.encode(url, "utf-8")}")
        return OkHttpClientUtils.getBytes(response)
    }

    override fun hiToKoTo(): Map<String, String> {
        val response = OkHttpClientUtils.get("https://v1.hitokoto.cn/")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return mapOf("text" to jsonObject.getString("hitokoto"), "from" to jsonObject.getString("from"))
    }

    override fun creatQr(content: String): ByteArray {
        val response = OkHttpClientUtils.get("https://www.zhihu.com/qrcode?url=${URLEncoder.encode(content, "utf-8")}")
        return OkHttpClientUtils.getBytes(response)
    }

    override fun girlImage(): String {
        val response = OkHttpClientUtils.get("$url/image/girl/list/random?$params")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val jsonArray = jsonObject.getJSONArray("data")
        return jsonArray.getJSONObject(Random.nextInt(jsonArray.size)).getString("imageUrl")
    }

    override fun lolFree(): String {
        val response = OkHttpClientUtils.get("http://game.gtimg.cn/images/lol/act/img/js/heroList/hero_list.js")
        val jsonArray = OkHttpClientUtils.getJson(response).getJSONArray("hero")
        val sb = StringBuilder("LOL本周周免英雄如下：\r\n")
        for (i in jsonArray.indices){
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getInteger("isWeekFree") == 1){
                sb.appendLine("${jsonObject.getString("name")}-${jsonObject.getString("title")}")
            }
        }
        return sb.removeSuffixLine().toString()
    }

    override fun abbreviation(content: String): String {
        val response = OkHttpClientUtils.post("https://lab.magiconch.com/api/nbnhhsh/guess",
                OkHttpClientUtils.addJson("{\"text\": \"$content\"}"))
        val str = OkHttpClientUtils.getStr(response)
        val jsonArray = JSON.parseArray(str)
        return if (jsonArray.size > 0){
            val transJsonArray = jsonArray.getJSONObject(0).getJSONArray("trans") ?: return "没有查询到结果！！"
            val sb = StringBuilder("缩写${content}的含义如下：\r\n")
            for (i in 0 until transJsonArray.size){
                sb.appendLine(transJsonArray.getString(i))
            }
            sb.removeSuffixLine().toString()
        }else "没有查询到结果"
    }

    override fun queryTime(): ByteArray {
        val sdf = SimpleDateFormat("HH-mm", Locale.CHINA)
        val time = sdf.format(Date())
        val response = OkHttpClientUtils.get("https://u.iheit.com/images/time/$time.jpg")
        return OkHttpClientUtils.getBytes(response)
    }

    override fun queryVersion(): String {
        val response = OkHttpClientUtils.get("https://github.com/kukume/kuku-bot/tags")
        val html = OkHttpClientUtils.getStr(response)
        val elements = Jsoup.parse(html).select(".Details .d-flex .commit-title a")
        val ele = elements[0]
        return ele.text()
    }

    override fun music163cloud(): String {
        val response = OkHttpClientUtils.get("http://api.heerdev.top/nemusic/random")
        return OkHttpClientUtils.getJson(response).getString("text")
    }

    override fun searchQuestion(question: String): String {
        val response = OkHttpClientUtils.get("http://api.xmlm8.com/tk.php?t=$question")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return """
            问题：${jsonObject.getString("tm")}
            答案：${jsonObject.getString("da")}
        """.trimIndent()
    }

    override fun bvToAv(bv: String): CommonResult<Map<String, String>> {
        if (bv.length != 12) return CommonResult(500, "不合格的bv号！！")
        val response = OkHttpClientUtils.get("https://api.bilibili.com/x/web-interface/view?bvid=$bv")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("code")) {
            0 -> {
                val dataJsonObject = jsonObject.getJSONObject("data")
                CommonResult(200, "", mapOf(
                        "pic" to dataJsonObject.getString("pic"),
                        "dynamic" to dataJsonObject.getString("dynamic"),
                        "title" to dataJsonObject.getString("title"),
                        "desc" to dataJsonObject.getString("desc"),
                        "aid" to dataJsonObject.getString("aid"),
                        "url" to "https://www.bilibili.com/video/av${dataJsonObject.getString("aid")}"
                ))
            }
            -404 -> CommonResult(500, "没有找到该BV号！！")
            else -> CommonResult(500, jsonObject.getString("message"))
        }
    }

    override fun zhiHuHot(): List<Map<String, String>> {
        val response = OkHttpClientUtils.get("https://www.zhihu.com/hot",
                OkHttpClientUtils.addUA(OkHttpClientUtils.MOBILE_UA))
        val html = OkHttpClientUtils.getStr(response)
        val elements = Jsoup.parse(html).getElementsByClass("css-hi1lih")
        val list = mutableListOf<Map<String, String>>()
        for (ele in elements){
            list.add(mapOf(
                    "title" to ele.getElementsByClass("css-dk79m8").first().text(),
                    "url" to ele.attr("href"),
                    "hot" to ele.getElementsByClass("css-1ixcu37").first().text().trim('\n')
            ))
        }
        return list
    }

    override fun hostLocPost(): List<Map<String, String>> {
        val response = OkHttpClientUtils.get("https://www.hostloc.com/forum.php?mod=forumdisplay&fid=45&filter=author&orderby=dateline",
                OkHttpClientUtils.addUA(OkHttpClientUtils.PC_UA))
        val html = OkHttpClientUtils.getStr(response)
        val elements = Jsoup.parse(html).getElementsByTag("tbody")
        val list = mutableListOf<Map<String, String>>()
        for (ele in elements){
            if (!ele.attr("id").startsWith("normalth")) continue
            val s = ele.getElementsByClass("s").first()
            val title = s.text()
            val url = "https://www.hostloc.com/" + s.attr("href")
            val name = ele.select("cite a").first().text()
            val time = ele.select("em a span").first().text()
            val id = BotUtils.regex("tid=", "&", url)!!
            list.add(mapOf(
                    "title" to title,
                    "url" to url,
                    "name" to name,
                    "time" to time,
                    "id" to id
            ))
        }
        return list
    }

    override fun wordSegmentation(text: String): String {
        val response = OkHttpClientUtils.get("https://api.devopsclub.cn/api/segcut?text=${URLEncoder.encode(text, "utf-8")}")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0){
            val sb = StringBuilder()
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("result")
            jsonArray.forEach{  sb.appendLine(it) }
            return sb.removeSuffixLine().toString()
        }else jsonObject.getString("msg")
    }

    override fun acgPic(): String {
        val response = OkHttpClientUtils.get("https://v1.alapi.cn/api/acg")
        response.close()
        return response.header("location")!!
    }

    override fun danBooRuPic(): Map<String, String> {
        val tagResponse = OkHttpClientUtils.get("https://danbooru.donmai.us/",
                OkHttpClientUtils.addUA(OkHttpClientUtils.PC_UA))
        val tagHtml = OkHttpClientUtils.getStr(tagResponse)
        val elements = Jsoup.parse(tagHtml).select("#tag-box ul li")
        val tags = mutableListOf<Map<String, String>>()
        elements.forEach { tags.add(mapOf(
                "tag" to it.attr("data-tag-name"),
                "num" to it.getElementsByClass("post-count").first().attr("title")
        )) }
        val tagMap = tags[Random.nextInt(tags.size)]
        val tag = tagMap.getValue("tag")
        val page = tagMap.getValue("num").toInt() / 20
        val maxPage = if (page > 1000) 1000 else page
        val picResponse = OkHttpClientUtils.get("https://danbooru.donmai.us/posts?tags=$tag&page=${Random.nextInt(1, maxPage + 1)}",
                OkHttpClientUtils.addUA(OkHttpClientUtils.PC_UA))
        val picHtml = OkHttpClientUtils.getStr(picResponse)
        val picElements = Jsoup.parse(picHtml).select("#posts-container article")
        val pics = mutableListOf<Map<String, String>>()
        picElements.forEach {
            pics.add(mapOf(
                    "picUrl" to it.attr("data-file-url"),
                    "originalUrl" to it.attr("data-source"),
                    "detailedUrl" to it.attr("data-normalized-source"),
                    "url" to "https://danbooru.donmai.us/posts/${it.attr("data-id")}"
            ))
        }
        return pics[Random.nextInt(pics.size)]
    }

    override fun identifyPic(url: String): String? {
        val response = OkHttpClientUtils.get("https://saucenao.com/search.php?url=$url&output_type=2")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val resultJsonObject = jsonObject.getJSONArray("results")
        return resultJsonObject?.getJSONObject(0)?.getJSONObject("data")?.getJSONArray("ext_urls")?.getString(0)
    }

    override fun githubQuicken(gitUrl: String) = "https://github.kuku.workers.dev/$gitUrl"

    override fun traceRoute(domain: String): String {
        val osName = System.getProperty("os.name")
        if (osName.contains("Windows")) return "不支持Windows系统！！"
        val file = File("besttrace")
        val runtime = Runtime.getRuntime()
        if (!file.exists()) {
            val response = OkHttpClientUtils.get("https://u.iheit.com/kuku/bot/besttrace")
            val bytes = OkHttpClientUtils.getBytes(response)
            IO.writeFile(file, bytes)
            runtime.exec("chmod +x besttrace")
        }
        val process = runtime.exec("./besttrace $domain")
        val resultBytes = IO.read(process.inputStream)
        return String(resultBytes, Charset.forName("utf-8"))
    }

    override fun teachYou(content: String, type: String): String? {
        val msg: String
        val url: String
        val suffix = URLEncoder.encode(Base64.getEncoder().encodeToString(content.toByteArray()), "utf-8")
        when (type){
            "baidu" -> {
                msg = "百度"
                url = "https://u.iheit.com/teachsearch/baidu/index.html?q=$suffix"
            }
            "google" -> {
                msg = "谷歌"
                url = "https://u.iheit.com/teachsearch/google/index.html?q=$suffix"
            }
            "bing" -> {
                msg = "必应"
                url = "https://u.iheit.com/teachsearch/bing/index.html?q=$suffix"
            }
            "sougou" -> {
                msg = "搜狗"
                url = "https://u.iheit.com/teachsearch/sougou/index.html?q=$suffix"
            }
            else -> return null
        }
        return "点击以下链接即可教您使用${msg}搜索“$content”\n${BotUtils.shortUrl(url)}"
    }

    override fun preventQQRed(url: String): String {
        val jsonObject = OkHttpClientUtils.postJson("https://www.91she.cn/ajax.php?act=creat", mapOf(
                "url" to url,
                "type" to "2",
                "dwz" to "2"
        ), OkHttpClientUtils.addUA(OkHttpClientUtils.PC_UA))
        return if (jsonObject.getInteger("code") == 0){
            jsonObject.getString("dwz1")
        }else {
            jsonObject.getString("msg")
        }
    }

    override fun preventQQWechatRed(url: String): String {
        val jsonObject = OkHttpClientUtils.getJson("http://fh.dw81.cn:81/dwz.php?type=ty&longurl=$url&dwzapi=500",
                OkHttpClientUtils.addUA(OkHttpClientUtils.PC_UA))
        return if (jsonObject.getInteger("code ") == 1){
            jsonObject.getString("ae_url")
        }else jsonObject.getString("msg")
    }
}
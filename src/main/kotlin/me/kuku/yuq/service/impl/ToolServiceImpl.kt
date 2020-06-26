package me.kuku.yuq.service.impl

import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.service.ToolService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import org.jsoup.Jsoup
import java.lang.NullPointerException
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class ToolServiceImpl: ToolService {
    private val url = "https://www.mxnzp.com/api"
    private val appId = "ghpgtsokjvkjdmlk"
    private val appSecret = "N2hNMC93empxb0twUW1jd1FRbVVtQT09"
    private val params = "&app_id=$appId&app_secret=$appSecret"

    private val neTeaseUrl = "https://netease.kuku.me"

    override fun dogLicking() : String {
        val response = OkHttpClientUtils.get("http://api.yyhy.me/tg.php?type=api")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 1)
            "${jsonObject.getString("date")}\n${jsonObject.getString("content")}"
        else "获取失败"
    }

    override fun baiKe(text: String): String {
        val encodeText = URLEncoder.encode(text, "utf-8")
        var url = "https://baike.baidu.com/item/$encodeText"
        var response = OkHttpClientUtils.get(url)
        if (response.code == 302){
            response.close()
            val location = response.header("Location")
            if ("https://baike.baidu.com/error.html" == location) return "抱歉，没有找到与“$text”相关的百科结果。"
            url = "https://baike.baidu.com$location"
            response = OkHttpClientUtils.get(url)
        }
        val html = OkHttpClientUtils.getStr(response)
        val doc = Jsoup.parse(html)
        return try {
            doc.select(".lemma-summary .para").first().text() + "\n查看详情：" + BotUtils.shortUrl(url)
        }catch (e : NullPointerException){
            "查询失败，链接：$url"
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
        val response = OkHttpClientUtils.get("https://v1.alapi.cn/api/ip?ip=$ip")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200) {
            val data = jsonObject.getJSONObject("data")
            "$ip-${data.getString("pos")} ${data.getString("isp")}"
        } else jsonObject.getString("msg")
    }

    override fun queryWhois(domain: String): String {
        val response = OkHttpClientUtils.get("https://api.devopsclub.cn/api/whoisquery?domain=$domain&standard=true")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val data = jsonObject.getJSONObject("data").getJSONObject("data")
        return if (data.size == 0) "未找到该域名的whois信息"
        else {
            val sb = StringBuilder()
            sb.appendln("域名：${data.getString("domainName")}")
                    .appendln("域名状态：${data.getString("domainStatus")}")
                    .appendln("联系人：${data.getString("registrant")}")
                    .appendln("联系邮箱：${data.getString("contactEmail")}")
                    .appendln("注册商：${data.getString("registrar")}")
                    .appendln("DNS：${data.getString("dnsNameServer")}")
                    .appendln("创建时间：${data.getString("registrationTime")}")
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
            sb.appendln("主办单位名称：${data.getString("organizer_name")}")
                    .appendln("主办单位性质：${data.getString("organizer_nature")}")
                    .appendln("网站备案/许可证号：${data.getString("recording_license_number")}")
                    .appendln("网站名称：${data.getString("site_name")}")
                    .appendln("网站首页网址：${data.getString("site_index_url")}")
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
                sb.appendln("标题：${json.getString("title")}")
                        .appendln("链接：${json.getString("url")}").appendln(" -------------- ")
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
            sb.append(element.getElementsByTag("dt").first().text()).appendln(element.getElementsByTag("dd").text())
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
                sb.appendln("${aim.getString("goodsName")}；${aim.getString("goodsType")}")
            val recommendList = data.getJSONArray("recommendList")
            for (i in (0 until recommendList.size)){
                val obj = recommendList.getJSONObject(i)
                sb.appendln("${obj.getString("goodsName")}；${obj.getString("goodsType")}")
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
            sb.appendln("${jsonObject.getString("year")}年${jsonObject.getInteger("month")}月" +
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

    override fun weather(local: String): String {
        //获取经纬度
        val firstResponse = OkHttpClientUtils.get("https://apis.map.qq.com/jsapi?qt=geoc&addr=$local&key=UGMBZ-CINWR-DDRW5-W52AK-D3ENK-ZEBRC&output=jsonp&pf=jsapi&ref=jsapi&cb=qq.maps._svcb3.geocoder0")
        val firstJsonObject = OkHttpClientUtils.getJson(firstResponse, "\\{[\\s\\S]*\\}")
        val detailJsonObject = firstJsonObject.getJSONObject("detail")
        return if (!detailJsonObject.containsKey("errmsg")){
            //查询天气
            val secondResponse = OkHttpClientUtils.get("https://api.caiyunapp.com/v2.5/4QaAKiWJ8eof2Oux/${detailJsonObject.getString("pointx")},${detailJsonObject.getString("pointy")}/hourly.json")
            val secondJsonObject = OkHttpClientUtils.getJson(secondResponse)
            val hourlyJsonObject = secondJsonObject.getJSONObject("result").getJSONObject("hourly")
            val sb = StringBuilder("${local}的天气如下：\n自然语言描述：${hourlyJsonObject.getString("description")}\n")
            //本地降水强度  降水量(mm/h)
            val precipitation = hourlyJsonObject.getJSONArray("precipitation")
            //温度
            val temperature = hourlyJsonObject.getJSONArray("temperature")
            //风  风向：direction    风速：speed
            val wind = hourlyJsonObject.getJSONArray("wind")
            //相对湿度  相对湿度(%)
            val humidity = hourlyJsonObject.getJSONArray("humidity")
            //云量  云量(0.0-1.0)
            val cloudRate = hourlyJsonObject.getJSONArray("cloudrate")
            //天气状况
            val skyCon = hourlyJsonObject.getJSONArray("skycon")
            //气压 气压(Pa)
            val pressure = hourlyJsonObject.getJSONArray("pressure")
            //能见度
            val visibility = hourlyJsonObject.getJSONArray("visibility")
            //短波辐射  向下短波辐射通量(W/M2)
            val dsWrf = hourlyJsonObject.getJSONArray("dswrf")
            for (i in 0 until 2){
                sb.appendln("--------------")
                sb.appendln("时间：${precipitation.getJSONObject(i).getString("datetime")}")
                sb.appendln("温度${temperature.getJSONObject(i).getString("value")}℃，" +
                        "气压${pressure.getJSONObject(i).getString("value")}Pa，" +
                        "相对湿度${humidity.getJSONObject(i).getString("value")}%，" +
                        "风向${wind.getJSONObject(i).getString("direction")}°，风速${wind.getJSONObject(i).getString("speed")}，" +
                        "降水量${precipitation.getJSONObject(i).getString("value")}mm/h，" +
                        "云量${cloudRate.getJSONObject(i).getString("value")}，" +
                        "短波辐射${dsWrf.getJSONObject(i).getString("value")}，" +
                        "能见度${visibility.getJSONObject(i).getString("value")}，" +
                        "天气现象${skyCon.getJSONObject(i).getString("value")}")
            }
            sb.removeSuffix("\n").toString()
        }else detailJsonObject.getString("errmsg")
    }

    override fun ping(domain: String): String {
        val response = OkHttpClientUtils.get("https://api.devopsclub.cn/api/ipv4query?ip=$domain")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0){
            val ip = jsonObject.getJSONObject("data").getString("ip")
            val ipMsg = this.queryIp(ip)
            val sb = StringBuilder("====查询结果====\n")
            sb.appendln("域名/IP：$domain")
            sb.appendln("IP：$ip")
            val secondResponse = OkHttpClientUtils.post("http://www.jsons.cn/ping/", OkHttpClientUtils.addForms("txt_url", domain))
            val str = OkHttpClientUtils.getStr(secondResponse)
            var text = Jsoup.parse(str).select("#form1 .form-group .col-sm-12 pre").first().text()
            text = BotUtils.regex("(?<=平均\\s\\=\\s).*", text)
            sb.appendln("延迟：$text")
            sb.append("位置：${ipMsg.split("-")[1]}")
            sb.toString()
        }else "无法解析域名！"
    }

    override fun colorPic(): ByteArray {
        val page = Random.nextInt(11)
        val day = LocalDate.now().minusDays(Random.nextLong(12))
        val date = day.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        var response = OkHttpClientUtils.get("https://api.imjad.cn/pixiv/v2/?type=rank&mode=day_male&page=$page&date=$date")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val jsonArray = jsonObject.getJSONArray("illusts")
        val singleJsonObject = jsonArray.getJSONObject(Random.nextInt(jsonArray.size))
        val id = singleJsonObject.getString("id")
        response = OkHttpClientUtils.get("https://www.pixivdl.net/api/pixiv/info?zid=$id")
        val json = OkHttpClientUtils.getJson(response).getJSONObject("result").getJSONObject("info").getJSONArray("urls").getJSONObject(0)
        var url = json.getJSONObject("urls").getString("regular")
        url = url.replace("i.pximg.net", "www.pixivdl.net")
        val imageResponse = OkHttpClientUtils.get(url)
        return OkHttpClientUtils.getBytes(imageResponse)
    }

    override fun hiToKoTo(): Map<String, String> {
        val response = OkHttpClientUtils.get("https://v1.hitokoto.cn/")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return mapOf("text" to jsonObject.getString("hitokoto"), "from" to jsonObject.getString("from"))
    }

    override fun songByQQ(name: String): String {
        val firstResponse = OkHttpClientUtils.get("https://c.y.qq.com/soso/fcgi-bin/client_search_cp?w=${URLEncoder.encode(name, "utf-8")}&format=json")
        val jsonObject = OkHttpClientUtils.getJson(firstResponse)
        val songJsonObject = jsonObject.getJSONObject("data").getJSONObject("song").getJSONArray("list").getJSONObject(0)
        val songName = songJsonObject.getString("songname")
        val mid = songJsonObject.getString("songmid")
        val secondResponse = OkHttpClientUtils.get("https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22$mid%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%220%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%220%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D")
        val secondJsonObject = OkHttpClientUtils.getJson(secondResponse)
        val dataJsonObject = secondJsonObject.getJSONObject("req_0").getJSONObject("data")
        val urlJsonObject = dataJsonObject.getJSONArray("midurlinfo").getJSONObject(0)
        val musicUrl = dataJsonObject.getJSONArray("sip").getString(0) + urlJsonObject.getString("purl")
        val jumpUrl = "https://y.qq.com/n/yqq/song/$mid.html"
        val thirdResponse = OkHttpClientUtils.get(jumpUrl, OkHttpClientUtils.addUA(OkHttpClientUtils.PC_UA))
        val html = OkHttpClientUtils.getStr(thirdResponse)
        val imageUrl = Jsoup.parse(html).select(".main .mod_data .data__cover img").first().attr("src")
        return "{\"app\":\"com.tencent.structmsg\",\"desc\":\"音乐\",\"view\":\"music\",\"ver\":\"0.0.0.1\",\"prompt\":\"[分享]$songName\",\"appID\":\"\",\"sourceName\":\"\",\"actionData\":\"\",\"actionData_A\":\"\",\"sourceUrl\":\"\",\"meta\":{\"music\":{\"action\":\"\",\"android_pkg_name\":\"\",\"app_type\":1,\"appid\":100497308,\"desc\":\"${songJsonObject.getJSONArray("singer").getJSONObject(0).getString("name")}\",\"jumpUrl\":\"$jumpUrl\",\"musicUrl\":\"$musicUrl\",\"preview\":\"http:$imageUrl\",\"sourceMsgId\":\"0\",\"source_icon\":\"\",\"source_url\":\"\",\"tag\":\"QQ音乐\",\"title\":\"$songName\"}},\"config\":{\"autosize\":true,\"ctime\":1592152029,\"forward\":true,\"token\":\"00a77c3ec88562b6e75b6202ede77f54\",\"type\":\"normal\"},\"text\":\"\",\"sourceAd\":\"\",\"extra\":\"\"}"
    }

    override fun songBy163(name: String): CommonResult<String> {
        val response = OkHttpClientUtils.get("$neTeaseUrl/search?keywords=${URLEncoder.encode(name, "utf-8")}")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val resultJsonObject = jsonObject.getJSONObject("result")
        return if (resultJsonObject.getInteger("songCount") != 0){
            val songJsonObject = resultJsonObject.getJSONArray("songs").getJSONObject(0)
            val id = songJsonObject.getInteger("id")
            val secondResponse = OkHttpClientUtils.get("$neTeaseUrl/song/url?id=$id")
            val secondJsonObject = OkHttpClientUtils.getJson(secondResponse)
            val url = secondJsonObject.getJSONArray("data").getJSONObject(0).getString("url")
            if (url != null){
                val songName = songJsonObject.getString("name")
                val author = songJsonObject.getJSONArray("artists").getJSONObject(0).getString("name")
                val thirdResponse = OkHttpClientUtils.get("https://y.music.163.com/m/song?id=$id", OkHttpClientUtils.addUA(OkHttpClientUtils.MOBILE_UA))
                val html = OkHttpClientUtils.getStr(thirdResponse)
                val imageUrl = Jsoup.parse(html).select("meta[property=og:image]").first().attr("content")
                CommonResult(200, "成功", "{\"app\":\"com.tencent.structmsg\",\"desc\":\"音乐\",\"view\":\"music\",\"ver\":\"0.0.0.1\",\"prompt\":\"[分享]$songName\",\"appID\":\"\",\"sourceName\":\"\",\"actionData\":\"\",\"actionData_A\":\"\",\"sourceUrl\":\"\",\"meta\":{\"music\":{\"action\":\"\",\"android_pkg_name\":\"\",\"app_type\":1,\"appid\":100497308,\"desc\":\"$author\",\"jumpUrl\":\"${"https://music.163.com/song?id=$id"}\",\"musicUrl\":\"$url\",\"preview\":\"$imageUrl\",\"sourceMsgId\":\"0\",\"source_icon\":\"\",\"source_url\":\"\",\"tag\":\"网易云音乐\",\"title\":\"$songName\"}},\"config\":{\"autosize\":true,\"ctime\":1592152029,\"forward\":true,\"token\":\"00a77c3ec88562b6e75b6202ede77f54\",\"type\":\"normal\"},\"text\":\"\",\"sourceAd\":\"\",\"extra\":\"\"}")
            }else CommonResult(500, "可能该歌曲没有版权或者无法下载！")
        }else CommonResult(500, "未找到该歌曲！！")
    }

    override fun creatQr(content: String): String {
        val response = OkHttpClientUtils.get("$url/qrcode/create/single?content=你好&size=500&type=0$params")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return jsonObject.getJSONObject("data").getString("qrCodeUrl")
    }

    override fun girlImage(): String {
        val response = OkHttpClientUtils.get("$url/image/girl/list/random?$params")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val jsonArray = jsonObject.getJSONArray("data")
        return jsonArray.getJSONObject(Random.nextInt(jsonArray.size)).getString("imageUrl")
    }
}
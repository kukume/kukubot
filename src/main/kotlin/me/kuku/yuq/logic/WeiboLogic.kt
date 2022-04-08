package me.kuku.yuq.logic

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.pojo.Result
import me.kuku.pojo.ResultStatus
import me.kuku.pojo.UA
import me.kuku.utils.OkHttpKtUtils
import me.kuku.utils.OkUtils
import me.kuku.utils.toUrlEncode
import org.jsoup.Jsoup
import java.net.URLEncoder

object WeiboLogic {

    suspend fun hotSearch(): List<HotSearch> {
        val jsonObject = OkHttpKtUtils.postJsonp("https://passport.weibo.com/visitor/genvisitor",
            mapOf("cb" to "gen_callback", "fp" to """{"os":"1","browser":"Chrome97,0,4692,99","fonts":"undefined","screenInfo":"1536*864*24","plugins":"Portable Document Format::internal-pdf-viewer::PDF Viewer|Portable Document Format::internal-pdf-viewer::Chrome PDF Viewer|Portable Document Format::internal-pdf-viewer::Chromium PDF Viewer|Portable Document Format::internal-pdf-viewer::Microsoft Edge PDF Viewer|Portable Document Format::internal-pdf-viewer::WebKit built-in PDF"}""")
        )
        val tid = jsonObject.getJSONObject("data").getString("tid")
        val response =
            OkHttpKtUtils.get("https://passport.weibo.com/visitor/visitor?a=incarnate&t=${tid.toUrlEncode()}&w=2&c=095&gc=&cb=cross_domain&from=weibo&_rand=${Math.random()}")
                .apply { close() }
        val cookie = OkUtils.cookie(response)
        val str = OkHttpKtUtils.getStr("https://s.weibo.com/top/summary", OkUtils.cookie(cookie))
        val doc = Jsoup.parse(str)
        val elements = doc.getElementById("pl_top_realtimehot")?.getElementsByTag("tbody")?.first()
            ?.getElementsByTag("tr") ?: return emptyList()
        val list = mutableListOf<HotSearch>()
        for (ele in elements) {
            val hotSearch = HotSearch()
            ele.getElementsByClass("td-01").first()?.text()?.toIntOrNull()
                ?.let { hotSearch.count = it }
            val a = ele.select(".td-02 a").first()
            a?.attr("href")?.let { hotSearch.url = "https://s.weibo.com${it}" }
            a?.text()?.let { hotSearch.content = it }
            ele.select(".td-02 span").first()?.text()?.toLongOrNull()?.let { hotSearch.heat = it }
            ele.select(".td-03").first()?.text()?.let { hotSearch.tag = it }
            list.add(hotSearch)
        }
        return list
    }

    suspend fun getIdByName(name: String, page: Int = 1): Result<List<WeiboPojo>> {
        val newName = name.toUrlEncode()
        val response = OkHttpKtUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D3%26q%3D$newName%26t%3D0&page_type=searchall&page=$page",
            OkUtils.referer("https://m.weibo.cn/search?containerid=100103type%3D1%26q%3D$newName"))
        return if (response.code == 200) {
            val jsonObject = OkUtils.json(response)
            val cardsJsonArray = jsonObject.getJSONObject("data").getJSONArray("cards")
            var jsonArray: JSONArray? = null
            for (obj in cardsJsonArray) {
                val singleJsonObject = obj as JSONObject
                val cardGroupJsonArray = singleJsonObject.getJSONArray("card_group")
                if (cardGroupJsonArray != null) {
                    jsonArray = cardGroupJsonArray
                    break
                }
            }
            if (jsonArray == null) return Result.failure("没有找到该用户")
            val list = mutableListOf<WeiboPojo>()
            for (obj in jsonArray) {
                val newJsonObject = obj as JSONObject
                if (newJsonObject.containsKey("user") || newJsonObject.containsKey("users")) {
                    val userJsonObject = newJsonObject.getJSONObject("user")
                    if (userJsonObject != null) {
                        val username = userJsonObject.getString("name") ?:
                            userJsonObject.getString("screen_name")
                        list.add(WeiboPojo(name = username, userid = userJsonObject.getString("id")))
                    } else {
                        val usersJsonArray = newJsonObject.getJSONArray("users")
                        for (any in usersJsonArray) {
                            val singleJsonObject = any as JSONObject
                            val username = singleJsonObject.getString("name")
                                ?: singleJsonObject.getString("screen_name")
                            list.add(WeiboPojo(name = username, userid = singleJsonObject.getString("id")))
                        }
                    }
                }
            }
            if (list.isEmpty()) Result.failure("未找到该用户")
            else Result.success(list)
        } else Result.failure("查询失败，请稍后再试！")
    }

    private fun convert(jsonObject: JSONObject): WeiboPojo {
        val weiboPojo = WeiboPojo()
        val userJsonObject = jsonObject.getJSONObject("user")
        weiboPojo.id = jsonObject.getLong("id")
        weiboPojo.name = userJsonObject.getString("screen_name")
        weiboPojo.created = jsonObject.getString("created_at")
        weiboPojo.text = Jsoup.parse(jsonObject.getString("text")).text()
        weiboPojo.bid = jsonObject.getString("bid")
        weiboPojo.userid = userJsonObject.getString("id")
        val picNum = jsonObject.getInteger("pic_num")
        if (picNum != 0) {
            val list = weiboPojo.imageUrl
            val pics = jsonObject["pics"]
            if (pics is JSONArray) {
                pics.map { it as JSONObject }.map { it.getJSONObject("large").getString("url")}.forEach {
                    it?.let { list.add(it) }
                }
            } else if (pics is JSONObject) {
                jsonObject.forEach { _, u ->
                    val ss = u as? JSONObject
                    ss?.getJSONObject("large")?.getString("url")?.let {
                        list.add(it)
                    }
                }
            }
        }
        if (jsonObject.containsKey("retweeted_status")) {
            val forwardJsonObject = jsonObject.getJSONObject("retweeted_status")
            weiboPojo.isForward = true
            weiboPojo.forwardId = forwardJsonObject.getString("id")
            weiboPojo.forwardTime = forwardJsonObject.getString("created_at")
            val forwardUserJsonObject = forwardJsonObject.getJSONObject("user")
            val name = if (forwardUserJsonObject == null)  "原微博删除"
            else forwardUserJsonObject.getString("screen_name")
            weiboPojo.forwardName = name
            weiboPojo.forwardText = Jsoup.parse(forwardJsonObject.getString("text")).text()
            weiboPojo.forwardBid = forwardJsonObject.getString("bid")
        }
        return weiboPojo
    }

    fun convert(weiboPojo: WeiboPojo): String {
        val sb = StringBuilder()
        sb.append("""
            ${weiboPojo.name}
            发布时间：${weiboPojo.created}
            内容：${weiboPojo.text}
            链接：https://m.weibo.cn/status/${weiboPojo.bid}
        """.trimIndent())
        if (weiboPojo.isForward) {
            sb.append("\n")
            sb.append("""
                转发自：${weiboPojo.forwardName}
                发布时间：${weiboPojo.forwardTime}
                内容：${weiboPojo.forwardText}
                链接：https://m.weibo.cn/status/${weiboPojo.forwardBid}
            """.trimIndent())
        }
        return sb.toString()
    }

    suspend fun getWeiboById(id: String): Result<List<WeiboPojo>> {
        val response = OkHttpKtUtils.get("https://m.weibo.cn/api/container/getIndex?type=uid&uid=$id&containerid=107603$id")
        return if (response.code == 200) {
            val jsonObject = OkUtils.json(response)
            val cardJsonArray = jsonObject.getJSONObject("data").getJSONArray("cards")
            val list = mutableListOf<WeiboPojo>()
            for (any in cardJsonArray) {
                val singleJsonObject = any as JSONObject
                val blogJsonObject = singleJsonObject.getJSONObject("mblog") ?: continue
                if (1 == blogJsonObject.getInteger("isTop")) continue
                list.add(convert(blogJsonObject))
            }
            Result.success(list)
        } else Result.failure("查询失败，请稍后重试！")
    }

    private suspend fun mobileCookie(pcCookie: String): String {
        val response = OkHttpKtUtils.get("https://login.sina.com.cn/sso/login.php?url=https%3A%2F%2Fm.weibo.cn%2F%3F%26jumpfrom%3Dweibocom&_rand=1588483688.7261&gateway=1&service=sinawap&entry=sinawap&useticket=1&returntype=META&sudaref=&_client_version=0.6.33",
            OkUtils.cookie(pcCookie)).apply { close() }
        return OkUtils.cookie(response)
    }

    suspend fun loginByQr1(): WeiboQrcode {
        val jsonObject = OkHttpKtUtils.getJsonp("https://login.sina.com.cn/sso/qrcode/image?entry=weibo&size=180&callback=STK_16010457545441",
            OkUtils.referer("https://weibo.com/"))
        val dataJsonObject = jsonObject.getJSONObject("data")
        return WeiboQrcode(dataJsonObject.getString("qrid"), dataJsonObject.getString("image"))
    }

    suspend fun loginByQr2(weiboQrcode: WeiboQrcode): Result<WeiboEntity> {
        val jsonObject = OkHttpKtUtils.getJsonp("https://login.sina.com.cn/sso/qrcode/check?entry=weibo&qrid=${weiboQrcode.id}&callback=STK_16010457545443",
            OkUtils.referer("https://weibo.com/"))
        return when (jsonObject.getInteger("retcode")) {
            20000000 -> {
                val dataJsonObject = jsonObject.getJSONObject("data")
                val alt = dataJsonObject.getString("alt")
                val response = OkHttpKtUtils.get("https://login.sina.com.cn/sso/login.php?entry=weibo&returntype=TEXT&crossdomain=1&cdult=3&domain=weibo.com&alt=$alt&savestate=30&callback=STK_160104719639113")
                val cookie = OkUtils.cookie(response)
                val resultJsonObject = OkUtils.jsonp(response)
                val jsonArray = resultJsonObject.getJSONArray("crossDomainUrlList")
                val url = jsonArray.getString(2)
                val finallyResponse = OkHttpKtUtils.get(url).apply { close() }
                val pcCookie = OkUtils.cookie(finallyResponse)
                val mobileCookie = mobileCookie(cookie)
                Result.success(WeiboEntity().also {
                    it.pcCookie = cookie
                    it.mobileCookie = mobileCookie
                })
            }
            50114001 -> Result.failure(201, "未扫码")
            50114003 -> Result.failure("您的微博登录二维码已失效")
            50114002 -> Result.failure(202, "已扫码")
            else -> Result.failure(jsonObject.getString("msg"), null)
        }
    }

    suspend fun friendWeibo(weiboEntity: WeiboEntity): Result<List<WeiboPojo>> {
        val str = OkHttpKtUtils.getStr("https://m.weibo.cn/feed/friends?",
            OkUtils.cookie(weiboEntity.mobileCookie))
        return if ("" != str) {
            val jsonArray = kotlin.runCatching {
                JSON.parseObject(str).getJSONObject("data").getJSONArray("statuses")
            }.onFailure {
                return Result.failure("查询微博失败，请稍后再试！！", null)
            }.getOrNull()!!
            val list = mutableListOf<WeiboPojo>()
            for (any in jsonArray) {
                val jsonObject = any as JSONObject
                list.add(convert(jsonObject))
            }
            Result.success(list)
        } else Result.failure("您的cookie已失效，请重新绑定微博")
    }

    suspend fun myWeibo(weiboEntity: WeiboEntity): Result<List<WeiboPojo>> {
        val jsonObject = OkHttpKtUtils.getJson("https://m.weibo.cn/profile/info",
            OkUtils.cookie(weiboEntity.mobileCookie))
        return if (jsonObject.getInteger("ok") == 1) {
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("statuses")
            val list = mutableListOf<WeiboPojo>()
            for (any in jsonArray) {
                val singleJsonObject = any as JSONObject
                list.add(convert(singleJsonObject))
            }
            Result.success(list)
        } else Result.failure("您的cookie已失效，请重新绑定微博")
    }

    private suspend fun getToken(weiboEntity: WeiboEntity): WeiboToken {
        val response = OkHttpKtUtils.get("https://m.weibo.cn/api/config",
            OkUtils.cookie(weiboEntity.mobileCookie))
        val jsonObject = OkUtils.json(response).getJSONObject("data")
        return if (jsonObject.getBoolean("login")) {
            val cookie = OkUtils.cookie(response)
            WeiboToken(jsonObject.getString("st"), cookie + weiboEntity.mobileCookie)
        } else throw WeiboCookieExpiredException("cookie已失效")
    }

    suspend fun superTalkSign(weiboEntity: WeiboEntity): Result<Void> {
        val weiboToken = getToken(weiboEntity)
        val response = OkHttpKtUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100803_-_followsuper&luicode=10000011&lfid=231093_-_chaohua",
            mapOf("cookie" to weiboToken.cookie, "x-xsrf-token" to weiboToken.token)
        )
        if (response.code != 200) return Result.failure(ResultStatus.COOKIE_EXPIRED)
        val cookie = OkUtils.cookie(response)
        val jsonObject = OkUtils.json(response)
        return if (jsonObject.getInteger("ok") == 1) {
            val cardsJsonArray = jsonObject.getJSONObject("data").getJSONArray("cards").getJSONObject(0).getJSONArray("card_group")
            for (any in cardsJsonArray) {
                val singleJsonObject = any as JSONObject
                if (singleJsonObject.containsKey("buttons")) {
                    val buttonJsonArray = singleJsonObject.getJSONArray("buttons")
                    for (bu in buttonJsonArray) {
                        val buttonJsonObject = bu as JSONObject
                        if (buttonJsonObject.getString("name") == "签到") {
                            val scheme = "https://m.weibo.cn${buttonJsonObject.getString("scheme")}"
                            val signJsonObject = OkHttpKtUtils.postJson(scheme,
                                mapOf("st" to weiboToken.token, "_spr" to "screen:393x851"),
                                mapOf("x-xsrf-token" to weiboToken.token, "cookie" to weiboToken.cookie + cookie,
                                    "referer" to "https://m.weibo.cn/p/tabbar?containerid=100803_-_followsuper&luicode=10000011&lfid=231093_-_chaohua&page_type=tabbar",
                                    "user-agent" to UA.PC.value, "mweibo-pwa" to "1")
                            )
                        }
                    }
                }
            }
            Result.success()
        } else Result.failure("获取关注超话列表失败")
    }

}

data class HotSearch(
    var count: Int = 0,
    var content: String = "",
    var heat: Long = 0,
    var tag: String = "",
    var url: String = ""
)

data class WeiboPojo(
    var id: Long = 0,
    var name: String = "",
    var userid: String = "",
    var created: String = "",
    var text: String = "",
    var bid: String = "",
    var imageUrl: MutableList<String> = mutableListOf(),
    var isForward: Boolean = false,
    var forwardId: String = "",
    var forwardTime: String = "",
    var forwardName: String = "",
    var forwardText: String = "",
    var forwardBid: String = ""
)

data class WeiboQrcode(
    var id: String = "",
    var url: String = ""
)

data class WeiboToken(
    var token: String = "",
    var cookie: String = ""
)

class WeiboCookieExpiredException(msg: String): RuntimeException(msg)
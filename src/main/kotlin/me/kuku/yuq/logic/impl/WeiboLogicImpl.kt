package me.kuku.yuq.logic.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.WeiboPojo
import me.kuku.yuq.utils.*
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

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

    override fun getIdByName(name: String): CommonResult<List<WeiboPojo>> {
        val response = OkHttpClientUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100103type=1%26q=$name&page_type=searchall")
        return if (response.code == 200){
            val jsonObject = OkHttpClientUtils.getJson(response)
            val jsonArray = jsonObject.getJSONObject("data")?.getJSONArray("cards")?.getJSONObject(0)
                    ?.getJSONArray("card_group") ?: return CommonResult(500, "没有找到该用户")
            val list = mutableListOf<WeiboPojo>()
            for (i in jsonArray.indices){
                val newJsonObject = jsonArray.getJSONObject(i)
                if (newJsonObject.containsKey("user") || newJsonObject.containsKey("users")) {
                    val userJsonObject = newJsonObject.getJSONObject("user")
                    if (userJsonObject != null) list.add(WeiboPojo(userJsonObject.getString("name") ?: userJsonObject.getString("screen_name"), userJsonObject.getString("id")))
                    else {
                        val usersJsonArray = newJsonObject.getJSONArray("users")
                        for (j in usersJsonArray.indices) {
                            val singleJsonObject = usersJsonArray.getJSONObject(j)
                            list.add(WeiboPojo(singleJsonObject.getString("name") ?: singleJsonObject.getString("screen_name"), singleJsonObject.getString("id")))
                        }
                    }
                }
            }
            if (list.size == 0) CommonResult(500, "未找到该用户")
            else CommonResult(200, "", list.toList())
        }else CommonResult(500, "查询失败，请稍后再试！！")
    }

    private fun convert(jsonObject: JSONObject): WeiboPojo{
        val weiboPojo = WeiboPojo()
        weiboPojo.id = jsonObject.getString("id")
        weiboPojo.name = jsonObject.getJSONObject("user").getString("screen_name")
        weiboPojo.created = jsonObject.getString("created_at")
        weiboPojo.text = Jsoup.parse(jsonObject.getString("text")).text()
        weiboPojo.bid = jsonObject.getString("bid")
        val picNum = jsonObject.getInteger("pic_num")
        if (picNum != 0){
            val list = mutableListOf<String>()
            val jsonArray = jsonObject.getJSONArray("pics")
            jsonArray.forEach {
                val picJsonObject = it as JSONObject
                val url = picJsonObject.getJSONObject("large").getString("url")
                list.add(url)
            }
        }
        return weiboPojo
    }

    override fun convertStr(weiboPojo: WeiboPojo): String{
        return """
            ${weiboPojo.name}
            发布时间：${weiboPojo.created}
            内容：${weiboPojo.text}
            链接：https://m.weibo.cn/status/${weiboPojo.bid}
        """.trimIndent()
    }

    override fun getWeiboById(id: String): CommonResult<List<WeiboPojo>> {
        val response = OkHttpClientUtils.get("https://m.weibo.cn/api/container/getIndex?type=uid&uid=$id&containerid=107603$id")
        return if (response.code == 200){
            val jsonObject = OkHttpClientUtils.getJson(response)
            val cardJsonArray = jsonObject.getJSONObject("data").getJSONArray("cards")
            val list = mutableListOf<WeiboPojo>()
            cardJsonArray.stream().skip(1).forEach {
                val singleJsonObject = it as JSONObject
                val blogJsonObject = singleJsonObject.getJSONObject("mblog")
                if (1 == blogJsonObject.getInteger("isTop")) return@forEach
                val weiboPojo = this.convert(blogJsonObject)
                list.add(weiboPojo)
            }
            CommonResult(200, "", list)
        }else CommonResult(500, "查询失败，请稍后再试！！")
    }

    private fun loginParams(username: String): MutableMap<String, String> {
        val response = OkHttpClientUtils.get("https://login.sina.com.cn/sso/prelogin.php?entry=weibo&callback=sinaSSOController.preloginCallBack&su=${URLEncoder.encode(username, "utf-8")}&rsakt=mod&checkpin=1&client=ssologin.js(v1.4.19)&_=${Date().time}")
        val jsonObject = OkHttpClientUtils.getJsonp(response)
        val map = mutableMapOf<String, String>()
        for ((k, v) in jsonObject){
            map[k] = v.toString()
        }
        map["cookie"] = OkHttpClientUtils.getCookie(response)
        map["username"] = username
        return map
    }

    private fun encryptPassword(map: Map<String, String>, password: String): String {
        val message = "${map["servertime"]}\t${map["nonce"]}\n$password"
        val newPassword = RSAUtils.encrypt(message, RSAUtils.getPublicKey(map["pubkey"], "10001"))
        val bytes = Base64.getDecoder().decode(newPassword)
        return HexUtils.bytesToHexString(bytes)
    }

    private fun login(map: MutableMap<String, String>, password: String): CommonResult<MutableMap<String, String>>{
        val newPassword = this.encryptPassword(map, password)
        val response = OkHttpClientUtils.post("https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.19)&_=${Date().time}", OkHttpClientUtils.addForms(
                "entry", "weibo",
                "gateway", "1",
                "from", "",
                "savestate", "7",
                "qrcode_flag", "false",
                "useticket", "1",
                "pagerefer", "https://passport.weibo.com/visitor/visitor?entry=miniblog&a=enter&url=https%3A%2F%2Fweibo.com%2F&domain=.weibo.com&ua=php-sso_sdk_client-0.6.36&_rand=1596261779.2657",
                "vnf", "1",
                "su", map.getValue("username"),
                "service", "miniblog",
                "servertime", map.getValue("servertime"),
                "nonce", map.getValue("nonce"),
                "pwencode", "rsa2",
                "rsakv", map.getValue("rsakv"),
                "sp", newPassword,
                "sr", "1536*864",
                "encoding", "UTF-8",
                "prelt", "55",
                "url", "https://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack",
                "returntype", "META"
        ), OkHttpClientUtils.addCookie(map.getValue("cookie")))
        val str = OkHttpClientUtils.getStr(response)
        val token = BotUtils.regex("token%3D", "\"", str)
        return if (token == null){
            val reason = BotUtils.regex("reason=", "&", str)
            val result = URLDecoder.decode(reason, "gbk")
            CommonResult(500, result)
        }else {
            val phoneResponse = OkHttpClientUtils.get("https://login.sina.com.cn/protection/index?token=$token&callback_url=https%3A%2F%2Fweibo.com")
            val html = OkHttpClientUtils.getStr(phoneResponse)
            val phone = Jsoup.parse(html).getElementById("ss0").attr("value")
            val smsResponse = OkHttpClientUtils.post("https://login.sina.com.cn/protection/mobile/sendcode?token=$token", OkHttpClientUtils.addForms(
                    "encrypt_mobile", phone
            ))
            val smsJsonObject = OkHttpClientUtils.getJson(smsResponse)
            if (smsJsonObject.getInteger("retcode") == 20000000) {
                map["token"] = token
                map["phone"] = phone
                CommonResult(200, "", map)
            }else CommonResult(500, smsJsonObject.getString("msg"))
        }
    }

    override fun login(username: String, password: String): CommonResult<MutableMap<String, String>> {
        val newUsername = Base64.getEncoder().encodeToString(username.toByteArray())
        val loginParams = this.loginParams(newUsername)
        return this.login(loginParams, password)
    }

    override fun loginBySms(token: String, phone: String, code: String): CommonResult<WeiboEntity> {
        val response = OkHttpClientUtils.post("https://login.sina.com.cn/protection/mobile/confirm?token=$token", OkHttpClientUtils.addForms(
                "encrypt_mobile", phone,
                "code", code
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("retcode")){
            20000000 -> {
                val url = jsonObject.getJSONObject("data").getString("redirect_url")
                val resultResponse = OkHttpClientUtils.get(url)
                resultResponse.close()
                val pcCookie = OkHttpClientUtils.getCookie(resultResponse)
                val mobileResponse = OkHttpClientUtils.get("https://login.sina.com.cn/sso/login.php?url=https%3A%2F%2Fm.weibo.cn%2F%3F%26jumpfrom%3Dweibocom&_rand=1588483688.7261&gateway=1&service=sinawap&entry=sinawap&useticket=1&returntype=META&sudaref=&_client_version=0.6.33",
                        OkHttpClientUtils.addCookie(pcCookie))
                mobileResponse.close()
                val mobileCookie = OkHttpClientUtils.getCookie(mobileResponse)
                CommonResult(200, "", WeiboEntity(pcCookie = pcCookie, mobileCookie = mobileCookie))
            }
            8518 -> CommonResult(402, "验证码错误或已经过期！！！")
            else -> CommonResult(500, jsonObject.getString("msg"))
        }
    }

    override fun getFriendWeibo(weiboEntity: WeiboEntity): CommonResult<List<WeiboPojo>> {
        val response = OkHttpClientUtils.get("https://m.weibo.cn/feed/friends?",
                OkHttpClientUtils.addCookie(weiboEntity.mobileCookie))
        val str = OkHttpClientUtils.getStr(response)
        return if ("" != str){
            val jsonArray = JSON.parseObject(str).getJSONObject("data").getJSONArray("statuses")
            val list = mutableListOf<WeiboPojo>()
            for (i in jsonArray.indices){
                val jsonObject = jsonArray.getJSONObject(i)
                val weiboPojo = this.convert(jsonObject)
                list.add(weiboPojo)
            }
            CommonResult(200, "", list)
        }else CommonResult(500, "您的cookie已失效，请重新绑定微博！！")
    }
}
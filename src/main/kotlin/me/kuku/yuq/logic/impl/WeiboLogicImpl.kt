package me.kuku.yuq.logic.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.WeiboPojo
import me.kuku.yuq.pojo.WeiboToken
import me.kuku.yuq.utils.*
import okhttp3.FormBody
import okhttp3.MultipartBody
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

class WeiboLogicImpl: WeiboLogic {
    override fun hotSearch(): List<String> {
        val doc = Jsoup.connect("https://s.weibo.com/top/summary").get()
        val elements = doc.getElementById("pl_top_realtimehot").getElementsByTag("tbody").first()
                .getElementsByTag("tr")
        val list = mutableListOf<String>()
        for (ele in elements){
            var text: String = ele.getElementsByClass("td-01").first().text()
            text = if (text == "") "顶" else text
            val title: String = ele.getElementsByClass("td-02").first().getElementsByTag("a").first().text()
            list.add("$text、$title")

        }
        return list
    }

    override fun getIdByName(name: String): CommonResult<List<WeiboPojo>> {
        val newName = URLEncoder.encode(name, "utf-8")
        val response = OkHttpClientUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D3%26q%3D$newName%26t%3D0&page_type=searchall",
                OkHttpClientUtils.addReferer("https://m.weibo.cn/search?containerid=100103type%3D1%26q%3D$newName"))
        return if (response.code == 200){
            val jsonObject = OkHttpClientUtils.getJson(response)
            val cardsJsonArray = jsonObject.getJSONObject("data")?.getJSONArray("cards") ?: return CommonResult(500, "搜索失败，请稍后再试！！")
            var jsonArray: JSONArray? = null
            for (i in cardsJsonArray.indices) {
                val cardGroupJsonArray = cardsJsonArray.getJSONObject(i).getJSONArray("card_group")
                if (cardGroupJsonArray != null) {
                    jsonArray = cardGroupJsonArray
                    break
                }
            }
            if (jsonArray == null) return CommonResult(500, "没有找到该用户")
            val list = mutableListOf<WeiboPojo>()
            for (i in jsonArray.indices){
                val newJsonObject = jsonArray.getJSONObject(i)
                if (newJsonObject.containsKey("user") || newJsonObject.containsKey("users")) {
                    val userJsonObject = newJsonObject.getJSONObject("user")
                    if (userJsonObject != null) list.add(WeiboPojo(name = userJsonObject.getString("name") ?: userJsonObject.getString("screen_name"), userId = userJsonObject.getString("id")))
                    else {
                        val usersJsonArray = newJsonObject.getJSONArray("users")
                        for (j in usersJsonArray.indices) {
                            val singleJsonObject = usersJsonArray.getJSONObject(j)
                            list.add(WeiboPojo(name = singleJsonObject.getString("name") ?: singleJsonObject.getString("screen_name"), userId = singleJsonObject.getString("id")))
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
        val userJsonObject = jsonObject.getJSONObject("user")
        weiboPojo.id = jsonObject.getString("id")
        weiboPojo.name = userJsonObject.getString("screen_name")
        weiboPojo.created = jsonObject.getString("created_at")
        weiboPojo.text = Jsoup.parse(jsonObject.getString("text")).text()
        weiboPojo.bid = jsonObject.getString("bid")
        weiboPojo.userId = userJsonObject.getString("id")
        val picNum = jsonObject.getInteger("pic_num")
        if (picNum != 0){
            val list = mutableListOf<String>()
            val jsonArray = jsonObject.getJSONArray("pics")
            jsonArray?.forEach {
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
        map["cookie"] = OkHttpClientUtils.getCookie(response) + "ULOGIN_IMG=${map["pcid"]}; "
        map["username"] = username
        return map
    }

    override fun getCaptchaUrl(pcId: String): String{
        return "https://login.sina.com.cn/cgi/pin.php?r=${BotUtils.randomNum(8)}&s=0&p=$pcId"
    }

    private fun encryptPassword(map: Map<String, String>, password: String): String {
        val message = "${map["servertime"]}\t${map["nonce"]}\n$password"
        val newPassword = RSAUtils.encrypt(message, RSAUtils.getPublicKey(map["pubkey"], "10001"))
        val bytes = Base64.getDecoder().decode(newPassword)
        return HexUtils.bytesToHexString(bytes)
    }

    private fun getMobileCookie(pcCookie: String): String{
        val response = OkHttpClientUtils.get("https://login.sina.com.cn/sso/login.php?url=https%3A%2F%2Fm.weibo.cn%2F%3F%26jumpfrom%3Dweibocom&_rand=1588483688.7261&gateway=1&service=sinawap&entry=sinawap&useticket=1&returntype=META&sudaref=&_client_version=0.6.33",
                OkHttpClientUtils.addCookie(pcCookie))
        response.close()
        return OkHttpClientUtils.getCookie(response)
    }

    override fun login(map: MutableMap<String, String>, door: String?): CommonResult<MutableMap<String, String>>{
        val newPassword = this.encryptPassword(map, map.getValue("password"))
        val response = OkHttpClientUtils.post("https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.19)&_=${Date().time}", OkHttpClientUtils.addForms(
                "entry", "weibo",
                "gateway", "1",
                "from", "",
                "savestate", "7",
                "qrcode_flag", "false",
                "useticket", "1",
                "pagerefer", "https://passport.weibo.com/visitor/visitor?entry=miniblog&a=enter&url=https%3A%2F%2Fweibo.com%2F&domain=.weibo.com&ua=php-sso_sdk_client-0.6.36&_rand=1596261779.2657",
                "pcid", if (door != null) map.getValue("pcid") else "",
                "door", door ?: "",
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
        val html = OkHttpClientUtils.getStr(response)
        val url = BotUtils.regex("location.replace\\(\"", "\"\\);", html) ?: return CommonResult(500, "获取失败！！")
        val token = BotUtils.regex("token%3D", "\"", html)
        val cookie = OkHttpClientUtils.getCookie(response)
        map["cookie"] = cookie
        return if (url.contains("https://login.sina.com.cn/crossdomain2.php")){
            map["url"] = url
            map["referer"] = "https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.19)"
            CommonResult(200, "登录成功", map)
        }
        else if (token == null){
            val reason = BotUtils.regex("reason=", "&", html)
            val result = URLDecoder.decode(reason, "gbk")
            CommonResult(500, result)
        }else {
            val phoneResponse = OkHttpClientUtils.get("https://login.sina.com.cn/protection/index?token=$token&callback_url=https%3A%2F%2Fweibo.com")
            val phoneHtml = OkHttpClientUtils.getStr(phoneResponse)
            val phone = Jsoup.parse(phoneHtml).getElementById("ss0").attr("value")
            val smsResponse = OkHttpClientUtils.post("https://login.sina.com.cn/protection/mobile/sendcode?token=$token", OkHttpClientUtils.addForms(
                    "encrypt_mobile", phone
            ))
            val smsJsonObject = OkHttpClientUtils.getJson(smsResponse)
            if (smsJsonObject.getInteger("retcode") == 20000000) {
                map["token"] = token
                map["phone"] = phone
                CommonResult(201, "请输入短信验证码！！", map)
            }else CommonResult(500, smsJsonObject.getString("msg"))
        }
    }

    override fun loginSuccess(cookie: String, referer: String, url: String): WeiboEntity{
        val response = OkHttpClientUtils.get(url, OkHttpClientUtils.addHeaders(
                "cookie", cookie,
                "referer", referer
        ))
        val html = OkHttpClientUtils.getStr(response)
        val jsonStr = BotUtils.regex("sinaSSOController.setCrossDomainUrlList\\(", "\\);", html)
        val urlJsonObject = JSON.parseObject(jsonStr)
        val pcUrl = urlJsonObject.getJSONArray("arrURL").getString(0)
        val pcResponse = OkHttpClientUtils.get("$pcUrl&callback=sinaSSOController.doCrossDomainCallBack&scriptId=ssoscript0&client=ssologin.js(v1.4.19)&_=${Date().time}")
        pcResponse.close()
        val pcCookie = OkHttpClientUtils.getCookie(pcResponse)
        val mobileCookie = this.getMobileCookie(cookie)
        return WeiboEntity(pcCookie = pcCookie, mobileCookie = mobileCookie)
    }

    override fun preparedLogin(username: String, password: String): CommonResult<MutableMap<String, String>> {
        val newUsername = Base64.getEncoder().encodeToString(username.toByteArray())
        val loginParams = this.loginParams(newUsername)
        loginParams["password"] = password
        return if (loginParams["showpin"] == "0") CommonResult(200, "不需要验证码", loginParams)
        else CommonResult(201, "需要验证码", loginParams)
    }

    override fun loginBySms(token: String, phone: String, code: String): CommonResult<WeiboEntity> {
        val refererUrl = "https://login.sina.com.cn/protection/mobile/confirm?token=$token"
        val response = OkHttpClientUtils.post(refererUrl, OkHttpClientUtils.addForms(
                "encrypt_mobile", phone,
                "code", code
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("retcode")){
            20000000 -> {
                val url = jsonObject.getJSONObject("data").getString("redirect_url")
                val resultResponse = OkHttpClientUtils.get(url, OkHttpClientUtils.addReferer(refererUrl))
                val cookie = OkHttpClientUtils.getCookie(resultResponse)
                val html = OkHttpClientUtils.getStr(resultResponse)
                val secondUrl = BotUtils.regex("location.replace\\(\"", "\"\\);", html) ?: return CommonResult(500, "登录失败，请稍后再试！！")
                CommonResult(200, "", this.loginSuccess(cookie, url, secondUrl))
            }
            8518 -> CommonResult(402, "验证码错误或已经过期！！！")
            else -> CommonResult(500, jsonObject.getString("msg"))
        }
    }

    override fun loginByQQ(qqEntity: QQEntity): CommonResult<WeiboEntity> {
        val startWeiboResponse = OkHttpClientUtils.get("https://passport.weibo.com/othersitebind/authorize?entry=miniblog&site=qq")
        startWeiboResponse.close()
        val weiboCookie = OkHttpClientUtils.getCookie(startWeiboResponse)
        val code = BotUtils.regex("crossidccode=", ";", weiboCookie)
        val startUrl = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=101019034&daid=383&pt_skey_valid=0&style=35&s_url=http%3A%2F%2Fconnect.qq.com&refer_cgi=authorize&which=&client_id=101019034&response_type=code&scope=get_info%2Cget_user_info&redirect_uri=https%3A%2F%2Fpassport.weibo.com%2Fothersitebind%2Fbind%3Fsite%3Dqq%26state%3D$code%26bentry%3Dminiblog%26wl%3D&display="
        val startResponse = OkHttpClientUtils.get(startUrl)
        startResponse.close()
        val cookie = OkHttpClientUtils.getCookie(startResponse)
        val response = OkHttpClientUtils.get("https://ssl.ptlogin2.qq.com/pt_open_login?openlogin_data=which%3D%26refer_cgi%3Dauthorize%26response_type%3Dcode%26client_id%3D101019034%26state%3D%26display%3D%26openapi%3D%2523%26switch%3D0%26src%3D1%26sdkv%3D%26sdkp%3Da%26tid%3D1597734121%26pf%3D%26need_pay%3D0%26browser%3D0%26browser_error%3D%26serial%3D%26token_key%3D%26redirect_uri%3Dhttps%253A%252F%252Fpassport.weibo.com%252Fothersitebind%252Fbind%253Fsite%253Dqq%2526state%253D$code%2526bentry%253Dminiblog%2526wl%253D%26sign%3D%26time%3D%26status_version%3D%26status_os%3D%26status_machine%3D%26page_type%3D1%26has_auth%3D0%26update_auth%3D0%26auth_time%3D${Date().time}&auth_token=${QQUtils.getToken2(qqEntity.superToken)}&pt_vcode_v1=0&pt_verifysession_v1=&verifycode=&u=${qqEntity.qq}&pt_randsalt=0&ptlang=2052&low_login_enable=0&u1=http%3A%2F%2Fconnect.qq.com&from_ui=1&fp=loginerroralert&device=2&aid=716027609&daid=383&pt_3rd_aid=101019034&ptredirect=1&h=1&g=1&pt_uistyle=35&regmaster=&", OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookieWithSuper() + cookie,
                "referer", startUrl
        ))
        val str = OkHttpClientUtils.getStr(response)
        val commonResult = QQUtils.getResultUrl(str)
        val url = commonResult.t ?: return CommonResult(500, commonResult.msg)
        val secondResponse = OkHttpClientUtils.get(url, OkHttpClientUtils.addHeaders(
                "cookie", weiboCookie,
                "referer", startUrl
        ))
        secondResponse.close()
        val refererUrl = secondResponse.header("location") ?: return CommonResult(500, "登录失败，请稍后再试！！")
        val thirdResponse = OkHttpClientUtils.get(refererUrl)
        val thirdHtml = OkHttpClientUtils.getStr(thirdResponse)
        val cnCookie = OkHttpClientUtils.getCookie(thirdResponse)
        val secondUrl = BotUtils.regex("location.replace\\(\"", "\"\\);", thirdHtml) ?: return CommonResult(500, "登录失败，请稍后再试！！")
        return CommonResult(200, "", this.loginSuccess(cnCookie, refererUrl, secondUrl))
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

    override fun getMyWeibo(weiboEntity: WeiboEntity): CommonResult<List<WeiboPojo>> {
        val response = OkHttpClientUtils.get("https://m.weibo.cn/profile/info",
                OkHttpClientUtils.addCookie(weiboEntity.mobileCookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ok") == 1){
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("statuses")
            val list = mutableListOf<WeiboPojo>()
            jsonArray.forEach {
                val singleJsonObject = it as JSONObject
                list.add(this.convert(singleJsonObject))
            }
            if (list.size == 0) CommonResult(500, "没有发现微博！！")
            else CommonResult(200, "", list.toList())
        }else CommonResult(500, jsonObject.getString("msg"))
    }

    override fun weiboTopic(keyword: String): CommonResult<List<WeiboPojo>> {
        val response = OkHttpClientUtils.get("https://m.weibo.cn/api/container/getIndex?containerid=100103type%3D1%26q%3D%23${URLEncoder.encode(keyword, "utf-8")}%23&page_type=searchall")
        if (response.code != 200) return CommonResult(500, "查询失败，请稍后再试！！")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val jsonArray = jsonObject.getJSONObject("data").getJSONArray("cards")
        val list = mutableListOf<WeiboPojo>()
        for (i in jsonArray.indices){
            val singleJsonObject = jsonArray.getJSONObject(i)
            val mBlogJsonObject = singleJsonObject.getJSONObject("mblog")
            if (mBlogJsonObject != null) list.add(this.convert(mBlogJsonObject))
        }
        return if (list.size == 0) CommonResult(500, "没有找到该话题")
        else CommonResult(200, "", list)
    }

    private fun getToken(weiboEntity: WeiboEntity): CommonResult<WeiboToken>{
        val response = OkHttpClientUtils.get("https://m.weibo.cn/api/config",
                OkHttpClientUtils.addCookie(weiboEntity.mobileCookie))
        val jsonObject = OkHttpClientUtils.getJson(response).getJSONObject("data")
        return if (jsonObject.getBoolean("login")) {
            val cookie = OkHttpClientUtils.getCookie(response)
            CommonResult(200, "", WeiboToken(
                    jsonObject.getString("st"), cookie + weiboEntity.mobileCookie
            ))
        }else CommonResult(500, "登录已失效")
    }

    override fun like(weiboEntity: WeiboEntity, id: String): String {
        val weiboToken = this.getToken(weiboEntity).t ?: return "登录已失效！！"
        val response = OkHttpClientUtils.post("https://m.weibo.cn/api/attitudes/create", OkHttpClientUtils.addForms(
                "id", id,
                "attitude", "heart",
                "st", weiboToken.token,
                "_spr", "screen:1536x864"
        ), OkHttpClientUtils.addHeaders(
                "cookie", "${weiboEntity.mobileCookie}${weiboToken.cookie}",
                "referer", "https://m.weibo.cn/detail/$id"
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return jsonObject.getString("msg")
    }

    override fun comment(weiboEntity: WeiboEntity, id: String, commentContent: String): String {
        val weiboToken = this.getToken(weiboEntity).t ?: return "登录已失效！！"
        val response = OkHttpClientUtils.post("https://m.weibo.cn/api/comments/create", OkHttpClientUtils.addForms(
                "content", commentContent,
                "mid", id,
                "st", weiboToken.token,
                "_spr", "screen:411x731"
        ), OkHttpClientUtils.addHeaders(
                "cookie", weiboToken.cookie,
                "referer", "https://m.weibo.cn/detail/$id"
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ok") == 1) "评论成功"
        else jsonObject.getString("msg")
    }

    private fun uploadPic(picUrl: String, referer: String, weiboToken: WeiboToken): String?{
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("type", "json")
                .addFormDataPart("pic", "pic.jpg", OkHttpClientUtils.addStream(picUrl))
                .addFormDataPart("st", weiboToken.token)
                .addFormDataPart("_spr", "screen:411x731").build()
        val response = OkHttpClientUtils.post("https://m.weibo.cn/api/statuses/uploadPic", body, OkHttpClientUtils.addHeaders(
                "cookie", weiboToken.cookie,
                "referer", referer
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return jsonObject.getString("pic_id")
    }

    override fun forward(weiboEntity: WeiboEntity, id: String, content: String, picUrl: String?): String {
        val weiboToken = this.getToken(weiboEntity).t ?: return "登录已失效！！"
        var picId: String? = null
        if (picUrl != null){
            picId = this.uploadPic(picUrl, "https://m.weibo.cn/compose/repost?id=$id", weiboToken)
        }
        val builder = FormBody.Builder()
                .add("id", id)
                .add("content", content)
                .add("mid", id)
                .add("st", weiboToken.token)
                .add("_spr", "screen:411x731")
        if (picId != null) builder.add("picId", picId)
        val response = OkHttpClientUtils.post("https://m.weibo.cn/api/statuses/repost", builder.build(), OkHttpClientUtils.addHeaders(
                "cookie", weiboToken.cookie,
                "referer", "https://m.weibo.cn/compose/repost?id=$id&pids=${picId ?: ""}"
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ok") == 1) "转发微博成功！！"
        else jsonObject.getString("msg")
    }

    override fun getUserInfo(id: String): String {
        val response = OkHttpClientUtils.get("https://m.weibo.cn/api/container/getIndex?uid=$id&luicode=10000011&lfid=100103type%3D1&containerid=100505$id")
        return if (response.code == 200){
            val jsonObject = OkHttpClientUtils.getJson(response)
            val userInfoJsonObject = jsonObject.getJSONObject("data").getJSONObject("userInfo")
            val sb = StringBuilder()
            sb.appendln("id：${userInfoJsonObject.getString("id")}")
                    .appendln("昵称：${userInfoJsonObject.getString("screen_name")}")
                    .appendln("关注：${userInfoJsonObject.getString("follow_count")}")
                    .appendln("粉丝：${userInfoJsonObject.getString("followers_count")}")
                    .appendln("微博会员：${userInfoJsonObject.getString("mbrank")}级")
                    .appendln("微博认证：${userInfoJsonObject.getString("verified_reason")}")
                    .appendln("描述：${userInfoJsonObject.getString("description")}")
                    .append("主页：https://m.weibo.cn/u/${userInfoJsonObject.getString("id")}")
            sb.removeSuffixLine().toString()
        }else "查询失败，请稍后再试！！！"
    }

    override fun publishWeibo(weiboEntity: WeiboEntity, content: String, url: List<String>?): String {
        val commonResult = this.getToken(weiboEntity)
        val weiboToken = commonResult.t ?: return commonResult.msg
        val picIds = StringBuilder()
        url?.forEach {
            val id = this.uploadPic(it, "https://m.weibo.cn/compose/", weiboToken)
            picIds.append("$id,")
        }
        val builder = FormBody.Builder()
                .add("content", content)
                .add("st", weiboToken.token)
                .add("_spr", "screen:411x731")
        if (picIds.isNotEmpty()){
            builder.add("picId", picIds.removeSuffix(",").toString())
        }
        val response = OkHttpClientUtils.post("https://m.weibo.cn/api/statuses/update", builder.build(), OkHttpClientUtils.addHeaders(
                "cookie", weiboToken.cookie,
                "referer", "https://m.weibo.cn/compose/?pids=${picIds.removeSuffix(",")}"
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ok") == 1) "发布微博成功！！"
        else jsonObject.getString("msg")
    }

    override fun removeWeibo(weiboEntity: WeiboEntity, id: String): String {
        val commonResult = this.getToken(weiboEntity)
        val weiboToken = commonResult.t ?: return commonResult.msg
        val response = OkHttpClientUtils.post("https://m.weibo.cn/profile/delMyblog", OkHttpClientUtils.addForms(
                "mid", id,
                "st", weiboToken.token,
                "_spr", "screen:411x731"
        ), OkHttpClientUtils.addHeaders(
                "cookie", weiboToken.cookie,
                "referer", "https://m.weibo.cn/profile/"
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ok") == 1) "删除微博成功！！"
        else jsonObject.getString("msg")
    }

    override fun favoritesWeibo(weiboEntity: WeiboEntity, id: String): String {
        val commonResult = this.getToken(weiboEntity)
        val weiboToken = commonResult.t ?: return commonResult.msg
        val response = OkHttpClientUtils.post("https://m.weibo.cn/mblogDeal/addFavMblog", OkHttpClientUtils.addForms(
                "id", id,
                "st", weiboToken.token,
                "_spr", "screen:411x731"
        ), OkHttpClientUtils.addHeaders(
                "cookie", weiboToken.cookie,
                "referer", "https://m.weibo.cn/"
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return jsonObject.getString("msg")
    }
}
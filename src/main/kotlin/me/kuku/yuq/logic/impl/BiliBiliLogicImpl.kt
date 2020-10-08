package me.kuku.yuq.logic.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.pojo.BiliBiliPojo
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.QQUtils
import me.kuku.yuq.utils.removeSuffixLine
import okhttp3.MultipartBody
import okhttp3.Response
import okio.ByteString
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class BiliBiliLogicImpl: BiliBiliLogic {

    override fun getIdByName(username: String): CommonResult<List<BiliBiliPojo>> {
        val enUserName = URLEncoder.encode(username, "utf-8")
        val response = OkHttpClientUtils.get("https://api.bilibili.com/x/web-interface/search/type?context=&search_type=bili_user&page=1&order=&keyword=$enUserName&category_id=&user_type=&order_sort=&changing=mid&__refresh__=true&_extra=&highlight=1&single_column=0&jsonp=jsonp&callback=__jp2",
                OkHttpClientUtils.addReferer("https://search.bilibili.com/topic?keyword=enUserName"))
        val jsonObject = OkHttpClientUtils.getJsonp(response)
        val dataJsonObject = jsonObject.getJSONObject("data")
        // pagesize  页大小    numResults 总大小  numPages  页均
        return if (dataJsonObject.getInteger("numResults") != 0){
            val jsonArray = dataJsonObject.getJSONArray("result")
            val list = mutableListOf<BiliBiliPojo>()
            jsonArray.forEach {
                val singleJsonObject = it as JSONObject
                list.add(BiliBiliPojo(
                        singleJsonObject.getString("mid"),
                        singleJsonObject.getString("uname")
                ))
            }
            CommonResult(200, "", list)
        }else CommonResult(500, "没有找到该用户！！")
    }

    private fun convert(jsonObject: JSONObject): BiliBiliPojo{
        val biliBiliPojo = BiliBiliPojo()
        val descJsonObject = jsonObject.getJSONObject("desc")
        val infoJsonObject = descJsonObject.getJSONObject("user_profile").getJSONObject("info")
        val forwardJsonObject = descJsonObject.getJSONObject("origin")
        biliBiliPojo.userId = infoJsonObject.getString("uid")
        biliBiliPojo.name = infoJsonObject.getString("uname")
        biliBiliPojo.id = descJsonObject.getString("dynamic_id")
        biliBiliPojo.rid = descJsonObject.getString("rid")
        biliBiliPojo.time = (descJsonObject.getString("timestamp") + "000").toLong()
        biliBiliPojo.bvId = descJsonObject.getString("bvid")
        biliBiliPojo.isForward = forwardJsonObject != null
        biliBiliPojo.forwardBvId = forwardJsonObject?.getString("bvid")
        val forwardTime = forwardJsonObject?.getLong("timestamp")
        if (forwardTime != null)
            biliBiliPojo.forwardTime = (forwardTime.toString() + "000").toLong()
        biliBiliPojo.forwardId = forwardJsonObject?.getString("dynamic_id")
        val cardStr = jsonObject.getString("card")
        val cardJsonObject = JSON.parseObject(cardStr)
        val itemJsonObject = cardJsonObject?.getJSONObject("item")
        biliBiliPojo.text = cardJsonObject.getString("dynamic") ?:
                itemJsonObject?.getString("description") ?:
                itemJsonObject?.getString("content") ?:
                cardJsonObject?.getJSONObject("vest")?.getString("content") ?:
                if (cardJsonObject.containsKey("title")) "${cardJsonObject.getString("title")}------${cardJsonObject.getString("summary") }" else null ?:
                "没有发现内容！！"
        val picJsonArray = itemJsonObject?.getJSONArray("pictures")
        val picList = biliBiliPojo.picList
        picJsonArray?.forEach {
            val picJsonObject = it as JSONObject
            picList.add(picJsonObject.getString("img_src"))
        }
        val originStr = cardJsonObject.getString("origin")
        if (originStr != null){
            val forwardContentJsonObject = JSON.parseObject(originStr)
            if (forwardContentJsonObject.containsKey("item")){
                val forwardItemJsonObject = forwardContentJsonObject.getJSONObject("item")
                biliBiliPojo.forwardText = forwardItemJsonObject.getString("description") ?: forwardItemJsonObject.getString("content")
                val forwardPicJsonArray = forwardItemJsonObject.getJSONArray("pictures")
                val forwardPicList = biliBiliPojo.forwardPicList
                forwardPicJsonArray?.forEach {
                    val picJsonObject = it as JSONObject
                    forwardPicList.add(picJsonObject.getString("img_src"))
                }
                val forwardUserJsonObject = forwardContentJsonObject.getJSONObject("user")
                biliBiliPojo.forwardUserId = forwardUserJsonObject.getString("uid")
                biliBiliPojo.forwardName = forwardUserJsonObject.getString("name") ?: forwardUserJsonObject.getString("uname")
            }else {
                biliBiliPojo.forwardText = forwardContentJsonObject.getString("dynamic")
                val forwardOwnerJsonObject = forwardContentJsonObject.getJSONObject("owner")
                if (forwardOwnerJsonObject != null) {
                    biliBiliPojo.forwardUserId = forwardOwnerJsonObject.getString("mid")
                    biliBiliPojo.forwardName = forwardOwnerJsonObject.getString("name")
                }else{
                    biliBiliPojo.forwardName = forwardContentJsonObject.getString("uname")
                    biliBiliPojo.forwardUserId = forwardContentJsonObject.getString("uid")
                    biliBiliPojo.forwardText = forwardContentJsonObject.getString("title")
                }
            }
        }
        biliBiliPojo.type = if (biliBiliPojo.bvId == null){
            if (biliBiliPojo.picList.size == 0) 17
            else 11
        }else 1
        return biliBiliPojo
    }

    override fun convertStr(biliBiliPojo: BiliBiliPojo): String{
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val bvId = biliBiliPojo.bvId
        val forwardBvId = biliBiliPojo.forwardBvId
        val sb = StringBuilder()
                .appendln(biliBiliPojo.name)
                .appendln("发布时间：${sdf.format(Date(biliBiliPojo.time))}")
                .appendln("内容：${biliBiliPojo.text}")
                .appendln("动态链接：https://t.bilibili.com/${biliBiliPojo.id}")
                .appendln("视频链接：${if (bvId != null) "https://www.bilibili.com/video/$bvId" else "没有发现视频"}")
        if (biliBiliPojo.isForward)
            sb.appendln("转发自：${biliBiliPojo.forwardName}")
                .appendln("发布时间：${sdf.format(Date(biliBiliPojo.forwardTime!!))}")
                .appendln("内容：${biliBiliPojo.forwardText}")
                .appendln("动态链接：https://t.bilibili.com/${biliBiliPojo.forwardId}")
                .append("视频链接：${if (forwardBvId != null) "https://www.bilibili.com/video/$forwardBvId" else "没有发现视频"}")
        return sb.removeSuffixLine().toString()
    }

    private fun getDynamicById(id: String, offsetId: String): CommonResult<List<BiliBiliPojo>> {
        val response = OkHttpClientUtils.get("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=$id&offset_dynamic_id=$offsetId&need_top=1",
                OkHttpClientUtils.addReferer("https://space.bilibili.com/$id/dynamic"))
        val jsonObject = OkHttpClientUtils.getJson(response)
        // next_offset  下一页开头
        val dataJsonObject = jsonObject.getJSONObject("data")
        val jsonArray = dataJsonObject.getJSONArray("cards") ?: return CommonResult(500, "该用户并没有动态哦！！")
        val list = mutableListOf<BiliBiliPojo>()
        for (i in jsonArray.indices){
            val singleJsonObject = jsonArray.getJSONObject(i)
            if (singleJsonObject.getJSONObject("extra")?.getInteger("is_space_top") == 1) continue
            list.add(this.convert(singleJsonObject))
        }
        return CommonResult(200, dataJsonObject.getString("next_offset"), list)
    }

    override fun getDynamicById(id: String) = this.getDynamicById(id, "0")

    override fun getAllDynamicById(id: String): List<BiliBiliPojo> {
        var offsetId = "0"
        val allList = mutableListOf<BiliBiliPojo>()
        while (true){
            val commonResult = this.getDynamicById(id, offsetId)
            if (commonResult.code == 200){
                val list = commonResult.t!!
                allList.addAll(list)
                offsetId = commonResult.msg
            }else return allList
        }
    }

    override fun loginByQQ(qqEntity: QQEntity): CommonResult<BiliBiliEntity> {
        val dfcResponse = OkHttpClientUtils.post("https://passport.bilibili.com/captcha/dfc")
        val dfcToken = OkHttpClientUtils.getJson(dfcResponse).getJSONObject("data").getString("dfc")
        val dfcCookie = OkHttpClientUtils.getCookie(dfcResponse)
        val loginUrlResponse = OkHttpClientUtils.post("https://passport.bilibili.com/login/qq", OkHttpClientUtils.addForms(
                "gourl", "",
                "csrf", dfcToken
        ), OkHttpClientUtils.addCookie(dfcCookie))
        val loginUrlJsonObject = OkHttpClientUtils.getJson(loginUrlResponse)
        val loginUrl = loginUrlJsonObject.getString("data")
        val state = BotUtils.regex("state%3D", "&", loginUrl) ?: return CommonResult(500, "登录失败，请稍后再试！！")
        val loginFirstResponse = OkHttpClientUtils.get("https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=101135748&daid=383&pt_skey_valid=1&style=35&s_url=http%3A%2F%2Fconnect.qq.com&refer_cgi=authorize&which=&response_type=code&state=authorize&client_id=101135748&redirect_uri=https%3A%2F%2Fpassport.bilibili.com%2Flogin%2Fsnsback%3Fsns%3Dqq%26%26state%3D$state&scope=do_like,get_user_info,get_simple_userinfo,get_vip_info,get_vip_rich_info,add_one_blog,list_album,upload_pic,add_album,list_photo,get_info,add_t,del_t,add_pic_t,get_repost_list,get_other_info,get_fanslist,get_idollist,add_idol,del_idol,get_tenpay_addr")
        loginFirstResponse.close()
        val qqLoginCookie = OkHttpClientUtils.getCookie(loginFirstResponse)
        val superLoginUrl = "https://ssl.ptlogin2.qq.com/pt_open_login?openlogin_data=which%3D%26refer_cgi%3Dauthorize%26response_type%3Dcode%26client_id%3D101135748%26state%3Dauthorize%26display%3D%26openapi%3D%2523%26switch%3D0%26src%3D1%26sdkv%3D%26sdkp%3Da%26tid%3D1598024545%26pf%3D%26need_pay%3D0%26browser%3D0%26browser_error%3D%26serial%3D%26token_key%3D%26redirect_uri%3Dhttps%253A%252F%252Fpassport.bilibili.com%252Flogin%252Fsnsback%253Fsns%253Dqq%2526%2526state%253D$state%26sign%3D%26time%3D%26status_version%3D%26status_os%3D%26status_machine%3D%26page_type%3D1%26has_auth%3D0%26update_auth%3D0%26auth_time%3D${Date().time}&auth_token=${QQUtils.getToken2(qqEntity.superToken)}&pt_vcode_v1=0&pt_verifysession_v1=&verifycode=&u=${qqEntity.qq}&pt_randsalt=0&ptlang=2052&low_login_enable=0&u1=http%3A%2F%2Fconnect.qq.com&from_ui=1&fp=loginerroralert&device=2&aid=716027609&daid=383&pt_3rd_aid=101135748&ptredirect=1&h=1&g=1&pt_uistyle=35&regmaster=&"
        val loginResponse = OkHttpClientUtils.get(superLoginUrl,
                OkHttpClientUtils.addCookie(qqEntity.getCookieWithSuper() + qqLoginCookie))
        val str = OkHttpClientUtils.getStr(loginResponse)
        val commonResult = QQUtils.getResultUrl(str)
        val url = commonResult.t ?: return CommonResult(500, commonResult.msg)
        val biliBiliLoginResponse = OkHttpClientUtils.get(url, OkHttpClientUtils.addHeaders(
                "cookie", dfcCookie,
                "referer", superLoginUrl
        ))
        val biliBiliEntity = this.getBiliBiliEntityByResponse(biliBiliLoginResponse)
        return if (biliBiliEntity == null) CommonResult(500, "您可能没有使用哔哩哔哩绑定您的QQ账号，请绑定后再试！！")
        else CommonResult(200, "登录成功", biliBiliEntity)
    }

    override fun loginByWeibo(weiboEntity: WeiboEntity): CommonResult<BiliBiliEntity> {
        val dfcResponse = OkHttpClientUtils.post("https://passport.bilibili.com/captcha/dfc")
        val dfcToken = OkHttpClientUtils.getJson(dfcResponse).getJSONObject("data").getString("dfc")
        val dfcCookie = OkHttpClientUtils.getCookie(dfcResponse)
        val loginUrlResponse = OkHttpClientUtils.post("https://passport.bilibili.com/login/weibo", OkHttpClientUtils.addForms(
                "gourl", "https://m.bilibili.com/",
                "csrf", dfcToken
        ), OkHttpClientUtils.addHeaders(
                "cookie", dfcCookie,
                "user-agent", OkHttpClientUtils.MOBILE_UA
        ))
        val loginUrl = OkHttpClientUtils.getJson(loginUrlResponse).getString("data")
        val beforeLoginResponse = OkHttpClientUtils.get(loginUrl, OkHttpClientUtils.addHeaders(
                "referer", "https://passport.bilibili.com/login",
                "cookie", weiboEntity.mobileCookie,
                "user-agent", OkHttpClientUtils.MOBILE_UA
        ))
        val html = OkHttpClientUtils.getStr(beforeLoginResponse)
        val doc = Jsoup.parse(html)
        val uid = doc.select("input[name=uid]")?.first()?.attr("value")?: return CommonResult(500, "微博的cookie已失效，请重新绑定微博！！")
        val callback = doc.select("input[name=regCallback]").first().attr("value")
        val clientId = doc.select("input[name=client_id]").first().attr("value")
        val appKey = doc.select("input[name=appkey62]").first().attr("value")
        val verifyToken = doc.select("input[name=verifyToken]").first().attr("value")
        val response = OkHttpClientUtils.post("https://api.weibo.com/oauth2/authorize", OkHttpClientUtils.addForms(
                "display", "mobile",
                "action", "scope",
                "scope", "email",
                "ticket", "",
                "login", "authorize",
                "isLoginSina", "",
                "withOfficalFlag", "0",
                "response_type", "code",
                "regCallback", callback,
                "redirect_uri", "https://passport.bilibili.com/login/snsback?sns=weibo&state=${BotUtils.regex("state%3D", "&", loginUrl)}",
                "client_id", clientId,
                "appkey62", appKey,
                "state", "",
                "from", "",
                "offcialMobile", "null",
                "uid", uid,
                "url", "",
                "verifyToken", verifyToken,
                "version", "",
                "sso_type", ""
        ), OkHttpClientUtils.addHeaders(
                "Referer", loginUrl,
                "cookie", weiboEntity.pcCookie
        ))
        response.close()
        val url = response.header("location") ?: return CommonResult(500, "登录失败，请稍后再试！！")
        val resultResponse = OkHttpClientUtils.get(url, OkHttpClientUtils.addHeaders(
                "cookie", dfcCookie,
                "referer", loginUrl
        ))
        val biliBiliEntity = this.getBiliBiliEntityByResponse(resultResponse)
        return if (biliBiliEntity == null) CommonResult(500, "您可能没有使用哔哩哔哩绑定您的微博账号，请绑定后重试！！")
        else CommonResult(200, "登录成功", biliBiliEntity)
    }

    override fun loginByQr1(): String {
        val response = OkHttpClientUtils.get("https://passport.bilibili.com/qrcode/getLoginUrl")
        val jsonObject = OkHttpClientUtils.getJson(response)
        return jsonObject.getJSONObject("data").getString("url")
    }

    override fun loginByQr2(url: String): CommonResult<BiliBiliEntity> {
        val oauthKey = BotUtils.regex("(?<=oauthKey\\=).*", url) ?: return CommonResult(500, "链接格式不正确！！")
        val response = OkHttpClientUtils.post(
            "https://passport.bilibili.com/qrcode/getLoginInfo", OkHttpClientUtils.addForms(
                "oauthKey", oauthKey,
                "gourl", "https://www.bilibili.com"
            )
        )
        val jsonObject = OkHttpClientUtils.getJson(response)
        val status = jsonObject.getBoolean("status")
        return if (!status){
            when (jsonObject.getInteger("data")){
                -2 -> CommonResult(500, "oauthKey不正确，请重试！！")
                -4 -> CommonResult(1, "二维码未被扫描！！")
                -5 -> CommonResult(2, "二维码已被扫描！！")
                else -> CommonResult(500, jsonObject.getString("message"))
            }
        }else{
            val successUrl = jsonObject.getJSONObject("data").getString("url")
            val sucResponse = OkHttpClientUtils.get(successUrl, OkHttpClientUtils.addReferer("https://passport.bilibili.com/login"))
            val biliBiliEntity = this.getBiliBiliEntityByResponse(sucResponse)!!
            CommonResult(200, "", biliBiliEntity)
        }
    }

    private fun getBiliBiliEntityByResponse(response: Response): BiliBiliEntity?{
        response.close()
        val cookie = OkHttpClientUtils.getCookie(response)
        val token = BotUtils.regex("bili_jct=", "; ", cookie) ?: return null
        val locationUrl = response.header("location")!!
        val userId = BotUtils.regex("DedeUserID=", "&", locationUrl)!!
        return BiliBiliEntity(cookie = cookie, token = token, userId = userId)
    }

    override fun getFriendDynamic(biliBiliEntity: BiliBiliEntity): CommonResult<List<BiliBiliPojo>> {
        val response = OkHttpClientUtils.get("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new?type_list=268435455",
                OkHttpClientUtils.addCookie(biliBiliEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        if (jsonObject.getInteger("code") != 0) return CommonResult(500, "您的哔哩哔哩登录已失效，请重新登录！！")
        val jsonArray = jsonObject.getJSONObject("data").getJSONArray("cards")
        val list = mutableListOf<BiliBiliPojo>()
        jsonArray.forEach { list.add(this.convert(it as JSONObject)) }
        return CommonResult(200, "成功", list)
    }

    override fun isLiveOnline(id: String): Boolean {
        val response = OkHttpClientUtils.get("https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=$id")
        val jsonObject = OkHttpClientUtils.getJson(response)
        val status = jsonObject.getJSONObject("data").getInteger("liveStatus")
        return status == 1
    }

    override fun liveSign(biliBiliEntity: BiliBiliEntity): String {
        val response = OkHttpClientUtils.get("https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign",
                OkHttpClientUtils.addCookie(biliBiliEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0) "哔哩哔哩直播签到成功！！"
        else jsonObject.getString("message")
    }

    override fun like(biliBiliEntity: BiliBiliEntity, id: String, isLike: Boolean): String {
        val response = OkHttpClientUtils.post("https://api.vc.bilibili.com/dynamic_like/v1/dynamic_like/thumb", OkHttpClientUtils.addForms(
                "uid", biliBiliEntity.userId,
                "dynamic_id", id,
                "up", if (isLike) "1" else "2",
                "csrf_token", biliBiliEntity.token
        ), OkHttpClientUtils.addCookie(biliBiliEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0) "赞动态成功！！"
        else "赞动态失败，${jsonObject.getString("message")}"
    }

    override fun comment(biliBiliEntity: BiliBiliEntity, rid: String, type: String, content: String): String {
        val response = OkHttpClientUtils.post("https://api.bilibili.com/x/v2/reply/add", OkHttpClientUtils.addForms(
                "oid", rid,
                "type", type,
                "message", content,
                "plat", "1",
                "jsonp", "jsonp",
                "csrf", biliBiliEntity.token
        ), OkHttpClientUtils.addCookie(biliBiliEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0) "评论动态成功！！"
        else "评论动态失败，${jsonObject.getString("message")}"
    }

    override fun forward(biliBiliEntity: BiliBiliEntity, id: String, content: String): String {
        val response = OkHttpClientUtils.post("https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/repost", OkHttpClientUtils.addForms(
                "uid", biliBiliEntity.userId,
                "dynamic_id", id,
                "content", content,
                "extension", "{\"emoji_type\":1}",
                "at_uids", "",
                "ctrl", "[]",
                "csrf_token", biliBiliEntity.token
        ), OkHttpClientUtils.addCookie(biliBiliEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0) "转发动态成功！！"
        else "转发动态失败，${jsonObject.getString("message")}"
    }

    override fun tossCoin(biliBiliEntity: BiliBiliEntity, rid: String, bvId: String, count: Int): String {
        val response = OkHttpClientUtils.post("https://api.bilibili.com/x/web-interface/coin/add", OkHttpClientUtils.addForms(
                "aid", rid,
                "multiply", count.toString(),
                "select_like", "1",
                "cross_domain", "true",
                "csrf", biliBiliEntity.token
        ), OkHttpClientUtils.addHeaders(
                "cookie", biliBiliEntity.cookie,
                "referer", "https://www.bilibili.com/video/$"
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0) "对该动态（视频）投硬币成功！！"
        else "对该动态（视频）投硬币成功！！，${jsonObject.getString("message")}"
    }

    override fun favorites(biliBiliEntity: BiliBiliEntity, rid: String, name: String): String {
        val userId = biliBiliEntity.userId
        val cookie = biliBiliEntity.cookie
        val token = biliBiliEntity.token
        val firstResponse = OkHttpClientUtils.get("https://api.bilibili.com/x/v3/fav/folder/created/list-all?type=2&rid=$rid&up_mid=$userId",
                OkHttpClientUtils.addCookie(cookie))
        val firstJsonObject = OkHttpClientUtils.getJson(firstResponse)
        if (firstJsonObject.getInteger("code") != 0) return "收藏失败，请重新绑定哔哩哔哩！！"
        var favoriteId: String? = null
        firstJsonObject.getJSONObject("data").getJSONArray("list").forEach {
            val jsonObject = it as JSONObject
            if (jsonObject.getString("title") == name){
                favoriteId = jsonObject.getString("id")
                return@forEach
            }
        }
        if (favoriteId == null){
            val addFolderResponse = OkHttpClientUtils.post("https://api.bilibili.com/x/v3/fav/folder/add", OkHttpClientUtils.addForms(
                    "title", name,
                    "privacy", "0",
                    "jsonp", "jsonp",
                    "csrf", token
            ), OkHttpClientUtils.addCookie(cookie))
            val jsonObject = OkHttpClientUtils.getJson(addFolderResponse)
            if (jsonObject.getInteger("code") != 0) return "您并没有该收藏夹，而且创建该收藏夹失败，请重试！！"
            favoriteId = jsonObject.getJSONObject("data").getString("id")
        }
        val resultResponse = OkHttpClientUtils.post("https://api.bilibili.com/x/v3/fav/resource/deal", OkHttpClientUtils.addForms(
                "rid", rid,
                "type", "2",
                "add_media_ids", favoriteId!!,
                "del_media_ids", "",
                "jsonp", "jsonp",
                "csrf", token
        ), OkHttpClientUtils.addCookie(cookie))
        val jsonObject = OkHttpClientUtils.getJson(resultResponse)
        return if (jsonObject.getInteger("code") == 0) "收藏该视频成功！！"
        else "收藏视频失败，${jsonObject.getString("message")}！！"
    }

    override fun uploadImage(biliBiliEntity: BiliBiliEntity, byteString: ByteString): CommonResult<JSONObject> {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file_up", "${BotUtils.randomStr(8)}.jpg", OkHttpClientUtils.addStream(byteString))
                .addFormDataPart("biz", "draw")
                .addFormDataPart("category", "daily").build()
        val response = OkHttpClientUtils.post("https://api.vc.bilibili.com/api/v1/drawImage/upload", body,
                OkHttpClientUtils.addCookie(biliBiliEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0) CommonResult(200, "", jsonObject.getJSONObject("data"))
        else CommonResult(500, "图片上传失败，${jsonObject.getString("message")}")
     }

    override fun publishDynamic(biliBiliEntity: BiliBiliEntity, content: String, images: List<String>): String {
        val jsonArray = JSONArray()
        images.forEach {
            val response = OkHttpClientUtils.get(it)
            jsonArray.add(this.uploadImage(biliBiliEntity, OkHttpClientUtils.getByteStr(response)))
        }
        val response = OkHttpClientUtils.post("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/create_draw", OkHttpClientUtils.addForms(
                "biz", "3",
                "category", "3",
                "type", "0",
                "pictures", jsonArray.toString(),
                "title", "",
                "tags", "",
                "description", content,
                "content", content,
                "setting", "{\"copy_forbidden\":0,\"cachedTime\":0}",
                "from", "create.dynamic.web",
                "up_choose_comment", "0",
                "extension", "{\"emoji_type\":1,\"from\":{\"emoji_type\":1},\"flag_cfg\":{}}",
                "at_uids", "",
                "at_control", "",
                "csrf_token", biliBiliEntity.token
        ), OkHttpClientUtils.addCookie(biliBiliEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0) "发布动态成功！！！"
        else "发布动态失败，${jsonObject.getString("message")}"
    }
}
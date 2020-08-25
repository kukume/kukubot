package me.kuku.yuq.logic.impl

import com.alibaba.fastjson.JSON
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
        biliBiliPojo.text = cardJsonObject.getString("dynamic") ?: itemJsonObject?.getString("description") ?: itemJsonObject?.getString("content") ?: "没有发现内容！！"
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
                biliBiliPojo.forwardText = forwardItemJsonObject.getString("description")
                val forwardPicJsonArray = forwardItemJsonObject.getJSONArray("pictures")
                val forwardPicList = biliBiliPojo.forwardPicList
                forwardPicJsonArray?.forEach {
                    val picJsonObject = it as JSONObject
                    forwardPicList.add(picJsonObject.getString("img_src"))
                }
                val forwardUserJsonObject = forwardContentJsonObject.getJSONObject("user")
                biliBiliPojo.forwardUserId = forwardUserJsonObject.getString("uid")
                biliBiliPojo.forwardName = forwardUserJsonObject.getString("name")
            }else {
                biliBiliPojo.forwardText = forwardContentJsonObject.getString("dynamic")
                val forwardOwnerJsonObject = forwardContentJsonObject.getJSONObject("owner")
                biliBiliPojo.forwardUserId = forwardOwnerJsonObject.getString("mid")
                biliBiliPojo.forwardName = forwardOwnerJsonObject.getString("name")
            }
        }
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

    override fun getDynamicById(id: String): CommonResult<List<BiliBiliPojo>> {
        val response = OkHttpClientUtils.get("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=$id&offset_dynamic_id=0&need_top=1",
                OkHttpClientUtils.addReferer("https://space.bilibili.com/$id/dynamic"))
        val jsonObject = OkHttpClientUtils.getJson(response)
        // next_offset  下一页开头
        val jsonArray = jsonObject.getJSONObject("data").getJSONArray("cards") ?: return CommonResult(500, "该用户并没有动态哦！！")
        val list = mutableListOf<BiliBiliPojo>()
        for (i in jsonArray.indices){
            val singleJsonObject = jsonArray.getJSONObject(i)
            list.add(this.convert(singleJsonObject))
        }
        return CommonResult(200, "", list)
    }

    override fun loginByQQ(qqEntity: QQEntity): CommonResult<String> {
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
        biliBiliLoginResponse.close()
        val cookie = OkHttpClientUtils.getCookie(biliBiliLoginResponse)
        return CommonResult(200, "登录成功", cookie)
    }

    override fun loginByWeibo(weiboEntity: WeiboEntity): CommonResult<String> {
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
        resultResponse.close()
        val cookie = OkHttpClientUtils.getCookie(resultResponse)
        return CommonResult(200, "登录成功", cookie)
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
        return jsonObject.getString("message")
    }
}
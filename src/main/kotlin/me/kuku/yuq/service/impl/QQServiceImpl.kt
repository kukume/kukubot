package me.kuku.yuq.service.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.service.QQService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.QQSuperLoginUtils
import me.kuku.yuq.utils.QQUtils
import okhttp3.MultipartBody
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

class QQServiceImpl: QQService {
    /**
     * {"category_id":9,"page":0,"pic_id":178}  坚持戴口罩
     * 9    0     124   学习打卡
     * 9    0     120    上班打卡
     * 180  自习开始
     * 181  自习结束
     * 127  每天早起
     * 125  每天运动
     * 126  每天早睡
     * 8   0     100  晚安  4
     * 2   0    18  心情   5
     * ""  ""   ""  运势   template_id   8
     * template_id 1 天气
     */
    override fun groupSign(qqEntity: QQEntity, group: Long, place: String, text: String, info: String): String {
        val response = OkHttpClientUtils.post("https://qun.qq.com/cgi-bin/qiandao/sign/publish", OkHttpClientUtils.addForms(
                "btn", qqEntity.getGtk(),
                "template_data", "",
                "gallery_info", info,
                "template_id", "[object Object]",
                "gc", group.toString(),
                "client", "2",
                "lgt", "0",
                "lat", "0",
                "poi", place,
                "text", text
        ), qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("cgicode") == 0)
            "qq群${group}签到成功"
        else "qq群签到失败，请更新QQ！"
    }

    override fun groupLottery(qqEntity: QQEntity, group: Long): String {
        val response = OkHttpClientUtils.post("https://pay.qun.qq.com/cgi-bin/group_pay/good_feeds/draw_lucky_gift", OkHttpClientUtils.addForms(
                "bkn", qqEntity.getGtk(),
                "from", "0",
                "gc", group.toString(),
                "client", "1",
                "version", "8.3.0.4480"
        ), qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ec") == 0){
            when {
                jsonObject.getInteger("lucky_code") == 7779 -> "抱歉，等级不够5级，无法抽礼物"
                "" == jsonObject.getString("name") -> "抱歉，没有抽到礼物"
                else -> "抽礼物成功，抽到了${jsonObject.getString("name")}"
            }
        }else "抽礼物失败，请更新QQ！"
    }

    override fun vipSign(qqEntity: QQEntity): String {
        val sb = StringBuilder()
        val gtk2 = qqEntity.getGtk2()
        val cookie = qqEntity.cookie()
        var response = OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=79968&format=json&g_tk=$gtk2&cachetime=${Date().time}", cookie)
        when (OkHttpClientUtils.getJson(response).getInteger("ret")){
            0 -> sb.appendln("会员面板签到成功！")
            10601 -> sb.appendln("会员面板今天已经签到！")
            10002 -> sb.appendln("会员面板签到失败！请更新QQ！")
            20101 -> sb.appendln("会员面板签到失败，不是QQ会员！")
        }
        response = OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=403490&rand=0.27489888${Date().time}&g_tk=$gtk2&format=json", cookie)
        when (OkHttpClientUtils.getJson(response).getInteger("ret")){
            0 -> sb.appendln("会员电脑端签到成功！")
            10601 -> sb.appendln("会员电脑端今天已经签到！")
            10002 -> sb.appendln("会员电脑端签到失败！请更新QQ！")
        }
        response = OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?actid=52002&rand=0.27489888${Date().time}&g_tk=$gtk2", cookie)
        when (OkHttpClientUtils.getJson(response).getInteger("ret")){
            0 -> sb.appendln("会员手机端签到成功！")
            10601 -> sb.appendln("会员手机端今天已经签到！")
            10002 -> sb.appendln("会员手机端签到失败！请更新QQ！")
        }
        response = OkHttpClientUtils.get("https://iyouxi4.vip.qq.com/ams3.0.php?_c=page&actid=239151&isLoadUserInfo=1&format=json&g_tk=$gtk2", cookie)
        when (OkHttpClientUtils.getJson(response).getInteger("ret")){
            0 -> sb.appendln("会员积分签到成功！")
            10601 -> sb.appendln("会员积分今天已经签到！")
            10002 -> sb.appendln("会员积分签到失败！请更新QQ！")
        }
        response = OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=23074&format=json&g_tk=$gtk2", cookie)
        when (OkHttpClientUtils.getJson(response).getInteger("ret")){
            0 -> sb.appendln("会员积分手机端签到成功！")
            10601 -> sb.appendln("会员积分手机端今天已经签到！")
            10002 -> sb.appendln("会员积分手机端签到失败！请更新QQ！")
        }
        response = OkHttpClientUtils.get("https://pay.qun.qq.com/cgi-bin/group_pay/good_feeds/gain_give_stock?gain=1&bkn=${qqEntity.getGtk()}", OkHttpClientUtils.addHeaders(
                "Referer", "https://m.vip.qq.com/act/qun/jindou.html",
                "cookie", qqEntity.getCookie()
        ))
        when (OkHttpClientUtils.getJson(response).getInteger("ec")){
            0 -> sb.appendln("免费领金豆成功！")
            1010 -> sb.appendln("今天已经领取过金豆了！")
            else -> sb.appendln("领取金豆失败！")
        }
        OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=$gtk2&actid=27754&_=${Date().time}", cookie).close()
        OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=$gtk2&actid=27755&_c=page&_=${Date().time}", cookie).close()
        OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=$gtk2&actid=22894&_c=page&_=${Date().time}", cookie).close()
        OkHttpClientUtils.get("https://iyouxi4.vip.qq.com/ams3.0.php?g_tk=$gtk2&actid=239371&_c=page&format=json&_=${Date().time}", cookie).close()
        OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=$gtk2&actid=22887&_c=page&format=json&_=${Date().time}", cookie).close()
        OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=$gtk2&actid=202041&_c=page&format=json&_=${Date().time}", cookie).close()
        OkHttpClientUtils.get("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=$gtk2&actid=202049&_c=page&format=json&_=${Date().time}", cookie).close()
        return sb.toString()
    }

    override fun queryVip(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.get("https://api.unipay.qq.com/v1/r/1450000172/wechat_query?cmd=7&pf=vip_m-50000-html5&pfkey=pfkey&session_id=uin&expire_month=0&session_type=skey&openid=${qqEntity.qq}&openkey=${qqEntity.sKey}&format=jsonp__myserviceIcons")
        val jsonObject = OkHttpClientUtils.getJson(response, "\\{.*\\}")
        return if (jsonObject.getInteger("ret") == 0){
            val jsonArray = jsonObject.getJSONArray("service")
            val sb = StringBuilder("一共为您查询到${jsonArray.size}项业务")
            jsonArray.forEach {
                val vipJsonObject = it as JSONObject
                val name: String
                name = when{
                    vipJsonObject.containsKey("year_service_name") -> vipJsonObject.getString("year_service_name")
                    vipJsonObject.containsKey("upgrade_service_name") -> vipJsonObject.getString("upgrade_service_name")
                    else -> vipJsonObject.getString("service_name")
                }
                sb.appendln("业务名称：$name")
                        .appendln("开通日期：${vipJsonObject.getString("start_time")}")
                        .appendln("到期日期：${vipJsonObject.getString("end_time")}")
                        .appendln("------------")
            }
            sb.removeSuffix("\r\n").toString()
        }else "业务查询失败，请更新QQ！"
    }

    override fun phoneGameSign(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.get("http://reader.sh.vip.qq.com/cgi-bin/common_async_cgi?g_tk=${qqEntity.getGtkP()}&plat=1&version=6.6.6&param=%7B%22key0%22%3A%7B%22param%22%3A%7B%22bid%22%3A13792605%7D%2C%22module%22%3A%22reader_comment_read_svr%22%2C%22method%22%3A%22GetReadAllEndPageMsg%22%7D%7D", qqEntity.cookieWithQQZone())
        return if (OkHttpClientUtils.getJson(response).getInteger("ecode") == 0)
            "手游加速0.2天成功！"
        else "手游加速失败，请更新QQ！"
    }

    override fun yellowSign(qqEntity: QQEntity): String {
        val gtkP = qqEntity.getGtkP()
        var response = OkHttpClientUtils.post("https://vip.qzone.qq.com/fcg-bin/v2/fcg_mobile_vip_site_checkin?t=0.89457${Date().time}&g_tk=$gtkP&qzonetoken=423659183", OkHttpClientUtils.addForms(
                "uin", qqEntity.qq.toString(),
                "format", "json"
        ), qqEntity.cookieWithQQZone())
        val sb = StringBuilder()
        when (OkHttpClientUtils.getJson(response).getInteger("code")){
            0 -> sb.append("黄钻签到成功！")
            -3000 ->{
                sb.append("黄钻签到失败！请更新QQ！")
            }
            else -> sb.append("黄钻今日已签到！")
        }
        response = OkHttpClientUtils.post("https://activity.qzone.qq.com/fcg-bin/fcg_huangzuan_daily_signing?t=0.${Date().time}906035&g_tk=$gtkP&qzonetoken=-1", OkHttpClientUtils.addForms(
                "option", "sign",
                "uin", qqEntity.qq.toString(),
                "format", "json"
        ), qqEntity.cookieWithQQZone())
        when (OkHttpClientUtils.getJson(response).getInteger("code")){
            0 -> sb.append("黄钻公众号签到成功！")
            -3000 -> sb.append("黄钻公众号签到失败！请更新QQ！")
            -90002 -> sb.append("抱歉，您不是黄钻用户，签到失败")
            else -> sb.append("黄钻今日已签到！")
        }
        return sb.toString()
    }

    override fun qqVideoSign1(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.get("https://vip.video.qq.com/fcgi-bin/comm_cgi?name=hierarchical_task_system&cmd=2&_=${Date().time}8906", qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response, "\\{.*\\}")
        return when (jsonObject.getInteger("ret")){
            0 -> "腾讯视频会员签到成功"
            -10006 -> "腾讯视频会员签到失败，请更新QQ！"
            -10019 -> "您不是腾讯视频会员，签到失败！"
            else -> "腾讯视频会员签到失败，${jsonObject.getString("msg")}"
        }
    }

    override fun qqVideoSign2(qqEntity: QQEntity): String {
        var response = OkHttpClientUtils.get("https://access.video.qq.com/user/auth_login?vappid=11059694&vsecret=fdf61a6be0aad57132bc5cdf78ac30145b6cd2c1470b0cfe&login_flag=1&type=qq&appid=101483052&g_tk=${qqEntity.getGtk()}&g_vstk=&g_actk=&callback=jQuery19107079438303985055_1588043611061&_=${Date().time}",
                OkHttpClientUtils.addCookie("${qqEntity.getCookie()}video_guid=87f1f5fd3c3ebf5a; video_platform=2; "))
        response.close()
        val cookie = OkHttpClientUtils.getCookie(response)
        return if (cookie.contains("vusession")){
            response = OkHttpClientUtils.get("https://v.qq.com/x/bu/mobile_checkin",
                OkHttpClientUtils.addCookie(qqEntity.getCookie() + cookie + "video_guid=fd42304ceeead2c8; video_platform=2; "))
            val html = OkHttpClientUtils.getStr(response)
            if (!html.contains("签到失败"))
                "签到成功"
            else "签到失败，请先去腾讯视频app私信\"https://v.qq.com/x/bu/mobile_checkin\"并打开该链接"
        }else "腾讯视频二次签到失败！请更新QQ！"
    }

    override fun sVipMornSign(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.get("https://mq.vip.qq.com/m/signsport/signSport?uin=${qqEntity.qq}&isRemind=0&ps_tk=${qqEntity.getGtkP()}&g_tk=${qqEntity.getGtk2()}", OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookieWithQQZone(),
                "Referer", "https://mq.vip.qq.com/m/signsport/index"
        ))
        val str = OkHttpClientUtils.getStr(response)
        return if (str.contains("html")) "sVip打卡报名失败，请更新QQ！"
        else{
            val jsonObject = JSON.parseObject(str)
            return if (jsonObject.getInteger("ret") == 0){
                when (jsonObject.getJSONObject("data").getInteger("code")){
                    0 -> "sVip打卡报名成功！"
                    130002 -> "sVip打卡已报名！"
                    else -> "sVip打卡报名失败，${jsonObject.getString("msg")}"
                }
            }else "sVip打卡报名失败，请更新QQ！"
        }
    }

    override fun sVipMornClock(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.get("https://mq.vip.qq.com/m/signsport/callSport?uin=${qqEntity.qq}&g_tk=${qqEntity.getGtk2()}&ps_tk=${qqEntity.getGtkP()}", qqEntity.cookieWithQQZone())
        return if (response.code == 200) {
            val jsonObject = OkHttpClientUtils.getJson(response)
            if (jsonObject.getInteger("ret") == 0) {
                when (jsonObject.getJSONObject("data").getInteger("code")) {
                    0 -> "sVip打卡成功"
                    100001 -> "sVip今日已打卡"
                    else -> "sVip打卡失败，${jsonObject.getString("msg")}"
                }
            } else "sVip打卡失败，请更新QQ！"
        }else "sVip打卡失败，请更新QQ！"
    }

    override fun bigVipSign(qqEntity: QQEntity): String {
        OkHttpClientUtils.get("https://h5.qzone.qq.com/qzone/visitor?_wv=3&_wwv=1024&_proxy=1", qqEntity.cookie()).close()
        val response1 = OkHttpClientUtils.post("https://h5.qzone.qq.com/webapp/json/QQBigVipTask/CompleteTask?t=0.${Date().time}906319&g_tk=${qqEntity.getGtkP()}", OkHttpClientUtils.addForms(
                "outCharset", "utf-8",
                "iAppId", "0",
                "llTime", Date().time.toString(),
                "format", "json",
                "iActionType", "6",
                "strUid", qqEntity.qq.toString(),
                "uin", qqEntity.qq.toString(),
                "inCharset", "utf-8"
        ), qqEntity.cookieWithQQZone())
        val response2 = OkHttpClientUtils.post("https://vip.qzone.qq.com/fcg-bin/v2/fcg_vip_task_checkin?t=0${Date().time}082161&g_tk=${qqEntity.getGtkP()}", OkHttpClientUtils.addForms(
                "appid", "qq_big_vip",
                "op", "CheckIn",
                "uin", qqEntity.qq.toString(),
                "format", "json",
                "inCharset", "utf-8",
                "outCharset", "utf-8"
        ), qqEntity.cookieWithQQZone())
        val jsonObject1 = OkHttpClientUtils.getJson(response1)
        val jsonObject2 = OkHttpClientUtils.getJson(response2)
        return when{
            jsonObject1.getInteger("ret") == 0 && jsonObject2.getInteger("code") == 0 -> "大会员签到成功"
            jsonObject1.getInteger("ret") == -3000 && jsonObject2.getInteger("code") == -3000 -> "大会员签到失败！请更新QQ！"
            else -> "大会员签到失败！"
        }
    }

    override fun modifyNickname(qqEntity: QQEntity, nickname: String): String {
        val response = OkHttpClientUtils.post("https://w.qzone.qq.com/cgi-bin/user/cgi_apply_updateuserinfo_new?g_tk=${qqEntity.getGtkP()}", OkHttpClientUtils.addForms(
                "qzreferrer", "http%3A%2F%2Fctc.qzs.qq.com%2Fqzone%2Fv6%2Fsetting%2Fprofile%2Fprofile.html%3Ftab%3Dbase",
                "nickname", nickname,
                "emoji", "",
                "sex", "",
                "birthday", "",
                "province", "0",
                "city", "",
                "country", "",
                "marriage", "6",
                "bloodtype", "5",
                "hp", "0",
                "hc", "",
                "hco", "",
                "career", "",
                "company", "",
                "cp", "0",
                "cc", "0",
                "cb", "",
                "cco", "0",
                "lover", "",
                "islunar", "0",
                "mb", "1",
                "uin", qqEntity.qq.toString(),
                "pageindex", "1",
                "nofeeds", "1",
                "fupdate", "1",
                "format", "json"
        ), qqEntity.cookieWithQQZone())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0)
            "修改昵称成功！当前昵称：$nickname"
        else "修改昵称失败！${jsonObject.getString("msg")}"
    }

    override fun modifyAvatar(qqEntity: QQEntity, url: String): String {
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("is_set", "1")
                .addFormDataPart("is_share", "0")
                .addFormDataPart("format", "png")
                .addFormDataPart("name", "")
                .addFormDataPart("vip_level", "0")
                .addFormDataPart("isHD", "false")
                .addFormDataPart("catId", "0")
                .addFormDataPart("cmd", "set_and_share_face")
                .addFormDataPart("Filename", "image100*100")
                .addFormDataPart("Upload", "Submit Query")
                .addFormDataPart("Filedata[]", "image100*100", OkHttpClientUtils.addStream(url))
                .build()
        val response = OkHttpClientUtils.post("https://face.qq.com/client/uploadflash.php", multipartBody, qqEntity.cookieWithQQZone())
        return when (OkHttpClientUtils.getJson(response).getInteger("result")){
            0 -> "QQ头像设置成功！"
            1001 -> "头像设置失败，请更新QQ！"
            1002 -> "非QQ会员，无法设置头像！"
            2004 -> "图片不规范，换个图片吧！"
            else -> "头像设置失败"
        }
    }

    override fun weiYunSign(qqEntity: QQEntity): String {
        val commonResult = QQSuperLoginUtils.weiYunLogin(qqEntity)
        return if (commonResult.code == 200) {
            val response = OkHttpClientUtils.get("https://h5.weiyun.com/sign_in",
                    OkHttpClientUtils.addCookie(qqEntity.getCookie(commonResult.t)))
            val str = OkHttpClientUtils.getStr(response)
            val json = BotUtils.regex("(?<=window\\.__INITIAL_STATE__=).+?(?=</script>)", str)
            val jsonObject = JSON.parseObject(json)
            "微云签到成功，已连续签到${jsonObject.getJSONObject("index").getInteger("consecutiveSignInCount")}天，当前金币${jsonObject.getJSONObject("global").getInteger("totalCoin")}"
        }else "微云签到失败，请更新QQ！"
    }

    override fun qqMusicSign(qqEntity: QQEntity): String {
        val url = "https://u.y.qq.com/cgi-bin/musicu.fcg"
        val headers = OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookie(),
                "referer", url
        )
        val gtk = qqEntity.getGtk()
        var response = OkHttpClientUtils.post(url, OkHttpClientUtils.addJson("{\"req_0\":{\"module\":\"UserGrow.UserGrowScore\",\"method\":\"receive_score\",\"param\":{\"musicid\":\"${qqEntity.qq}\",\"type\":15}},\"comm\":{\"g_tk\":${qqEntity.getGtk()},\"uin\":${qqEntity.qq},\"format\":\"json\",\"ct\":23,\"cv\":0}}"),
                headers)
        var jsonObject = OkHttpClientUtils.getJson(response)
        val reqJsonObject = jsonObject.getJSONObject("req_0") ?: return "签到失败"
        jsonObject = reqJsonObject.getJSONObject("data")
        var result = when (jsonObject.getInteger("retCode")){
            0 -> "QQ音乐签到成功！获得积分：${jsonObject.getString("todayScore")}，签到天数：${jsonObject.getString("totalDays")}，总积分：${jsonObject.getString("totalScore")}"
            40001 -> "QQ音乐今日已签到！"
            -13004 -> "QQ音乐签到失败！请更新QQ！"
            else -> "QQ音乐签到失败！${jsonObject.getString("errMsg")}！"
        }

        response = OkHttpClientUtils.post(url,
                OkHttpClientUtils.addJson("{\"comm\":{\"g_tk\":$gtk,\"uin\":${qqEntity.qq},\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"h5\",\"needNewCode\":1,\"ct\":23,\"cv\":0},\"req_0\":{\"module\":\"music.activeCenter.ActiveCenterSignSvr\",\"method\":\"DoSignIn\",\"param\":{}}}"),
                headers)
        jsonObject = OkHttpClientUtils.getJson(response).getJSONObject("req_0").getJSONObject("data")
        result += when (jsonObject.getInteger("retCode")){
            0 -> "QQ音乐活动签到成功！已连续签到${jsonObject.getJSONObject("signInfo").getString("continuousDays")}天，累计签到${jsonObject.getJSONObject("signInfo").getString("totalDays")}天"
            40004 -> "QQ音乐活动今日已签到！"
            -13004 -> "QQ音乐签到失败！请更新QQ！"
            else -> "QQ音乐签到失败！${jsonObject.getString("errMsg")}！"
        }

        response = OkHttpClientUtils.post(url,
                OkHttpClientUtils.addJson("{\"req_0\":{\"module\":\"UserGrow.UserGrowScore\",\"method\":\"receive_score\",\"param\":{\"musicid\":\"'.$this->uin.'\",\"type\":1}},\"comm\":{\"g_tk\":$gtk,\"uin\":${qqEntity.qq},\"format\":\"json\",\"ct\":23,\"cv\":0}}"),
                headers)
        jsonObject = OkHttpClientUtils.getJson(response).getJSONObject("req_0").getJSONObject("data")
        result += when (jsonObject.getInteger("retCode")){
            0 -> "QQ音乐分享成功！获得积分：${jsonObject.getString("todayScore")}天，签到天数：${jsonObject.getString("totalDays")}天，总积分:${jsonObject.getString("totalScore")}"
            40001 -> "QQ音乐今日已分享！"
            40002 -> "QQ音乐今日分享未完成！"
            -13004 -> "QQ音乐分享失败！请更新QQ！"
            else -> "QQ音乐分享失败！${jsonObject.getString("errMsg")}！"
        }

        response = OkHttpClientUtils.post(url, OkHttpClientUtils.addJson("{\"req_0\":{\"module\":\"Radio.RadioLucky\",\"method\":\"clockIn\",\"param\":{\"platform\":2}},\"comm\":{\"g_tk\":${qqEntity.getGtkP()},\"uin\":${qqEntity.qq},\"format\":\"json\"}}"),
                headers)
        jsonObject = OkHttpClientUtils.getJson(response).getJSONObject("req_0").getJSONObject("data")
        result += when (jsonObject.getInteger("retCode")){
            0 -> "QQ音乐电台锦鲤打卡成功！积分+${jsonObject.getString("score")}"
            40001 -> "QQ音乐电台锦鲤已打卡！"
            -13004 -> "QQ音乐电台锦鲤打卡失败！请更新QQ！"
            else -> "QQ音乐电台锦鲤打卡失败！${jsonObject.getString("errMsg")}"
        }
        OkHttpClientUtils.get("https://service-n157vbwh-1252343050.ap-beijing.apigateway.myqcloud.com/release/lzz_qqmusic?qq=${qqEntity.qq}&hour=2").close()
        return result
    }

    override fun gameSign(qqEntity: QQEntity): String {
        val gtk= qqEntity.getGtk()
        val sb = StringBuilder()
        var response = OkHttpClientUtils.get("http://social.minigame.qq.com/cgi-bin/social/welcome_panel_operate?format=json&cmd=2&uin=${qqEntity.qq}&g_tk=$gtk", OkHttpClientUtils.addHeaders(
                "referer", "http://minigame.qq.com/appdir/social/cloudHall/src/index/welcome.html",
                "cookie", qqEntity.getCookie()
        ))
        var jsonObject = OkHttpClientUtils.getJson(response)
        var result = jsonObject.getInteger("result")
        var str = when (result){
            0 -> {
                if (jsonObject.getInteger("do_ret") == 11) "游戏大厅今天已签到！"
                else "游戏大厅签到成功！"
            }
            1000005 -> "游戏大厅签到失败！请更新QQ！"
            else -> "游戏大厅签到失败！${jsonObject.getString("resultstr")}"
        }
        sb.appendln(str)
        response = OkHttpClientUtils.get("http://social.minigame.qq.com/cgi-bin/social/CheckInPanel_Operate?Cmd=CheckIn_Operate&g_tk=$gtk", OkHttpClientUtils.addHeaders(
                "referer", "http://minigame.qq.com/appdir/social/cloudHall/src/index/welcome.html",
                "cookie", qqEntity.getCookie()
        ))
        jsonObject = OkHttpClientUtils.getJson(response)
        result = jsonObject.getInteger("result")
        str = when (result){
            0 -> "游戏大厅2签到成功！"
            1000005 -> "游戏大厅2签到失败！请更新QQ！"
            else -> "游戏大厅签到2失败！${jsonObject.getString("resultstr")}"
        }
        sb.appendln(str)
        response = OkHttpClientUtils.get("http://info.gamecenter.qq.com/cgi-bin/gc_my_tab_async_fcgi?merge=1&ver=0&st=${Date().time}746&sid=&uin=${qqEntity.qq}&number=0&path=489&plat=qq&gamecenter=1&_wv=1031&_proxy=1&gc_version=2&ADTAG=gamecenter&notShowPub=1&param=%7B%220%22%3A%7B%22param%22%3A%7B%22platform%22%3A1%2C%22tt%22%3A1%7D%2C%22module%22%3A%22gc_my_tab%22%2C%22method%22%3A%22sign_in%22%7D%7D&g_tk=$gtk", qqEntity.cookie())
        jsonObject = OkHttpClientUtils.getJson(response)
        val eCode = jsonObject.getInteger("ecode")
        str = when (eCode){
            0 -> {
                jsonObject = jsonObject.getJSONObject("data").getJSONObject("0")
                if (jsonObject.getInteger("retCode") == 0)
                    "手Q游戏中心签到成功！已连续签到${jsonObject.getJSONObject("retBody").getJSONObject("data").getInteger("cur_continue_sign")}天"
                else "手Q游戏中心签到失败！${jsonObject.getJSONObject("retBody").getString("message")}"
            }
            -120000 -> "手Q游戏中心签到失败！请更新QQ！"
            else -> "手Q游戏中心签到失败！"
        }
        sb.appendln(str)
        response = OkHttpClientUtils.get("https://1.game.qq.com/app/sign?start=" + SimpleDateFormat("yyyy-MM").format(Date()) +
                "&g_tk=$gtk&_t=0.6780016267291531", qqEntity.cookie())
        val jsonStr = BotUtils.regex("(?<=var sign_index = ).*?(?=;)", OkHttpClientUtils.getStr(response))
        jsonObject = JSON.parseObject(jsonStr)
        var iRet = jsonObject.getInteger("iRet")
        str = when (iRet){
            0 -> "精品页游签到成功！"
            1 -> "精品页游今天已签到！"
            else -> "精品页游签到失败！${jsonObject.getString("sMsg")}"
        }
        sb.appendln(str)
        response = OkHttpClientUtils.post("https://apps.game.qq.com/ams/ame/ame.php?ameVersion=0.3&sServiceType=dj&iActivityId=11117&sServiceDepartment=djc&set_info=djc", OkHttpClientUtils.addForms(
                "iActivityId", "11117",
                "iFlowId", "96939",
                "g_tk", gtk,
                "e_code", "0",
                "g_code", "0",
                "sServiceDepartment", "djc",
                "sServiceType", "dj"
        ), qqEntity.cookie())
        jsonObject = OkHttpClientUtils.getJson(response).getJSONObject("modRet")
        if (jsonObject == null) {
            sb.appendln("道聚城签到失败")
        } else {
            when (jsonObject.getInteger("ret")){
                0 -> sb.appendln("道聚城签到成功！")
                600 -> sb.appendln("道聚城今天已签到！")
                else -> sb.appendln("道聚城签到失败！${jsonObject.getJSONObject("modRet").getString("msg")}")
            }
        }
        val dnfUrl = "https://apps.game.qq.com/cms/index.php?serviceType=dnf&actId=2&sAction=duv&sModel=Data&retType=json"
        response = OkHttpClientUtils.get(dnfUrl, OkHttpClientUtils.addHeaders(
                "referer", dnfUrl,
                "cookie", qqEntity.getCookie()
        ))
        jsonObject = OkHttpClientUtils.getJson(response)
        iRet = jsonObject.getInteger("iRet")
        if (iRet == 0) {
            if (jsonObject.getJSONObject("jData").getInteger("iLotteryRet") == 100002)
                sb.append("DNF社区积分已领取！")
            else sb.append("DNF社区积分领取成功！")
        } else sb.append("DNF社区积分领取失败！")
        return sb.toString()
    }

    override fun qPetSign(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.get("https://fight.pet.qq.com/cgi-bin/petpk?cmd=award&op=1&type=0", qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ret")){
            null -> "大乐斗领礼包失败"
            -1,0 -> "大乐斗${jsonObject.getString("ContinueLogin")}${jsonObject.getString("DailyAward")}"
            5 -> "大乐斗领礼包失败！请更新QQ！"
            else -> "大乐斗领礼包失败！"
        }
    }

    override fun tribeSign(qqEntity: QQEntity): String {
        val sb = StringBuilder()
        val gtk = qqEntity.getGtk()
        var response = OkHttpClientUtils.post("https://buluo.qq.com/cgi-bin/bar/login_present_heart", OkHttpClientUtils.addForms(
                "bkn", gtk
        ), OkHttpClientUtils.addHeaders("cookie", qqEntity.getCookie(), "referer", "https://buluo.qq.com/mobile/my_heart.html"))
        val jsonObject = OkHttpClientUtils.getJson(response)
        when (jsonObject.getInteger("retcode")){
            0 ->{
                if (jsonObject.getJSONObject("result").getInteger("add_hearts") == 0)
                    sb.append("今日已领取爱心，")
                else
                    sb.append("成功领取爱心 + ${jsonObject.getJSONObject("result").getInteger("add_hearts")}，")
            }
            100000 -> sb.append("领取爱心失败，请更新QQ！")
            else -> sb.append("领取爱心失败！")
        }
        response = OkHttpClientUtils.get("https://buluo.qq.com/cgi-bin/bar/card/bar_list_by_page?uin=${qqEntity.qq}&neednum=30&startnum=0&r=0.98389${Date().time}", OkHttpClientUtils.addHeaders(
                "Referer", "https://buluo.qq.com/mobile/personal.html",
                "cookie", qqEntity.getCookie()
        ))
        if (response.code != 200) return "获取兴趣部落列表失败！"
        val tribeJsonObject = OkHttpClientUtils.getJson(response)
        when (tribeJsonObject.getInteger("retcode")){
            0 ->{
                val jsonArray = tribeJsonObject.getJSONObject("result").getJSONArray("followbars")
                jsonArray.forEach {
                    val singleJsonObject = it as JSONObject
                    response = OkHttpClientUtils.post("https://buluo.qq.com/cgi-bin/bar/user/sign", OkHttpClientUtils.addForms(
                            "bid", singleJsonObject.getString("bid"),
                            "bkn", gtk,
                            "r", "0.84746${Date().time}"
                    ), OkHttpClientUtils.addHeaders("cookie", qqEntity.getCookie(), "Referer", "https://buluo.qq.com/mobile/personal.html"))
                    val resultJsonObject = OkHttpClientUtils.getJson(response)
                    sb.append(singleJsonObject.getString("name"))
                    when (resultJsonObject.getInteger("retcode")){
                        0 -> sb.append("部落签到成功！")
                        100000 -> sb.append("部落签到失败！请更新QQ！")
                        else -> sb.append("部落签到失败！")
                    }
                }
            }
            100000 -> sb.append("兴趣部落签到失败！请更新QQ！")
            else -> sb.append("兴趣部落签到失败！")
        }
        return sb.toString()
    }

    override fun refuseAdd(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.post("https://ti.qq.com/cgi-node/friend-auth/set", OkHttpClientUtils.addForms(
                "req", "{\"at\":2,\"q\":\"\",\"a\":\"\",\"l\":[],\"viaphone\":0}",
                "bkn", qqEntity.getGtk()
        ), qqEntity.cookieWithQQZone())
        return if (OkHttpClientUtils.getJson(response).getInteger("ec") == 0){
            "设置拒绝任何人添加成功！"
        }else "设置失败，请更新QQ！"
    }

    override fun motionSign(qqEntity: QQEntity): String {
        val step = Random.nextInt(11111, 99999)
        val timestamp = Date().time
        val response = OkHttpClientUtils.post("https://yundong.qq.com/cgi/common_daka_tcp?g_tk=${qqEntity.getGtk()}", OkHttpClientUtils.addForms(
                "params", "{\"reqtype\":11,\"mbtodayStep\":$step,\"todayStep\":$step,\"timestamp\":$timestamp}",
                "l5apiKey", "daka.server",
                "dcapiKey", "daka_tcp"
        ), OkHttpClientUtils.addHeaders("cookie", qqEntity.getCookie(), "referer", "https://yundong.qq.com/daka/index?_wv=2098179&rank=1&steps=$step&asyncMode=1&type=&mid=105&timestamp=$timestamp"))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("code")){
            0 -> "QQ运动打卡成功！QQ成长值+0.2天！"
            -10001 -> "今天步数未达到打卡门槛，再接再厉！"
            -10003 -> "QQ运动今日已打卡！"
            -1001 -> "QQ运动打卡失败！请更新QQ！"
            else -> "QQ运动打卡失败！${jsonObject.getString("emsg")}"
        }
    }

    override fun blueSign(qqEntity: QQEntity): String {
        val commonResult = QQSuperLoginUtils.blueLogin(qqEntity)
        return if (commonResult.code == 200){
            val psKey = commonResult.t
            val cookie = qqEntity.getCookie(psKey) + "DomainID=176; "
            val gtk = qqEntity.getGtk()
            val sb = StringBuilder()
            var response = OkHttpClientUtils.get("https://app.gamevip.qq.com/cgi-bin/gamevip_sign/GameVip_SignIn?format=json&g_tk=$gtk&_=${Date().time}", OkHttpClientUtils.addHeaders(
                    "cookie", cookie,
                    "referer", "https://gamevip.qq.com/sign_pop/sign_pop_v2.html"
            ))
            var jsonObject = OkHttpClientUtils.getJson(response)
            when (jsonObject.getInteger("result")){
                0 -> sb.appendln("蓝钻签到成功！当前签到积分${jsonObject.getString("SignScore")}点")
                1000005 -> sb.appendln("蓝钻签到失败！请更新QQ！")
                else -> sb.appendln("蓝钻签到失败！${jsonObject.getString("resultstr")}")
            }
            response = OkHttpClientUtils.get("https://app.gamevip.qq.com/cgi-bin/gamevip_sign/GameVip_Lottery?format=json&g_tk=$gtk&_=${Date().time}0334", OkHttpClientUtils.addHeaders(
                    "cookie", cookie,
                    "referer", "https://gamevip.qq.com/sign_pop/sign_pop_v2.html"
            ))
            jsonObject = OkHttpClientUtils.getJson(response)
            when (jsonObject.getInteger("result")){
                0 -> sb.appendln("蓝钻抽奖成功")
                1000005 -> sb.appendln("蓝钻抽奖失败！请更新QQ！")
                102 -> sb.appendln("蓝钻抽奖次数已用完！")
                else -> sb.appendln("蓝钻抽奖失败！${jsonObject.getString("resultstr")}")
            }
            response = OkHttpClientUtils.get("https://app.gamevip.qq.com/cgi-bin/gamevip_m_sign/GameVip_m_SignIn", OkHttpClientUtils.addHeaders(
                    "referer", "https://gamevip.qq.com/sign_pop/sign_pop_v2.html",
                    "cookie", cookie
            ))
            jsonObject = OkHttpClientUtils.getJson(response)
            when (jsonObject.getInteger("result")){
                0 -> sb.appendln("蓝钻手机签到成功")
                1000005 -> sb.appendln("蓝钻手机签到失败！请更新QQ！")
                else -> sb.appendln("蓝钻抽奖失败！${jsonObject.getString("resultstr")}")
            }
            sb.toString()
        }else "蓝钻签到失败，请更新QQ！"
    }

    override fun like(qqEntity: QQEntity, qq: Long): String {
        var msg = ""
        val commonResult = QQSuperLoginUtils.vipLogin(qqEntity)
        if (commonResult.code != 200) return "点赞失败，登录已失效！！"
        for (i in 0 until 20) {
            val response = OkHttpClientUtils.get("https://club.vip.qq.com/visitor/like?g_tk=${QQUtils.getGtk(commonResult.t)}&nav=0&uin=$qq&t=${Date().time}", OkHttpClientUtils.addHeaders(
                    "cookie", qqEntity.getCookie(commonResult.t),
                    "referer", "https://club.vip.qq.com/visitor/index?_wv=4099&_nav_bgclr=ffffff&_nav_titleclr=ffffff&_nav_txtclr=ffffff&_nav_alpha=0"
            ))
            val jsonObject = OkHttpClientUtils.getJson(response)
            when (jsonObject.getInteger("retcode")){
                20003 -> return "已点名片赞${i}次，点赞已超过上限！"
                10003 -> return "点名片赞失败，权限异常！"
                -400 -> return "点名片赞失败，机器人登录已失效！"
                0 -> msg = "点名片赞成功"
                else -> return "点名片赞失败！"
            }
        }
        return msg
    }

    override fun sendFlower(qqEntity: QQEntity, qq: Long, group: Long): String {
        val response = OkHttpClientUtils.post("https://pay.qun.qq.com/cgi-bin/group_pay/good_feeds/send_goods", OkHttpClientUtils.addForms(
                "instanceID", "537064459",
                "giftID", "99",
                "channel", "1",
                "goodsId", "flower",
                "count", "3",
                "from", "0",
                "toUin", qq.toString(),
                "isCustom", "1",
                "rule", "0",
                "gc", group.toString(),
                "_r", "229",
                "version", "Android8.3.6.4590",
                "bkn", qqEntity.getGtk()
        ), OkHttpClientUtils.addHeaders(
                "referer", "https://qun.qq.com/qunpay/gifts/index.html?troopUin=$group&uin=$qq&name=&from=profilecard&_wv=1031&_bid=2204&_wvSb=1&_nav_alpha=0",
                "cookie", qqEntity.getCookie()
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> "送花成功！"
            4 -> "送花失败，请更新QQ！"
            20000 -> "鲜花不足，充点钱再送吧！！！"
            else -> "送花失败，${jsonObject.getString("em")}"
        }
    }

    override fun anotherSign(qqEntity: QQEntity): String {
        var response = OkHttpClientUtils.post("https://ti.qq.com/hybrid-h5/api/json/daily_attendance/SignInMainPage",
                OkHttpClientUtils.addJson("{\"uin\": \"${qqEntity.qq}\",\"QYY\": 2,\"qua\": \"V1_AND_SQ_8.3.3_1376_YYB_D\",\"loc\": {\"lat\": 27719813,\"lon\": 111317537}}"), qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response)
        if (jsonObject.getInteger("ret") == 0){
            val jsonArray = jsonObject.getJSONObject("data").getJSONObject("vecSignInfo").getJSONArray("value")
            var type: String? = null
            var subType: String? = null
            for (i in 0 until jsonArray.size){
                val signJsonObject = jsonArray.getJSONObject(i)
                if ("收集卡" == signJsonObject.getJSONObject("signInCover").getString("title")){
                    if (signJsonObject.getInteger("signInResult") == 1) return "今日已打卡！"
                    subType = signJsonObject.getString("subType")
                    type = signJsonObject.getString("type")
                }
            }
            return if (type != null && subType != null){
                response = OkHttpClientUtils.post("https://ti.qq.com/hybrid-h5/api/json/daily_attendance/SignIn",
                        OkHttpClientUtils.addJson("{\"uin\":\"${qqEntity.qq}\",\"type\":$type,\"sId\":\"\",\"subType\":$subType,\"qua\":\"V1_AND_SQ_8.3.3_1376_YYB_D\"}"), qqEntity.cookie())
                val signJsonObject = OkHttpClientUtils.getJson(response)
                if (signJsonObject.getInteger("ret") == 0){
                    for (i in 0 until 3)
                        OkHttpClientUtils.post("https://ti.qq.com/hybrid-h5/api/json/daily_attendance/AdvNotify",
                                OkHttpClientUtils.addJson("{\"uin\":${qqEntity.qq}}"), qqEntity.cookie()).close()
                    "打卡成功！"
                }else "打卡失败！${signJsonObject.getString("msg")}"
            }else "打卡失败！没有发现收集卡"
        }else return "打卡失败！请更新QQ！"
    }

    private fun getBubbleId(qqEntity: QQEntity, psKey: String, name: String): Int?{
        for (page in 0 until 5) {
            val response = OkHttpClientUtils.post("https://zb.vip.qq.com/bubble/cgi/getDiyBubbleList?daid=18&g_tk=${qqEntity.getGtk()}&p_tk=${qqEntity.pt4Token}", OkHttpClientUtils.addForms(
                    "page", page.toString(),
                    "num", "15"
            ), OkHttpClientUtils.addHeaders(
                    "cookie", "${qqEntity.getCookie(psKey)}pt4_token=${qqEntity.pt4Token}; ",
                    "user-agent", OkHttpClientUtils.QQ_UA
            ))
            val jsonObject = OkHttpClientUtils.getJson(response)
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("list")
            jsonArray.forEach {
                val bubbleJsonObject = it as JSONObject
                val bubbleName = bubbleJsonObject.getJSONArray("baseInfo").getJSONObject(0).getString("name")
                if (name == bubbleName){
                    return bubbleJsonObject.getInteger("id")
                }
            }
        }
        return null
    }

    /**
     * id 为null  即为随机气泡
     */
    override fun diyBubble(qqEntity: QQEntity, text: String, name: String?): String {
        val commonResult = QQSuperLoginUtils.vipLogin(qqEntity)
        return if (commonResult.code == 200){
            //登录成功
            val id = if (name == null){
                //随机id
                val ids = "2551|2514|2516|2493|2494|2464|2465|2428|2427|2426|2351|2319|2320|2321|2232|2239|2240|2276|2275|2274|2273|2272|2271"
                ids.split("|").random().toInt()
            }else{
                this.getBubbleId(qqEntity, commonResult.t, name)
            }
            if (id != null){
                val response = OkHttpClientUtils.get("https://g.vip.qq.com/bubble/bubbleSetup?id=$id&platformId=2&uin=${qqEntity.qq}&version=8.3.0.4480&diyText=%7B%22diyText%22%3A%22$text%22%7D&format=jsonp&t=${Date().time}&g_tk=${qqEntity.getGtk()}&p_tk=${qqEntity.pt4Token}&callback=jsonp0",
                        OkHttpClientUtils.addCookie(qqEntity.getCookie(commonResult.t)))
                val jsonObject = OkHttpClientUtils.getJsonp(response)
                when (jsonObject.getInteger("ret")){
                    0 -> "更换气泡成功，由于缓存等原因，效果可能会在较长一段时间后生效！"
                    -100001 -> "更换气泡失败，请更新QQ！"
                    5002,2002 -> "您不是睾贵的超级会员用户，更换气泡失败！"
                    else -> "更换气泡失败！${jsonObject.getString("msg")}"
                }
            }else "抱歉，未找到该气泡名字，更换气泡失败！"
        }else "更换气泡失败！请更新QQ！"
    }

    override fun qqSign(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.post("https://ti.qq.com/hybrid-h5/api/json/daily_attendance/SignIn", OkHttpClientUtils.addForms(
                "uin", qqEntity.qq.toString(),
                "type", "1",
                "sld", ""
        ), qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ret") == 0) "打卡成功" else "打卡失败，请更新QQ！"
    }

    override fun vipGrowthAdd(qqEntity: QQEntity): String {
        val now = LocalDate.now()
        val nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA))
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val response = OkHttpClientUtils.post("https://proxy.vac.qq.com/cgi-bin/srfentry.fcgi?ts=${Date().time}&g_tk=${qqEntity.getGtk()}", OkHttpClientUtils.addJson(
               "{\"13357\":{\"month\":${now.month.value},\"pageIndex\":1,\"pageSize\":20,\"sUin\":\"${qqEntity.qq}\",\"year\":${now.year}}}"
        ), OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookie()
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ecode") == 0){
            val jsonArray = jsonObject.getJSONObject("13357").getJSONObject("data").getJSONObject("growthRecord").getJSONArray("record")
            val sb = StringBuilder()
            var allAdd = 0
            for (i in 0 until jsonArray.size) {
                val singleJsonObject = jsonArray.getJSONObject(i)
                if (simpleDateFormat.format(Date("${singleJsonObject.getString("acttime")}000".toLong())) != nowDate) break
                val type = when (singleJsonObject.getInteger("actid")) {
                    126 -> "手机QQ签到"
                    140 -> "节日签到"
                    -9999 -> "每日成长值"
                    169 -> "QQ会员官方账号每日签到"
                    664 -> "早期走运"
                    697 -> singleJsonObject.getString("actname")
                    else -> "其他活动"
                }
                val add = singleJsonObject.getInteger("finaladd")
                allAdd += add
                sb.appendln("$type->${add}")
            }
            sb.append("总成长值->$allAdd").toString()
        }else "获取失败，请更新QQ！"
    }

    override fun publishNotice(qqEntity: QQEntity, group: Long, text: String): String {
        val response = OkHttpClientUtils.post("https://web.qun.qq.com/cgi-bin/announce/add_qun_notice", OkHttpClientUtils.addForms(
                "qid", group.toString(),
                "bkn", qqEntity.getGtk(),
                "text", text,
                "pinned", "0",
                "type", "1",
                "settings", "{\"is_show_edit_card\":0,\"tip_window_type\":1,\"confirm_required\":0}"
        ), qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> "发公告成功！"
            35 -> "我还不是管理员呢，不能发送公告！"
            1 -> "发送公告失败，请更新QQ！"
            else -> "发公告失败！${jsonObject.getString("em")}"
        }
    }

    override fun getGroupLink(qqEntity: QQEntity, group: Long): String {
        val response = OkHttpClientUtils.post("https://admin.qun.qq.com/cgi-bin/qun_admin/get_join_link", OkHttpClientUtils.addForms(
                "gc", group.toString(),
                "type", "1",
                "bkn", qqEntity.getGtk()
        ), OkHttpClientUtils.addHeaders(
                "Referer", "https://admin.qun.qq.com/create/share/index.html?ptlang=2052&groupUin=$group",
                "cookie", qqEntity.getCookie()
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> jsonObject.getString("url")
            1 -> "获取链接失败，请更新QQ！！"
            else -> "加群链接获取失败，${jsonObject.getString("em")}"
        }
    }

    override fun groupActive(qqEntity: QQEntity, group: Long, page: Int): String {
        val response = OkHttpClientUtils.get("https://qqweb.qq.com/c/activedata/get_mygroup_data?bkn=${qqEntity.getGtk()}&gc=$group&page=$page",
                qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> {
                val jsonArray = jsonObject.getJSONObject("ginfo").getJSONArray("g_most_act")
                if (jsonArray != null){
                    val sb = StringBuilder()
                    jsonArray.forEach {
                        val singleJsonObject = it as JSONObject
                        sb.appendln("@${singleJsonObject.getString("name")}：${singleJsonObject.getString("sentences_num")}条")
                    }
                    sb.removeSuffix("\r\n").toString()
                }else "群活跃数据获取失败！可能没有活跃信息！"
            }
            else -> "群活跃数据获取失败，请更新QQ！"
        }
    }

    override fun weiShiSign(qqEntity: QQEntity): String {
        val response = OkHttpClientUtils.get("https://h5.qzone.qq.com/weishi/jifen/main?_proxy=1&_wv=3&navstyle=2&titleh=55.0&statush=20.0",
                OkHttpClientUtils.addHeaders(
                        "cookie", qqEntity.getCookie(),
                        "user-agent", OkHttpClientUtils.MOBILE_UA
                ))
        val str = OkHttpClientUtils.getStr(response)
        return if ("错误提示" != Jsoup.parse(str).getElementsByTag("title").first().text()){
            val gtk = qqEntity.getGtk()
            var result = ""
            var cookie = OkHttpClientUtils.getCookie(response)
            cookie += qqEntity.getCookie()
            val secondResponse = OkHttpClientUtils.post("https://h5.qzone.qq.com/proxy/domain/activity.qzone.qq.com/fcg-bin/fcg_weishi_task_report_login?t=0${Date().time}030444&g_tk=$gtk", OkHttpClientUtils.addForms(
                    "task_appid", "weishi",
                    "task_id", "SignIn",
                    "qua", "_placeholder",
                    "format", "json",
                    "uin", qqEntity.qq.toString(),
                    "inCharset", "utf-8",
                    "outCharset", "utf-8"
            ), OkHttpClientUtils.addCookie(cookie))
            val secondJsonObject = OkHttpClientUtils.getJson(secondResponse)
            result += if (secondJsonObject.getInteger("code") == 0)
                "微视签到成功!!"
            else "微视签到失败！${secondJsonObject.getString("message")}!!"
            val headers = OkHttpClientUtils.addHeaders(
                    "wesee_fe_map_ext", "{\"deviceInfoHeader\":\"i=undefined\",\"qimei\":\"7e8454fad0148911\",\"imei\":\"\"}",
                    "referer", "https://isee.weishi.qq.com/ws/app-pages/task_center/index.html?h5from=center&offlineMode=1&h5_data_report={%22navstyle%22:%222%22,%22needlogin%22:%221%22,%22_wv%22:%224096%22}&titleh=55.0&statush=27.272728",
                    "cookie", cookie,
                    "user-agent", "V1_AND_WEISHI_6.8.0_590_435013001_D/Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.92 Mobile Safari/537.36 QQJSSDK/1.3"
            )
            val thirdResponse = OkHttpClientUtils.post("https://api.weishi.qq.com/trpc.weishi.weishi_h5_proxy.weishi_h5_proxy/GetUserTaskList?g_tk=$gtk",
                    OkHttpClientUtils.addJson("{\"msg\":\"{\\\"sceneId\\\":1003,\\\"extInfo\\\":{}}\"}"),
                    headers
            )
            val thirdJsonObject = OkHttpClientUtils.getJson(thirdResponse)
            if (thirdJsonObject.getInteger("ret") == 0){
                val jsonObject = JSON.parseObject(thirdJsonObject.getString("msg"))
                val taskJsonObject = jsonObject.getJSONObject("taskInfoMp")
                var id = 0
                for (i in taskJsonObject){
                    val singleJsonObject = i.value as JSONObject
                    val taskInfoJsonObject = singleJsonObject.getJSONObject("taskInfoCfg")
                    if (taskInfoJsonObject.getString("taskName") == "QQ等级加速"){
                        id = taskInfoJsonObject.getInteger("taskId")
                        break
                    }
                }
                val forthResponse = OkHttpClientUtils.post("https://api.weishi.qq.com/trpc.weishi.weishi_h5_proxy.weishi_h5_proxy/ObtainTaskReward?g_tk=$gtk",
                        OkHttpClientUtils.addJson("{\"msg\":\"{\\\"taskId\\\":$id}\"}"), headers)
                val forthJsonObject = OkHttpClientUtils.getJson(forthResponse)
                result += when (forthJsonObject.getInteger("ret")){
                    0 -> "微视服务加速成功！成长值+0.5天"
                    2007 -> "微视服务今天已完成加速！"
                    else -> "微视领取任务奖励失败！${forthJsonObject.getString("err_msg")}"
                }
                result
            }else {
                result += "QQ微视获取任务详情失败！${thirdJsonObject.getString("err_msg")}"
                result
            }
        }else "微视签到失败，请更新QQ！"
    }

    private fun getGroupFileList(qqEntity: QQEntity, group: Long, folderName: String?, folderId: String?): CommonResult<List<Map<String, String>>>{
        val response = OkHttpClientUtils.get("https://pan.qun.qq.com/cgi-bin/group_file/get_file_list?gc=$group&bkn=${qqEntity.getGtk()}&start_index=0&cnt=30&filter_code=0&folder_id=${folderId ?: "%2F"}",
                qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> {
                val filesJsonArray = jsonObject.getJSONArray("file_list")
                //获取成功
                val list = mutableListOf<Map<String, String>>()
                var id: String? = null
                for (i in filesJsonArray.indices){
                    val fileJsonObject = filesJsonArray.getJSONObject(i)
                    if (folderName == null) {
                        if (fileJsonObject.getInteger("type") == 1) {
                            val map = mapOf(
                                    "busId" to fileJsonObject.getString("bus_id"),
                                    "id" to fileJsonObject.getString("id"),
                                    "name" to fileJsonObject.getString("name")
                            )
                            list.add(map)
                        }
                    }else{
                        if (fileJsonObject.getInteger("type") == 2){
                            if (fileJsonObject.getString("name") == folderName){
                                id = URLEncoder.encode(fileJsonObject.getString("id"), "utf-8")
                                break
                            }
                        }
                    }
                }
                if (id != null) {
                    return this.getGroupFileList(qqEntity, group, null, id)
                }
                if (folderName != null) return CommonResult(500, "没有找到该文件夹")
                CommonResult(200, "", list)
            }
            -107 -> CommonResult(500, "获取群文件失败，您还没有加入该群！！")
            4 -> CommonResult(500, "获取群文件失败，请更新QQ！")
            else -> CommonResult(500, "获取群文件失败，${jsonObject.getString("em")}")
        }
    }

    private fun getGroupFileUrl(qqEntity: QQEntity, group: Long, busId: String, id: String): String{
        val response = OkHttpClientUtils.get("https://pan.qun.qq.com/cgi-bin/group_share_get_downurl?uin=${qqEntity.qq}&groupid=$group&pa=${URLEncoder.encode("/$busId$id", "utf-8")}&r=0.${BotUtils.randomNum(16)}&charset=utf-8&g_tk=${qqEntity.getGtk()}&callback=_Callback",
                qqEntity.cookie())
        val jsonObject = OkHttpClientUtils.getJsonp(response)
        return if (jsonObject.getInteger("code") == 0){
            jsonObject.getJSONObject("data").getString("url")
        }else "获取链接失败！！！"
    }

    override fun groupFileUrl(qqEntity: QQEntity, group: Long, folderName: String?): String {
        val commonResult = this.getGroupFileList(qqEntity, group, folderName, null)
        return if (commonResult.code == 200){
            val sb = StringBuilder("本群的目录<${folderName?: "/"}>的群文件如下：\r\n")
            val list = commonResult.t
            for (map in list){
                val url = this.getGroupFileUrl(qqEntity, group, map.getValue("busId"), map.getValue("id"))
                sb.appendln("文件名：${map.getValue("name")}")
                sb.appendln("链接：${BotUtils.shortUrl(url)}")
                sb.appendln("--------------")
            }
            sb.removeSuffix("\r\n").toString()
        }else commonResult.msg
    }
}
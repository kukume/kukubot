@file:Suppress("DuplicatedCode")

package me.kuku.yuq.logic

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.QqMusicEntity
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.utils.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Service

@Service
class QqMusicLogic {

    private val appId = 716027609L
    private val daId = 383
    private val ptAid = 100497308L

    private suspend fun getSign(params: String): String{
        val jsonObject = OkHttpKtUtils.postJson("https://api.kukuqaq.com/exec/qqMusic", mutableMapOf("data" to params))
        return jsonObject.getString("sign")
    }

    suspend fun getQrcode(): QqLoginQrcode {
        return QqQrCodeLoginUtils.getQrCode(appId, daId, ptAid)
    }

    suspend fun checkQrcode(qqLoginQrcode: QqLoginQrcode): Result<QqMusicEntity> {
        val result = QqQrCodeLoginUtils.checkQrCode(appId, daId, ptAid,
            "https://graph.qq.com/oauth2.0/login_jump", qqLoginQrcode.sig
        )
        if (result.isFailure) return Result.failure(result.code, result.message)
        val urlResult = QqQrCodeLoginUtils.authorize(result.data, ptAid, "state",
            "https://y.qq.com/portal/wx_redirect.html?login_type=1&surl=https://y.qq.com/"
        )
        if (urlResult.isFailure) return Result.failure(urlResult.message)
        val url = urlResult.data
        val code = MyUtils.regex("code=", "&", url)
        val response = OkHttpKtUtils.post(
            "https://u.y.qq.com/cgi-bin/musicu.fcg",
            OkUtils.json("{\"comm\":{\"g_tk\":5381,\"platform\":\"yqq\",\"ct\":24,\"cv\":0},\"req\":{\"module\":\"QQConnectLogin.LoginServer\",\"method\":\"QQLogin\",\"param\":{\"code\":\"$code\"}}}")
        )
        response.close()
        val cookie = OkUtils.cookie(response)
        return Result.success(QqMusicEntity().also {
            it.cookie = cookie
        })
    }

    suspend fun loginByPassword(qq: Long, password: String): Result<QqMusicEntity> {
        val result = QqPasswordConnectLoginUtils.login(
            qq,
            password,
            100497308L,
            "https://y.qq.com/portal/wx_redirect.html?login_type=1&surl=https://y.qq.com/"
        )
        return if (result.failure()) Result.failure(result.message)
        else {
            val url = result.data()
            val code = MyUtils.regex("(?<=code\\=).*", url)
            val response = OkHttpKtUtils.post(
                "https://u.y.qq.com/cgi-bin/musicu.fcg",
                OkUtils.json("{\"comm\":{\"g_tk\":5381,\"platform\":\"yqq\",\"ct\":24,\"cv\":0},\"req\":{\"module\":\"QQConnectLogin.LoginServer\",\"method\":\"QQLogin\",\"param\":{\"code\":\"$code\"}}}")
            )
            response.close()
            val cookie = OkUtils.cookie(response)
            Result.success(QqMusicEntity().also {
                it.cookie = cookie
            })
        }
    }

    suspend fun sign(qqMusicEntity: QqMusicEntity): Result<Void> {
        val jsonObject = OkHttpKtUtils.postJson(
            "https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=DoSignIn&_=" + System.currentTimeMillis(),
            OkUtils.json("{\"comm\":{\"g_tk\":5381,\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"h5\",\"needNewCode\":1,\"ct\":23,\"cv\":0,\"uid\":\"4380989133\"},\"req_0\":{\"module\":\"music.actCenter.ActCenterSignNewSvr\",\"method\":\"DoSignIn\",\"param\":{\"ActID\":\"PR-Config20200828-31525466015\"}}}"),
            OkUtils.cookie(qqMusicEntity.cookie)
        )
        return when (jsonObject.getJSONObject("req_0").getInteger("code")) {
            0 -> Result.success("qq音乐签到成功！", null)
            200002 -> Result.success("qq音乐今日已签到", null)
            1000 -> Result.failure("qq音乐签到失败，cookie已失效，请重新登录！")
            else -> Result.failure("qq音乐签到失败，未知错误")
        }
    }

    suspend fun musicianSign(qqMusicEntity: QqMusicEntity): Result<Void> {
        val qqMusicKey = qqMusicEntity.qqMusicKey
        val jsonObject = OkHttpKtUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=reportUserTask&_=${System.currentTimeMillis()}",
            OkUtils.json("{\"req_0\":{\"module\":\"music.sociality.KolTask\",\"method\":\"reportUserTask\",\"param\":{\"type\":0,\"count\":1,\"op\":0}},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"yqq\"}}"),
            OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (jsonObject.getInteger("code") == 0 && jsonObject.getJSONObject("req_0").getInteger("code") == 0)
            Result.success("qq音乐人签到成功！", null)
        else Result.failure("qq音乐人签到失败！")
    }

    suspend fun publishNews(qqMusicEntity: QqMusicEntity, content: String): Result<Void> {
        val qqMusicKey = qqMusicEntity.qqMusicKey
        val preJsonObject = OkHttpKtUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=pre_submit_moment&_=${System.currentTimeMillis()}",
            OkUtils.json("{\"req_0\":{\"method\":\"pre_submit_moment\",\"param\":{\"cmd\":0,\"moment\":{\"type\":0,\"v_media\":[],\"v_pic\":[],\"v_tag\":[],\"v_track\":[],\"community\":{},\"v_topic\":[],\"content\":\"$content\"}},\"module\":\"music.magzine.MomentWrite\"},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"yqq\",\"ct\":24,\"cv\":0}}"),
            OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (preJsonObject.getInteger("code") == 0 && preJsonObject.getJSONObject("req_0").getInteger("code") == 0){
            val id = preJsonObject.getJSONObject("req_0").getJSONObject("data").getString("encrypt_moid")
            val jsonObject = OkHttpKtUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=submit_moment&_=${System.currentTimeMillis()}",
                OkUtils.json("{\"req_0\":{\"method\":\"submit_moment\",\"param\":{\"cmd\":0,\"moment\":{\"type\":0,\"v_media\":[],\"v_pic\":[],\"v_tag\":[],\"v_track\":[],\"community\":{},\"v_topic\":[],\"content\":\"$content\",\"encrypt_moid\":\"$id\"}},\"module\":\"music.magzine.MomentWrite\"},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"yqq\"}}"),
                OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
            if (jsonObject.getInteger("code") == 0 && jsonObject.getJSONObject("req_0").getInteger("code") == 0)
                Result.success("qq音乐发送动态成功！", null)
            else Result.failure("qq音乐发送动态失败！")
        }else Result.failure("qq音乐发送动态失败！可能cookie已失效！")
    }

    suspend fun comment(qqMusicEntity: QqMusicEntity, id: Int, content: String): Result<String> {
        val qqMusicKey = qqMusicEntity.qqMusicKey
        val gtk = QqUtils.getGTK(qqMusicKey)
        val params = "{\"comm\":{\"cv\":4747474,\"ct\":24,\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"yqq.json\",\"needNewCode\":1,\"uin\":${qqMusicEntity.qqEntity?.qq},\"g_tk_new_20200303\":$gtk,\"g_tk\":$gtk},\"req_1\":{\"module\":\"music.globalComment.CommentWriteServer\",\"method\":\"AddComment\",\"param\":{\"BizType\":1,\"BizId\":\"$id\",\"Content\":\"$content\"}}}"
        val jsonObject = OkHttpKtUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_=${System.currentTimeMillis()}",
            OkUtils.json(params),
            OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (jsonObject.getInteger("code") == 0)
            when (jsonObject.getJSONObject("req_1").getInteger("code")){
                0 -> Result.success("qq音乐评论成功", jsonObject.getJSONObject("req_1").getJSONObject("data").getString("AddedCmId"))
                10009 -> {
                    val url =
                        jsonObject.getJSONObject("req_1").getJSONObject("data").getString("VerifyUrl")
                    val res = identifyCaptcha(qqMusicEntity, url)
                    if (res.isFailure)
                        Result.failure("需要验证验证码，请打开该链接进行验证并重新发送该指令：$url")
                    else {
                        comment(qqMusicEntity, id, content)
                    }
                }
                else -> Result.failure("qq音乐随机歌曲评论失败！${jsonObject.getJSONObject("req_1")?.getJSONObject("data")?.getString("Msg") ?: "可能cookie已失效！"}")
            }
        else Result.failure("qq音乐评论失败，" + jsonObject.getJSONObject("req_1").getString("errmsg"))
    }

    suspend fun replyComment(qqMusicEntity: QqMusicEntity, content: String): Result<Void> {
        val str = OkHttpKtUtils.getStr("https://i.y.qq.com/n2/m/share/profile_v2/index.html",
            OkUtils.headers(qqMusicEntity.cookie, "", UA.MOBILE))
        val jsonStr = MyUtils.regex("firstPageData = ", "</sc", str) ?: return Result.failure("获取歌曲失败，cookie已失效！")
        val jsonObject = JSON.parseObject(jsonStr)
        val singleJsonObject = jsonObject?.getJSONObject("tabData")?.getJSONObject("song")?.getJSONArray("list")
            ?.random() as? JSONObject
        val id = singleJsonObject?.getString("id") ?: "319485192"
        val res = comment(qqMusicEntity, id.toInt(), content)
        val mid = res.data
        return replyComment(qqMusicEntity, id, mid, OkHttpKtUtils.getStr("https://v1.hitokoto.cn/?encode=text"))
    }

    private suspend fun replyComment(qqMusicEntity: QqMusicEntity, songId: String, cmId: String, content: String): Result<Void>{
        val qq = qqMusicEntity.qqEntity?.qq ?: 0
        val gtk = QqUtils.getGTK(qqMusicEntity.qqMusicKey)
        val resultJsonObject = OkHttpKtUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_=${System.currentTimeMillis()}",
            OkUtils.json("{\"comm\":{\"cv\":4747474,\"ct\":24,\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"yqq.json\",\"needNewCode\":1,\"uin\":$qq,\"g_tk_new_20200303\":$gtk,\"g_tk\":$gtk},\"req_1\":{\"module\":\"music.globalComment.CommentWriteServer\",\"method\":\"AddComment\",\"param\":{\"BizType\":1,\"BizId\":\"$songId\",\"Content\":\"$content\",\"RepliedCmId\":\"$cmId\"}}}"),
            OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (resultJsonObject.getInteger("code") == 0) {
            when (resultJsonObject.getJSONObject("req_1").getInteger("code")) {
                0 -> Result.success("qq音乐随机歌曲评论成功！", null)
                10009 -> {
                    val url =
                        resultJsonObject.getJSONObject("req_1").getJSONObject("data").getString("VerifyUrl")
                    val res = identifyCaptcha(qqMusicEntity, url)
                    if (res.isFailure)
                        Result.failure("需要验证验证码，请打开该链接进行验证并重新发送该指令：$url")
                    else {
                        replyComment(qqMusicEntity, songId, cmId, content)
                    }
                }
                else -> Result.failure("qq音乐随机歌曲评论失败！${resultJsonObject.getJSONObject("req_1")?.getJSONObject("data")?.getString("Msg") ?: "可能cookie已失效！"}")
            }
        }
        else Result.failure("qq音乐随机歌曲评论失败！")
    }

    suspend fun randomReplyComment(qqMusicEntity: QqMusicEntity, content: String): Result<Void> {
        val html = OkHttpUtils.getStr("https://y.qq.com/n/ryqq/toplist/4")
        val jsonStr = MyUtils.regex("window.__INITIAL_DATA__ =", "</sc", html)!!.replace("undefined", "\"\"")
        val jsonObject = JSON.parseObject(jsonStr)
        val songJsonObject = jsonObject.getJSONObject("data").getJSONArray("song").random() as JSONObject
        val songId = songJsonObject.getString("songId")
        val albumMid = songJsonObject.getString("albumMid")
        val qq = qqMusicEntity.qqEntity?.qq
        val gtk = QqUtils.getGTK(qqMusicEntity.qqMusicKey)
        val params = "{\"comm\":{\"cv\":4747474,\"ct\":24,\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"yqq.json\",\"needNewCode\":1,\"uin\":$qq,\"g_tk_new_20200303\":$gtk,\"g_tk\":$gtk},\"req_1\":{\"method\":\"GetCommentCount\",\"module\":\"GlobalComment.GlobalCommentReadServer\",\"param\":{\"request_list\":[{\"biz_type\":1,\"biz_id\":\"$songId\",\"biz_sub_type\":0}]}},\"req_2\":{\"module\":\"music.musicasset.SongFavRead\",\"method\":\"IsSongFanByMid\",\"param\":{\"v_songMid\":[\"$albumMid\"]}},\"req_3\":{\"module\":\"music.globalComment.CommentReadServer\",\"method\":\"GetNewCommentList\",\"param\":{\"BizType\":1,\"BizId\":\"$songId\",\"LastCommentSeqNo\":\"\",\"PageSize\":25,\"PageNum\":0,\"FromCommentId\":\"\",\"WithHot\":1}},\"req_4\":{\"module\":\"music.globalComment.CommentReadServer\",\"method\":\"GetHotCommentList\",\"param\":{\"BizType\":1,\"BizId\":\"$songId\",\"LastCommentSeqNo\":\"\",\"PageSize\":15,\"PageNum\":0,\"HotType\":2,\"WithAirborne\":1}},\"req_5\":{\"module\":\"userInfo.VipQueryServer\",\"method\":\"SRFVipQuery_V2\",\"param\":{\"uin_list\":[\"$qq\"]}},\"req_6\":{\"module\":\"userInfo.BaseUserInfoServer\",\"method\":\"get_user_baseinfo_v2\",\"param\":{\"vec_uin\":[\"$qq\"]}},\"req_7\":{\"module\":\"MessageCenter.MessageCenterServer\",\"method\":\"GetMessage\",\"param\":{\"uin\":\"$qq\",\"red_dot\":[{\"msg_type\":1}]}},\"req_8\":{\"module\":\"GlobalComment.GlobalCommentMessageReadServer\",\"method\":\"GetMessage\",\"param\":{\"uin\":\"$qq\",\"page_num\":0,\"page_size\":1,\"last_msg_id\":\"\",\"type\":0}}}"
        val commentsJsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_=${System.currentTimeMillis()}",
            OkUtils.json(params),
            OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (commentsJsonObject.getInteger("code") == 0 && commentsJsonObject.getJSONObject("req_3").getInteger("code") == 0){
            val commentJsonObject =
                commentsJsonObject.getJSONObject("req_3").getJSONObject("data").getJSONObject("CommentList2")
                    .getJSONArray("Comments").random() as JSONObject
            val cmId = commentJsonObject.getString("CmId")
            replyComment(qqMusicEntity, songId, cmId, content)
        }else Result.failure("qq音乐随机歌曲评论失败！获取评论列表失败！")
    }

    suspend fun convertGreenDiamond(qqMusicEntity: QqMusicEntity): Result<Void> {
        val params = "{\"req_0\":{\"module\":\"music.sociality.KolUserRight\",\"method\":\"getOrderId\",\"param\":{\"userRedeemType\":3,\"greenVipNum\":1,\"greenVipType\":0,\"creditType\":0}},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicEntity.qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"h5\",\"ct\":23,\"cv\":0}}"
        val jsonObject = OkHttpKtUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_=" + System.currentTimeMillis(),
            OkUtils.json(params), OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        val innerJsonObject = jsonObject.getJSONObject("req_0")
        return if (jsonObject.getInteger("code") == 0 && innerJsonObject.getInteger("code") == 0) {
            val orderId = innerJsonObject.getJSONObject("data").getString("orderId")
            val getParams = "{\"req_0\":{\"module\":\"music.sociality.KolUserRight\",\"method\":\"redeemGreenVip\",\"param\":{\"num\":1,\"greenVipType\":0,\"creditType\":0,\"orderId\":\"$orderId\"}},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicEntity.qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"h5\",\"ct\":23,\"cv\":0}}"
            val getJsonObject = OkHttpKtUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_=" + System.currentTimeMillis(),
                OkUtils.json(getParams), OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
            if (getJsonObject.getInteger("code") == 0 && getJsonObject.getJSONObject("req_0").getInteger("code") == 0)
                Result.success("兑换绿钻一个月成功！", null)
            else Result.failure("兑换绿钻失败：${getJsonObject.getJSONObject("req_0")?.getJSONObject("data")?.getString("retMsg") ?: "可能cookie已失效"}")
        }else Result.failure("兑换绿钻失败：${jsonObject.getJSONObject("req_0")?.getJSONObject("data")?.getString("retMsg") ?: "可能cookie已失效"}")
    }

    private suspend fun identifyCaptcha(qqMusicEntity: QqMusicEntity, url: String): Result<Any>{
        val appId = MyUtils.regex("appid=", "&", url)
        val msgId = MyUtils.regex("msgid=", "&", url)
        val gtk = QqUtils.getGTK(qqMusicEntity.qqMusicKey)
        val qq = qqMusicEntity.qqEntity?.qq ?: 0
        val preJsonObject = OkHttpKtUtils.getJsonp(
            "https://safety.music.qq.com/cgi/fcgi-bin/fcg_music_validate?iSubCmd=70&iAppid=$appId&iCaptchaType=8&msgid=$msgId&iDisturbLevel=2&clientid=10&g_tk=$gtk&g_tk_new_20200303=$gtk&uin=$qq&format=jsonp&inCharset=utf-8&outCharset=utf-8&notice=0&platform=h5&needNewCode=1&callback=MusicJsonCallback",
            OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.QQ)
        )
        if (preJsonObject.getInteger("code") != 0) return Result.failure(preJsonObject.getString("message"))
        val preDataJsonObject = preJsonObject.getJSONObject("data")
        val strCode = preDataJsonObject.getString("strCode")
        val strPic = preDataJsonObject.getString("strPic")
        val position = preDataJsonObject.getString("position")
        val identifyJsonObject = OkHttpKtUtils.postJson("https://api.kukuqaq.com/tool/qqMusicCaptchaByTt", mapOf("image" to strPic))
        if (identifyJsonObject.getInteger("code") != 200) return Result.failure(identifyJsonObject.getString("message"))
        val width = identifyJsonObject.getJSONObject("data").getInteger("data") + 26
        val height = MyUtils.regex(",", "]", position)!!.trim()
        val a = "[$width,%20$height]]"
        val resJsonObject = OkHttpKtUtils.getJsonp("https://safety.music.qq.com/cgi/fcgi-bin/fcg_music_validate?iSubCmd=71&iAppid=$appId&msgid=$msgId&strCode=$strCode&strSig=$a&strTk={%22device%22:{%22screenWidth%22:400,%22screenHeight%22:700,%22devicePixelRatio%22:1},%22captcha%22:{%22isFullScreen%22:true}}&clientid=10&g_tk=$gtk&g_tk_new_20200303=$gtk&uin=$qq&format=jsonp&inCharset=utf-8&outCharset=utf-8&notice=0&platform=h5&needNewCode=1&callback=MusicJsonCallback",
            OkUtils.headers(qqMusicEntity.cookie, "https://y.qq.com", UA.QQ))
        val b = resJsonObject.getInteger("code") == 0 && resJsonObject.getJSONObject("data").getInteger("iRet") == 0
        return if (b) Result.success("识别成功！", null)
        else Result.failure(resJsonObject.getJSONObject("data").getString("strErrMsg"))
    }

    suspend fun daySign(qqMusicEntity: QqMusicEntity): Result<Void> {
        val data = "{\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicEntity.qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq ?: 0},\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"h5\",\"needNewCode\":1,\"ct\":23,\"cv\":0},\"req_0\":{\"module\":\"music.actCenter.DaysignactSvr\",\"method\":\"doSignIn\",\"param\":{}}}"
        val jsonObject = OkHttpKtUtils.postJson("https://u.y.qq.com/cgi-bin/musics.fcg?_webcgikey=doSignIn&_=${System.currentTimeMillis()}&sign=${getSign(data)}",
            OkUtils.json(data), OkUtils.headers(qqMusicEntity.cookie, "https://i.y.qq.com/n2/m/client/day_sign/index.html",
                UA.QQ))
        return if (jsonObject.getInteger("code") == 0) {
            val innerJsonObject = jsonObject.getJSONObject("req_0")
            when (innerJsonObject.getInteger("code")){
                0 -> {
                    val ss = innerJsonObject.getJSONObject("data").getInteger("code")
                    if (ss == 0)
                        Result.success("qq音乐日签成功！", null)
                    else Result.success("qq音乐今日已日签！", null)
                }
                1000 -> Result.failure("qq音乐日签失败，cookie已失效！", null)
                else -> Result.failure("qq音乐日签失败，未知原因！")
            }
        }else Result.failure("sign验证失败！")
    }

    suspend fun shareMusic(qqMusicEntity: QqMusicEntity): Result<Void> {
        val str = OkHttpKtUtils.getStr("https://i.y.qq.com/n2/m/share/profile_v2/index.html",
            OkUtils.headers(qqMusicEntity.cookie, "", UA.MOBILE))
        val jsonStr = MyUtils.regex("firstPageData = ", "</sc", str) ?: return Result.failure("获取歌曲失败，cookie已失效！")
        val jsonObject = JSON.parseObject(jsonStr)
        val singleJsonObject = jsonObject?.getJSONObject("tabData")?.getJSONObject("song")?.getJSONArray("list")
            ?.random() as? JSONObject
        val id = singleJsonObject?.getString("id") ?: 319485192
        val singerId = singleJsonObject?.getJSONArray("singer")?.getJSONObject(0)?.getInteger("id") ?: 9005038
        val cookie = qqMusicEntity.cookie
        val openId = OkUtils.cookie(cookie, "psrf_qqopenid")
        val expires = OkUtils.cookie(cookie, "psrf_access_token_expiresAt")
        val accessToken = OkUtils.cookie(cookie, "psrf_qqaccess_token")
        val qq = qqMusicEntity.qqEntity?.qq
        val qqMusicKey = qqMusicEntity.qqMusicKey
        val params = """
            <?xml version="1.0" encoding="UTF-8"?><root><tmeLoginType>2</tmeLoginType><psrf_qqopenid>$openId</psrf_qqopenid><psrf_access_token_expiresAt>$expires</psrf_access_token_expiresAt><tid>628985721295008768</tid><OpenUDID>ffffffffefeffc54000000000033c587</OpenUDID><udid>ffffffffefeffc54000000000033c587</udid><ct>11</ct><qq>$qq</qq><authst>$qqMusicKey</authst><psrf_qqaccess_token>$accessToken</psrf_qqaccess_token><item cmd="17" optime="${System.currentTimeMillis() / 1000}" nettype="1020" QQ="$qq" uid="4380989133" os="11" model="RMX3031" version="10.18.0.5" songid="$id" singerid="$singerId" songtype="1" count="1" sharetype="1" source="3" share_prompt="0"/></root>
        """.trimIndent()
        val response = OkHttpUtils.post("https://stat6.y.qq.com/android/fcgi-bin/imusic_tj",
            GZipUtils.gzip(params.toByteArray()).toRequestBody() , OkUtils.ua("QQMusic 10180005(android 11)"))
        response.close()
        return Result.success()
    }

}
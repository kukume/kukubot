package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import me.kuku.pojo.QqLoginQrcode
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.utils.*
import me.kuku.yuq.entity.QqMusicEntity

@AutoBind
interface QqMusicLogic {
    fun getQrcode(): QqLoginQrcode
    fun checkQrcode(qqLoginQrcode: QqLoginQrcode): Result<QqMusicEntity>
    fun loginByPassword(qq: Long, password: String): Result<QqMusicEntity>
    fun sign(qqMusicEntity: QqMusicEntity): Result<Void>
    fun musicianSign(qqMusicEntity: QqMusicEntity): Result<Void>
    fun publishNews(qqMusicEntity: QqMusicEntity, content: String): Result<Void>
    fun comment(qqMusicEntity: QqMusicEntity, id: Int, content: String): Result<Void>
    fun randomReplyComment(qqMusicEntity: QqMusicEntity, content: String): Result<Void>
    fun convertGreenDiamond(qqMusicEntity: QqMusicEntity): Result<Void>
}

class QqMusicLogicImpl: QqMusicLogic{

    private val appId = 716027609L
    private val daId = 383
    private val ptAid = 100497308L

    private fun getSign(params: String): String{
        return "zza${MyUtils.randomStr(MyUtils.randomInt(10, 16))}${MD5Utils.toMD5("CJBPACrRuNy7$params")}"
    }

    override fun getQrcode(): QqLoginQrcode {
        return QqQrCodeLoginUtils.getQrCode(appId, daId, ptAid)
    }

    override fun checkQrcode(qqLoginQrcode: QqLoginQrcode): Result<QqMusicEntity> {
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
        val response = OkHttpUtils.post(
            "https://u.y.qq.com/cgi-bin/musicu.fcg",
            OkHttpUtils.addJson("{\"comm\":{\"g_tk\":5381,\"platform\":\"yqq\",\"ct\":24,\"cv\":0},\"req\":{\"module\":\"QQConnectLogin.LoginServer\",\"method\":\"QQLogin\",\"param\":{\"code\":\"$code\"}}}")
        )
        response.close()
        val cookie = OkHttpUtils.getCookie(response)
        val key = OkHttpUtils.getCookie(cookie, "qqmusic_key")
        return Result.success(QqMusicEntity(cookie = cookie, qqMusicKey = key))
    }

    override fun loginByPassword(qq: Long, password: String): Result<QqMusicEntity> {
        val result = QqPasswordConnectLoginUtils.login(
            qq,
            password,
            100497308L,
            "https://y.qq.com/portal/wx_redirect.html?login_type=1&surl=https://y.qq.com/"
        )
        return if (result.isFailure) Result.failure(result.message)
        else {
            val url = result.data
            val code = MyUtils.regex("(?<=code\\=).*", url)
            val response = OkHttpUtils.post(
                "https://u.y.qq.com/cgi-bin/musicu.fcg",
                OkHttpUtils.addJson("{\"comm\":{\"g_tk\":5381,\"platform\":\"yqq\",\"ct\":24,\"cv\":0},\"req\":{\"module\":\"QQConnectLogin.LoginServer\",\"method\":\"QQLogin\",\"param\":{\"code\":\"$code\"}}}")
            )
            response.close()
            val cookie = OkHttpUtils.getCookie(response)
            val key = OkHttpUtils.getCookie(cookie, "qqmusic_key")
            Result.success(QqMusicEntity(cookie = cookie, qqMusicKey = key))
        }
    }

    override fun sign(qqMusicEntity: QqMusicEntity): Result<Void> {
        val jsonObject = OkHttpUtils.postJson(
            "https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=DoSignIn&_=" + System.currentTimeMillis(),
            OkHttpUtils.addJson("{\"comm\":{\"g_tk\":5381,\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"h5\",\"needNewCode\":1,\"ct\":23,\"cv\":0,\"uid\":\"4380989133\"},\"req_0\":{\"module\":\"music.actCenter.ActCenterSignNewSvr\",\"method\":\"DoSignIn\",\"param\":{\"ActID\":\"PR-Config20200828-31525466015\"}}}"),
            OkHttpUtils.addCookie(qqMusicEntity.cookie)
        )
        return when (jsonObject.getJSONObject("req_0").getInteger("code")) {
            0 -> Result.success("qq音乐签到成功！", null)
            200002 -> Result.success("qq音乐今日已签到", null)
            1000 -> Result.failure("qq音乐签到失败，cookie已失效，请重新登录！")
            else -> Result.failure("qq音乐签到失败，未知错误")
        }
    }

    override fun musicianSign(qqMusicEntity: QqMusicEntity): Result<Void> {
        val qqMusicKey = qqMusicEntity.qqMusicKey ?: ""
//        if (qqMusicKey == null || qqMusicKey.isEmpty()){
//            qqMusicKey = OkHttpUtils.getCookie(qqMusicEntity.cookie, "qqmusic_key") ?: return Result.failure("没有取到qqMusicKey， 请重新登录！")
//        }
        val jsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=reportUserTask&_=${System.currentTimeMillis()}",
            OkHttpUtils.addJson("{\"req_0\":{\"module\":\"music.sociality.KolTask\",\"method\":\"reportUserTask\",\"param\":{\"type\":0,\"count\":1,\"op\":0}},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"yqq\"}}"),
            OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (jsonObject.getInteger("code") == 0 && jsonObject.getJSONObject("req_0").getInteger("code") == 0)
            Result.success("qq音乐人签到成功！", null)
        else Result.failure("qq音乐人签到失败！")
    }

    override fun publishNews(qqMusicEntity: QqMusicEntity, content: String): Result<Void> {
        val qqMusicKey = qqMusicEntity.qqMusicKey ?: ""
        val preJsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=pre_submit_moment&_=${System.currentTimeMillis()}",
            OkHttpUtils.addJson("{\"req_0\":{\"method\":\"pre_submit_moment\",\"param\":{\"cmd\":0,\"moment\":{\"type\":0,\"v_media\":[],\"v_pic\":[],\"v_tag\":[],\"v_track\":[],\"community\":{},\"v_topic\":[],\"content\":\"$content\"}},\"module\":\"music.magzine.MomentWrite\"},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"yqq\",\"ct\":24,\"cv\":0}}"),
            OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (preJsonObject.getInteger("code") == 0 && preJsonObject.getJSONObject("req_0").getInteger("code") == 0){
            val id = preJsonObject.getJSONObject("req_0").getJSONObject("data").getString("encrypt_moid")
            val jsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=submit_moment&_=${System.currentTimeMillis()}",
                OkHttpUtils.addJson("{\"req_0\":{\"method\":\"submit_moment\",\"param\":{\"cmd\":0,\"moment\":{\"type\":0,\"v_media\":[],\"v_pic\":[],\"v_tag\":[],\"v_track\":[],\"community\":{},\"v_topic\":[],\"content\":\"$content\",\"encrypt_moid\":\"$id\"}},\"module\":\"music.magzine.MomentWrite\"},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"yqq\"}}"),
                OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
            if (jsonObject.getInteger("code") == 0 && jsonObject.getJSONObject("req_0").getInteger("code") == 0)
                Result.success("qq音乐发送动态成功！", null)
            else Result.failure("qq音乐发送动态失败！")
        }else Result.failure("qq音乐发送动态失败！可能cookie已失效！")
    }

    override fun comment(qqMusicEntity: QqMusicEntity, id: Int, content: String): Result<Void> {
        val qqMusicKey = qqMusicEntity.qqMusicKey ?: ""
        val gtk = QqUtils.getGTK(qqMusicKey)
        val jsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=pre_submit_moment&_=${System.currentTimeMillis()}",
            OkHttpUtils.addJson("{\"comm\":{\"cv\":4747474,\"ct\":24,\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"yqq.json\",\"needNewCode\":1,\"uin\":${qqMusicEntity.qqEntity?.qq},\"g_tk_new_20200303\":$gtk,\"g_tk\":$gtk,\"req_1\":{\"module\":\"music.globalComment.CommentWriteServer\",\"method\":\"AddComment\",\"param\":{\"BizType\":1,\"BizId\":\"$id\",\"Content\":\"$content\"}}}"),
            OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (jsonObject.getInteger("code") == 0 && jsonObject.getJSONObject("req_1").getInteger("code") == 0)
            Result.success("qq音乐评论成功", null)
        else Result.failure("qq音乐评论失败，" + jsonObject.getJSONObject("req_1").getString("errmsg"))
    }

    override fun randomReplyComment(qqMusicEntity: QqMusicEntity, content: String): Result<Void> {
        val html = OkHttpUtils.getStr("https://y.qq.com/n/ryqq/toplist/4");
        val jsonStr = MyUtils.regex("window.__INITIAL_DATA__ =", "</sc", html).replace("undefined", "\"\"")
        val jsonObject = JSON.parseObject(jsonStr)
        val songJsonObject = jsonObject.getJSONObject("data").getJSONArray("song").random() as JSONObject
        val songId = songJsonObject.getString("songId")
        val albumMid = songJsonObject.getString("albumMid")
        val qq = qqMusicEntity.qqEntity?.qq
        val gtk = QqUtils.getGTK(qqMusicEntity.qqMusicKey ?: "")
        val params = "{\"comm\":{\"cv\":4747474,\"ct\":24,\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"yqq.json\",\"needNewCode\":1,\"uin\":$qq,\"g_tk_new_20200303\":$gtk,\"g_tk\":$gtk},\"req_1\":{\"method\":\"GetCommentCount\",\"module\":\"GlobalComment.GlobalCommentReadServer\",\"param\":{\"request_list\":[{\"biz_type\":1,\"biz_id\":\"$songId\",\"biz_sub_type\":0}]}},\"req_2\":{\"module\":\"music.musicasset.SongFavRead\",\"method\":\"IsSongFanByMid\",\"param\":{\"v_songMid\":[\"$albumMid\"]}},\"req_3\":{\"module\":\"music.globalComment.CommentReadServer\",\"method\":\"GetNewCommentList\",\"param\":{\"BizType\":1,\"BizId\":\"$songId\",\"LastCommentSeqNo\":\"\",\"PageSize\":25,\"PageNum\":0,\"FromCommentId\":\"\",\"WithHot\":1}},\"req_4\":{\"module\":\"music.globalComment.CommentReadServer\",\"method\":\"GetHotCommentList\",\"param\":{\"BizType\":1,\"BizId\":\"$songId\",\"LastCommentSeqNo\":\"\",\"PageSize\":15,\"PageNum\":0,\"HotType\":2,\"WithAirborne\":1}},\"req_5\":{\"module\":\"userInfo.VipQueryServer\",\"method\":\"SRFVipQuery_V2\",\"param\":{\"uin_list\":[\"$qq\"]}},\"req_6\":{\"module\":\"userInfo.BaseUserInfoServer\",\"method\":\"get_user_baseinfo_v2\",\"param\":{\"vec_uin\":[\"$qq\"]}},\"req_7\":{\"module\":\"MessageCenter.MessageCenterServer\",\"method\":\"GetMessage\",\"param\":{\"uin\":\"$qq\",\"red_dot\":[{\"msg_type\":1}]}},\"req_8\":{\"module\":\"GlobalComment.GlobalCommentMessageReadServer\",\"method\":\"GetMessage\",\"param\":{\"uin\":\"$qq\",\"page_num\":0,\"page_size\":1,\"last_msg_id\":\"\",\"type\":0}}}"
        val commentsJsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_=${System.currentTimeMillis()}",
            OkHttpUtils.addJson(params),
            OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        return if (commentsJsonObject.getInteger("code") == 0 && commentsJsonObject.getJSONObject("req_3").getInteger("code") == 0){
            val commentJsonObject =
                commentsJsonObject.getJSONObject("req_3").getJSONObject("data").getJSONObject("CommentList2")
                    .getJSONArray("Comments").random() as JSONObject
            val cmId = commentJsonObject.getString("CmId")
            val resultJsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musicu.fcg?_webcgikey=AddComment&_=${System.currentTimeMillis()}",
                OkHttpUtils.addJson("{\"comm\":{\"cv\":4747474,\"ct\":24,\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"yqq.json\",\"needNewCode\":1,\"uin\":$qq,\"g_tk_new_20200303\":$gtk,\"g_tk\":$gtk},\"req_1\":{\"module\":\"music.globalComment.CommentWriteServer\",\"method\":\"AddComment\",\"param\":{\"BizType\":1,\"BizId\":\"$songId\",\"Content\":\"$content\",\"RepliedCmId\":\"$cmId\"}}}"),
                OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
            if (resultJsonObject.getInteger("code") == 0) {
                val code = resultJsonObject.getJSONObject("req_1").getInteger("code")
                when (code) {
                    0 -> Result.success("qq音乐随机歌曲评论成功！", null)
                    10009 -> Result.failure("需要验证验证码，请打开该链接进行验证并重新发送该指令：${resultJsonObject.getJSONObject("req_1").getJSONObject("data").getString("VerifyUrl")}")
                    else -> Result.failure("qq音乐随机歌曲评论失败！${resultJsonObject?.getJSONObject("req_1")?.getJSONObject("data")?.getString("Msg") ?: "可能cookie已失效！"}")
                }
            }
            else Result.failure("qq音乐随机歌曲评论失败！")
        }else Result.failure("qq音乐随机歌曲评论失败！获取评论列表失败！")
    }

    override fun convertGreenDiamond(qqMusicEntity: QqMusicEntity): Result<Void> {
        val params = "{\"req_0\":{\"module\":\"music.sociality.KolUserRight\",\"method\":\"getOrderId\",\"param\":{\"userRedeemType\":3,\"greenVipNum\":1,\"greenVipType\":0,\"creditType\":0}},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicEntity.qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"h5\",\"ct\":23,\"cv\":0}}"
        val jsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musics.fcg?sign=${getSign(params)}&_=" + System.currentTimeMillis(),
            OkHttpUtils.addJson(params), OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        val innerJsonObject = jsonObject.getJSONObject("req_0")
        return if (jsonObject.getInteger("code") == 0 && innerJsonObject.getInteger("code") == 0) {
            val orderId = innerJsonObject.getJSONObject("data").getString("orderId")
            val getParams = "{\"req_0\":{\"module\":\"music.sociality.KolUserRight\",\"method\":\"redeemGreenVip\",\"param\":{\"num\":1,\"greenVipType\":0,\"creditType\":0,\"orderId\":\"$orderId\"}},\"comm\":{\"g_tk\":${QqUtils.getGTK(qqMusicEntity.qqMusicKey)},\"uin\":${qqMusicEntity.qqEntity?.qq},\"format\":\"json\",\"platform\":\"h5\",\"ct\":23,\"cv\":0}}";
            val getJsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musics.fcg?sign=${getSign(getParams)}&_=" + System.currentTimeMillis(),
                OkHttpUtils.addJson(getParams), OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
            if (getJsonObject.getInteger("code") == 0 && getJsonObject.getJSONObject("req_0").getInteger("code") == 0)
                Result.success("兑换绿钻一个月成功！", null)
            else Result.failure("兑换绿钻失败：${getJsonObject?.getJSONObject("req_0")?.getJSONObject("data")?.getString("retMsg") ?: "可能cookie已失效"}")
        }else Result.failure("兑换绿钻失败：${jsonObject?.getJSONObject("req_0")?.getJSONObject("data")?.getString("retMsg") ?: "可能cookie已失效"}")
    }
}
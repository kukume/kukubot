package me.kuku.simbot.logic

import me.kuku.pojo.QqLoginQrcode
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.simbot.entity.QqMusicEntity
import me.kuku.utils.MyUtils
import me.kuku.utils.OkHttpUtils
import me.kuku.utils.QqQrCodeLoginUtils
import org.springframework.stereotype.Service

interface QqMusicLogic {
    fun getQrcode(): QqLoginQrcode
    fun checkQrcode(qqLoginQrcode: QqLoginQrcode): Result<QqMusicEntity>
    fun sign(qqMusicEntity: QqMusicEntity): Result<Void>
    fun musicianSign(qqMusicEntity: QqMusicEntity): Result<Void>
}

@Service
class QqMusicLogicImpl: QqMusicLogic{

    private val appId = 716027609L
    private val daId = 383
    private val ptAid = 100497308L

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
        return Result.success(QqMusicEntity(cookie = cookie))
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
        val jsonObject = OkHttpUtils.postJson("https://u.y.qq.com/cgi-bin/musics.fcg?sign=zzb756ab6d3fvieelwxdlojczluhkkoqa2f79c3b&_=${System.currentTimeMillis()}",
            OkHttpUtils.addJson("{\"req_0\":{\"module\":\"music.sociality.KolTask\",\"method\":\"reportUserTask\",\"param\":{\"type\":0,\"count\":1,\"op\":0}},\"comm\":{\"g_tk\":1953844249,\"uin\":734669014,\"format\":\"json\",\"platform\":\"yqq\"}}"),
            OkHttpUtils.addHeaders(qqMusicEntity.cookie, "https://y.qq.com", UA.PC))
        if (jsonObject.getInteger("code") == 0 && jsonObject.getJSONObject("req_0").getInteger("code") == 0)
            return Result.success("qq音乐人签到成功！", null)
        else return Result.failure("qq音乐人签到失败！")
    }
}
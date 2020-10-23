package me.kuku.yuq.utils

import me.kuku.yuq.entity.QQLoginEntity
import me.kuku.yuq.pojo.CommonResult

object QQSuperLoginUtils {

    private fun login(qqLoginEntity: QQLoginEntity, appId: String, daId: String, prefixUrl: String, suffixUrl: String): CommonResult<String> {
        val response = OkHttpClientUtils.get("https://ssl.ptlogin2.qq.com/pt4_auth?daid=$daId&appid=$appId&auth_token=${qqLoginEntity.getToken()}", OkHttpClientUtils.addHeaders(
                "cookie", qqLoginEntity.getCookieWithSuper(),
                "referer", "https://ui.ptlogin2.qq.com/cgi-bin/login"
        ))
        val str = OkHttpClientUtils.getStr(response)
        val commonResult = QQUtils.getPtToken(str)
        return if (commonResult.code == 200){
            val map = QQUtils.getKey(commonResult.t!!, qqLoginEntity.qq.toString(), prefixUrl, suffixUrl)
            return CommonResult(200, "成功", map.getValue("p_skey"))
        }else CommonResult(500, commonResult.msg)
    }

    fun vipLogin(qqLoginEntity: QQLoginEntity): CommonResult<String>{
        return this.login(qqLoginEntity, "8000212", "18", "ptlogin2.vip.qq.com", "&daid=18&pt_login_type=4&service=pt4_auth&pttype=2&regmaster=&aid=8000212&s_url=https%3A%2F%2Fzb.vip.qq.com%2Fsonic%2Fbubble")
    }

    fun blueLogin(qqLoginEntity: QQLoginEntity): CommonResult<String>{
        return this.login(qqLoginEntity, "21000110", "176", "ptlogin2.gamevip.qq.com", "&daid=176&pt_login_type=4&service=pt4_auth&pttype=2&regmaster=&aid=21000110&s_url=http%3A%2F%2Fgamevip.qq.com%2F")
    }

    fun weiYunLogin(qqLoginEntity: QQLoginEntity): CommonResult<String>{
        return this.login(qqLoginEntity, "527020901", "372", "ssl.ptlogin2.weiyun.com", "&s_url=https%3A%2F%2Fh5.weiyun.com%2Fsign_in&f_url=&ptlang=2052&ptredirect=101&aid=527020901&daid=372&j_later=0&low_login_hour=720&regmaster=0&pt_login_type=1&pt_aid=0&pt_aaid=0&pt_light=0&pt_3rd_aid=0&service=login&nodirect=0")
    }
}
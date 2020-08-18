package me.kuku.yuq.utils

import me.kuku.yuq.pojo.CommonResult
import java.net.URLEncoder

/**
 * qq空间： appId 549000912 daId 5
 * qq群：  appId 715030901 daId 73
 */
object QQQrCodeLoginUtils {

    /**
     * 默认取qq空间
     */
    fun getQrCode(appId: String = "549000912", daId: String = "5"): Map<String, Any>{
        val response = OkHttpClientUtils.get("https://ssl.ptlogin2.qq.com/ptqrshow?appid=$appId&e=2&l=M&s=3&d=72&v=4&t=0.${BotUtils.randomStr(17)}&daid=$daId&pt_3rd_aid=0")
        val bytes = OkHttpClientUtils.getBytes(response)
        val sig = OkHttpClientUtils.getCookie(response, "qrsig").getValue("qrsig")
        return mapOf("qrCode" to bytes, "sig" to sig)
    }

    /**
     * 66  未失效
     * 67  验证中
     * 68  被拒绝
     * 65  已失效
     */
    fun checkQrCode(appId: String = "549000912", daId: String = "5", url: String = "https://qzs.qzone.qq.com/qzone/v5/loginsucc.html?para=izone", sig: String): CommonResult<Map<String, String>>{
        val response = OkHttpClientUtils.get("https://ssl.ptlogin2.qq.com/ptqrlogin?u1=${URLEncoder.encode(url, "utf-8")}&ptqrtoken=${this.getPtGrToken(sig)}&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=0-0-1591074900575&js_ver=20032614&js_type=1&login_sig=&pt_uistyle=40&aid=$appId&daid=$daId&",
                OkHttpClientUtils.addCookie("qrsig=$sig"))
        val str = OkHttpClientUtils.getStr(response)
        return when (BotUtils.regex("'", "','", str)?.toInt()){
            0 -> {
                val cookieMap = OkHttpClientUtils.getCookie(response, "skey", "superkey", "supertoken").toMutableMap()
                val commonResult = QQUtils.getResultUrl(str)
                val map = QQUtils.getKey(commonResult.t!!)
                cookieMap.putAll(map)
                CommonResult(200, "登录成功", cookieMap)
            }
            66,67 -> CommonResult(0, "未失效或者验证中！")
            else -> CommonResult(500, BotUtils.regex("','','0','", "', ''", str) ?: "其他错误！！")
        }
    }

    private fun getPtGrToken(sig: String): Int{
        var e = 0
        var i = 0
        val n = sig.length
        while (n > i) {
            e += (e shl 5) + sig[i].toInt()
            ++i
        }
        return e and 2147483647
    }

}
package me.kuku.yuq.utils

import me.kuku.yuq.pojo.CommonResult
import java.net.URLEncoder
import javax.script.ScriptEngineManager

object QQPasswordLoginUtils {

    private fun encryptPassword(qq: String, password: String, vCode: String): String{
        val se = ScriptEngineManager().getEngineByName("JavaScript")
        se.eval(OkHttpClientUtils.getStr(OkHttpClientUtils.get("https://u.iheit.com/kuku/login.js")))
        return se.eval("getmd5('$qq','$password','$vCode')").toString()
    }

    /**
     * qq空间  549000912  5  https%3A%2F%2Fqzs.qq.com%2Fqzone%2Fv5%2Floginsucc.html%3Fpara%3Dizone
     */
    private fun checkVc(appId: String, daId: String, url: String , qq: String): Map<String, String?>{
        val firstResponse = OkHttpClientUtils.get("https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=$daId&appid=$appId&s_url=${URLEncoder.encode(url, "utf-8")}")
        firstResponse.close()
        var cookie = OkHttpClientUtils.getCookie(firstResponse)
        val loginSig = BotUtils.regex("pt_login_sig=", "; ", cookie)
        val secondResponse = OkHttpClientUtils.get("https://ssl.ptlogin2.qq.com/check?regmaster=&pt_tea=2&pt_vcode=1&uin=$qq&appid=$appId&js_ver=20032614&js_type=1&login_sig=$loginSig&u1=${URLEncoder.encode(url, "utf-8")}&r=0.${BotUtils.randomNum(16)}&pt_uistyle=40",
                OkHttpClientUtils.addHeaders(
                        "cookie", cookie,
                        "referer", "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=$daId&appid=$appId&s_url=${URLEncoder.encode(url, "utf-8")}"
                ))
        val resultStr = OkHttpClientUtils.getStr(secondResponse)
        cookie += OkHttpClientUtils.getCookie(secondResponse)
        val arr = BotUtils.regex("\\('", "\\)", resultStr)!!.split("','")
        return if (arr[0] == "0"){
            mapOf("code" to arr[0], "randStr" to arr[1], "ticket" to arr[3], "cookie" to cookie)
        }else {
            mapOf("code" to arr[0], "sig" to arr[1], "cookie" to cookie)
        }
    }

    private fun login(appId: String, daId: String, qq: String, password: String, url: String, map1: Map<String, String?>, map2: Map<String, String?>): CommonResult<Map<String, String>>{
        val encryptPassword = this.encryptPassword(qq, password, map2["randStr"].toString())
        val ptdRvs = BotUtils.regex("(?<=ptdrvs=).+?(?=;)", map1["cookie"].toString())
        val sig = BotUtils.regex("(?<=pt_login_sig=).+?(?=;)", map1["cookie"].toString())
        val v1 = if (map2.getValue("randStr")!!.startsWith("!")) 0 else 1
        val uri = "https://ssl.ptlogin2.qq.com/login?u=$qq&verifycode=${map2["randStr"]}&pt_vcode_v1=$v1&pt_verifysession_v1=${map2["ticket"]}&p=$encryptPassword&pt_randsalt=2&u1=${URLEncoder.encode(url, "utf-8")}&ptredirect=0&h=1&t=1&g=1&from_ui=1&ptlang=2052&action=2-1-1591170931294&js_ver=20032614&js_type=1&login_sig=$sig&pt_uistyle=40&aid=$appId&daid=$daId&ptdrvs=$ptdRvs&"
        val response = OkHttpClientUtils.get(uri, OkHttpClientUtils.addHeaders(
                "cookie", map1["cookie"].toString(),
                "referer", url
        ))
        val cookieMap = OkHttpClientUtils.getCookie(response, "skey", "superkey", "supertoken").toMutableMap()
        val str = OkHttpClientUtils.getStr(response)
        val commonResult = QQUtils.getResultUrl(str)
        return if (commonResult.code == 200){
            val otherKeys = QQUtils.getKey(commonResult.t)
            cookieMap.putAll(otherKeys)
            CommonResult(200, "登录成功" , cookieMap)
        }else CommonResult(500, commonResult.msg)
    }

    fun login(appId: String = "549000912", daId: String = "5", qq: String, password: String, url: String = "https://qzs.qzone.qq.com/qzone/v5/loginsucc.html?para=izone&specifyurl=http://user.qzone.qq.com"): CommonResult<Map<String, String>>{
        val map1 = this.checkVc(appId, daId, url, qq)
        val map2 = if (map1.getValue("code") != "0")
            TenCentCaptchaUtils.identify(appId, map1["sig"].toString(), qq.toLong())
        else map1
        return this.login(appId, daId, qq, password, url, map1, map2)
    }
}
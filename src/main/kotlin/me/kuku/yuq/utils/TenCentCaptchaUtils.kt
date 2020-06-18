package me.kuku.yuq.utils

import com.alibaba.fastjson.JSON
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.roundToInt

object TenCentCaptchaUtils {

    private const val UA = "TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzgzLjAuNDEwMy42MSBTYWZhcmkvNTM3LjM2"

    private fun getCdata(ans: String, m: String, randStr: String): String{
        var a: String? = null
        var i = 0
        while (i < m.toInt() && i < 1000) {
            val c = randStr + i
            val d: String = MD5Utils.toMD5(c)
            if (ans == d) {
                a = "$i"
                break
            }
            i++
        }
        return a!!
    }

    private fun getCaptchaPictureUrl(
            appId: String, qq: String, vc: String?, sig: String, sess: String, sid: String, webSig: String?, index: Int
    ) = "https://ssl.captcha.qq.com/cap_union_new_getcapbysig?aid=$appId&captype=&protocol=https&clientype=2&disturblevel=&apptype=2&noheader=0&color=&showtype=&fb=1&theme=&lang=2052&ua=$UA&grayscale=1&subsid=3&sess=$sess&fwidth=0&sid=$sid&forcestyle=0&tcScale=1&uid=$qq&cap_cd=$sig&rnd=${BotUtils.randomNum(6)}&rand=${BotUtils.randomNum(8)}&websig=$webSig&vsig=$vc&img_index=$index"

    private fun getWidth(imageAUrl: String, imageBUrl: String): Int{
        val imageA: BufferedImage = ImageIO.read(URL(imageAUrl))
        val imageB: BufferedImage = ImageIO.read(URL(imageBUrl))
        val imgWidth = imageA.width
        val imgHeight = imageA.height
        var t = 0
        var r = 0
        for (i in 0 until imgHeight - 20) {
            for (j in 0 until imgWidth - 20) {
                val rgbA = imageA.getRGB(j, i)
                val rgbB = imageB.getRGB(j, i)
                if (abs(rgbA - rgbB) > 1800000) {
                    t++
                    r += j
                }
            }
        }
        return (r / t.toFloat()).roundToInt() - 55
    }

    private fun getCaptcha(appId: String, sig: String, qq: String): Map<String, String?> {
        val firstResponse = OkHttpClientUtils.get("https://ssl.captcha.qq.com/cap_union_prehandle?aid=$appId&captype=&curenv=inner&protocol=https&clientype=2&disturblevel=&apptype=2&noheader=&color=&showtype=embed&fb=1&theme=&lang=2052&ua=$UA&enableDarkMode=0&grayscale=1&cap_cd=$sig&uid=$qq&wxLang=&subsid=1&callback=_aq_365903&sess=")
        val jsonObject = OkHttpClientUtils.getJson(firstResponse, "\\{.*\\}")
        val secondResponse = OkHttpClientUtils.get("https://ssl.captcha.qq.com/cap_union_new_show?aid=$appId&captype=&curenv=inner&protocol=https&clientype=2&disturblevel=&apptype=2&noheader=&color=&showtype=embed&fb=1&theme=&lang=2052&ua=$UA&enableDarkMode=0&grayscale=1&subsid=2&sess=${jsonObject.getString("sess")}&fwidth=0&sid=${jsonObject.getString("sid")}&forcestyle=undefined&wxLang=&tcScale=1&noBorder=noborder&uid=$qq&cap_cd=$sig&rnd=${BotUtils.randomNum(6)}&TCapIframeLoadTime=14&prehandleLoadTime=74&createIframeStart=${Date().time}")
        val html = OkHttpClientUtils.getStr(secondResponse)
        val vc = BotUtils.regex("(?<=vsig:\\\")([0-9a-zA-Z\\*\\_\\-]{187})(?=\\\")", html)
        val height = BotUtils.regex("(?<=spt:\\\")(\\d+)(?=\\\")", html)
        val webSig = BotUtils.regex("(?<=websig:\\\")([0-9a-f]{128})(?=\\\")", html)
        val collectName = BotUtils.regex("(?<=collectdata:\\\")([a-z]{6})(?=\\\")", html)
        var cdata = BotUtils.regex("\\{&quot;.*&quot;\\}", html)?.replace("&quot;", "\"")
        val cDataJsonObject = JSON.parseObject(cdata)
        cdata = this.getCdata(cDataJsonObject.getString("ans"), cDataJsonObject.getString("M"), cDataJsonObject.getString("randstr"))
        val imageAUrl = this.getCaptchaPictureUrl(appId, qq, vc, sig, jsonObject.getString("sess"), jsonObject.getString("sid"), webSig, 1)
        val imageBUrl = this.getCaptchaPictureUrl(appId, qq, vc, sig, jsonObject.getString("sess"), jsonObject.getString("sid"), webSig, 0)
        val width = this.getWidth(imageAUrl, imageBUrl)
        val ans = "$width,$height;"
        return mapOf("sess" to jsonObject.getString("sess"), "sid" to jsonObject.getString("sid"),
                "qq" to qq, "sig" to sig, "ans" to ans, "vc" to vc, "collectName" to collectName,
                "webSig" to webSig, "cdata" to cdata, "width" to width.toString())
    }

    private fun identifyCaptcha(appId: String, map: Map<String, String?>): Map<String, String>{
        val firstResponse = OkHttpClientUtils.get("https://ssl.captcha.qq.com/dfpReg?0=Mozilla%2F5.0%20(Linux%3B%20Android%206.0%3B%20Nexus%205%20Build%2FMRA58N)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Chrome%2F81.0.4044.138%20Mobile%20Safari%2F537.36&1=zh-CN&2=1.6&3=1.6&4=24&5=8&6=-480&7=1&8=1&9=1&10=u&11=function&12=u&13=Win32&14=0&15=e466827d3971a555235e032f6e6f19d2&16=b845fd62efae6732b958d2b9c29e7145&17=a1f937b6ee969f22e6122bdb5cb48bde&18=0&19=9be20eda6faa41e1ca7c1de0183c0ef6&20=6344002463440024&21=2.0000000298023224%3B&22=1%3B1%3B1%3B1%3B1%3B1%3B1%3B0%3B1%3Bobject0UTF-8&23=0&24=1%3B1&25=31fc8c5fca18c5c1d5acbe2d336b9a63&26=48000_2_1_0_2_explicit_speakers&27=c8205b36aba2b1f3b581d8170984e918&28=ANGLE(AMDRadeon(TM)RXVega10GraphicsDirect3D11vs_5_0ps_5_0)&29=aa7dd7fcb223cfdbbec69dfc7d0d4109&30=a23a38527a38dcea2f63d5d078443f78&31=0&32=0&33=0&34=0&35=0&36=0&37=0&38=0&39=0&40=0&41=0&42=0&43=0&44=0&45=0&46=0&47=0&48=0&49=0&50=0&fesig=17289738368388340228&ut=1032&appid=0&refer=https%3A%2F%2Fssl.captcha.qq.com%2Fcap_union_new_show&domain=ssl.captcha.qq.com&fph=110050432DAF39D9F2557B9CEF152A9335E0A0C7BF1505220D8B743087D4ED2284EB594A94E23560CB9EC5341D741708DC24&fpv=0.0.15&ptcz=124cfe4aebb3fadb13b49e962bf49c91c49df4818afc73a91796c345d21c3eb2&callback=_fp_063731")
        val fpSig = OkHttpClientUtils.getJson(firstResponse, "\\{.*\\}").getString("fpsig")
        val secondResponse = OkHttpClientUtils.post("https://ssl.captcha.qq.com/cap_union_new_verify", OkHttpClientUtils.addForms(
                "aid", appId,
                "captype", "",
                "protocol", "https",
                "clientype", "1",
                "disturblevel", "",
                "noheader", "0",
                "color", "",
                "fb", "1",
                "theme", "",
                "lang", "2052",
                "ua", UA,
                "enableDarkMode", "0",
                "grayscale", "1",
                "sess", map["sess"].toString(),
                "fwidth", "0",
                "sid", map["sid"].toString(),
                "forcestyle", "0",
                "wxLang", "",
                "tcScale", "1",
                "uid", map["qq"].toString(),
                "cap_cd", map["sig"].toString(),
                "rnd", BotUtils.randomNum(6),
                "TCapIframeLoadTime", "14",
                "prehandleLoadTime", "97",
                "createIframeStart", Date().time.toString(),
                "ans", map["ans"] ?: "",
                "vsig", map["vc"] ?: "",
                "cdata", map["cdata"].toString(),
                "websig", map["webSig"].toString(),
                "subcapclass", "13",
                map["collectName"].toString(), "",
                "fpinfo", "fpsig=$fpSig",
                "eks", "",
                "tlg", "",
                "vlg", "0_1_1"
        ))
        val jsonObject = OkHttpClientUtils.getJson(secondResponse)
        return mapOf("ticket" to jsonObject.getString("ticket"), "randStr" to jsonObject.getString("randstr"))
    }

    /**
     * qq空间pc版 ： 549000912
     * qq空间手机版： 549000929
     */
    fun identify(appId: String, sig: String = "", qq: Long = 0L): Map<String, String>{
        val map = this.getCaptcha(appId, sig, qq.toString())
        return this.identifyCaptcha(appId, map)
    }

}
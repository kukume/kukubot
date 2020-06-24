package me.kuku.yuq.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import okhttp3.FormBody
import java.awt.image.BufferedImage
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

object TenCentCaptchaUtils {

    private const val UA = "TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzgzLjAuNDEwMy4xMTYgU2FmYXJpLzUzNy4zNg=="

    private fun getCaptchaPictureUrl(
            appId: String, qq: String, imageId: String?, sig: String, sess: String?, sid: String, index: Int
    ) = "https://t.captcha.qq.com/hycdn?index=$index&image=$imageId?aid=$appId&captype=&curenv=inner&protocol=https&clientype=1&disturblevel=&apptype=2&noheader=0&color=&showtype=&fb=1&theme=&lang=2052&ua=$UA&enableDarkMode=0&grayscale=1&subsid=3&sess=$sess&fwidth=0&sid=$sid&forcestyle=undefined&wxLang=&tcScale=1&uid=$qq&cap_cd=$sig&rnd=${Random.nextInt(100000, 999999)}&TCapIframeLoadTime=60&prehandleLoadTime=135&createIframeStart=${Date().time}487&rand=${Random.nextInt(100000, 999999)}&websig=&vsig=&img_index=$index"

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

    private fun getCaptcha(appId: String, sig: String, qq: String): Map<String, String> {
        val firstResponse = OkHttpClientUtils.get("https://t.captcha.qq.com/cap_union_prehandle?aid=$appId&captype=&curenv=inner&protocol=https&clientype=2&disturblevel=&apptype=2&noheader=&color=&showtype=embed&fb=1&theme=&lang=2052&ua=$UA&enableDarkMode=0&grayscale=1&cap_cd=$sig&uid=$qq&wxLang=&subsid=1&callback=_aq_103418&sess=")
        val jsonObject = OkHttpClientUtils.getJson(firstResponse, "\\{.*\\}")
        val secondResponse = OkHttpClientUtils.get("https://t.captcha.qq.com/cap_union_new_show?aid=$appId&captype=&curenv=inner&protocol=https&clientype=2&disturblevel=&apptype=2&noheader=&color=&showtype=embed&fb=1&theme=&lang=2052&ua=$UA&enableDarkMode=0&grayscale=1&subsid=2&sess=${jsonObject.getString("sess")}&fwidth=0&sid=${jsonObject.getString("sid")}&forcestyle=undefined&wxLang=&tcScale=1&noBorder=noborder&uid=$qq&cap_cd=$sig&rnd=${BotUtils.randomNum(6)}&TCapIframeLoadTime=14&prehandleLoadTime=74&createIframeStart=${Date().time}")
        val html = OkHttpClientUtils.getStr(secondResponse)
        val height = BotUtils.regex("(?<=spt:\\\")(\\d+)(?=\\\")", html)
        val collectName = BotUtils.regex("(?<=collectdata:\\\")([0-9a-zA-Z]+)(?=\\\")", html)
        val sess = BotUtils.regex("sess:\"", "\"", html)
        val imageId = BotUtils.regex("&image=", "\"", html)
        val imageAUrl = this.getCaptchaPictureUrl(appId, qq, imageId, sig, sess, jsonObject.getString("sid"), 1)
        val imageBUrl = this.getCaptchaPictureUrl(appId, qq, imageId, sig, sess, jsonObject.getString("sid"), 0)
        val width = this.getWidth(imageAUrl, imageBUrl)
        val ans = "$width,$height;"
        return mapOf("sess" to sess!!, "sid" to jsonObject.getString("sid"),
                "qq" to qq, "sig" to sig, "ans" to ans, "collectName" to collectName!!,
                "cdata" to "0", "width" to width.toString())
    }

    private fun identifyCaptcha(appId: String, map: Map<String, String?>): Map<String, String>{
        val firstResponse = OkHttpClientUtils.get("https://ssl.captcha.qq.com/dfpReg?0=Mozilla%2F5.0%20(Windows%20NT%2010.0%3B%20Win64%3B%20x64)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Chrome%2F83.0.4103.116%20Safari%2F537.36&1=zh-CN&2=1.8&3=1.9&4=24&5=8&6=-480&7=1&8=1&9=1&10=u&11=function&12=u&13=Win32&14=0&15=9dcc2da81f0e59e03185ad3db82acb72&16=b845fd62efae6732b958d2b9c29e7145&17=a1835c959081afa32e01bd14786db9b3&18=0&19=76cd47f4e5fcb3f96d6c57addb5498fd&20=824153624864153624&21=1.25%3B&22=1%3B1%3B1%3B1%3B1%3B1%3B1%3B0%3B1%3Bobject0UTF-8&23=0&24=0%3B0&25=31fc8c5fca18c5c1d5acbe2d336b9a63&26=48000_2_1_0_2_explicit_speakers&27=c8205b36aba2b1f3b581d8170984e918&28=ANGLE(AMDRadeon(TM)RXVega10GraphicsDirect3D11vs_5_0ps_5_0)&29=3331cb2359a3c1aded346ac2e279d401&30=a23a38527a38dcea2f63d5d078443f78&31=0&32=0&33=0&34=0&35=0&36=0&37=0&38=0&39=0&40=0&41=0&42=0&43=0&44=0&45=0&46=0&47=0&48=0&49=0&50=0&fesig=15341077811401570658&ut=1066&appid=0&refer=https%3A%2F%2Ft.captcha.qq.com%2Fcap_union_new_show&domain=t.captcha.qq.com&fph=1100805BC31DAAEC6C4B4A686CC8F312CD98DA4D6F7ADB1CE8619BDEB0EA580DE88363A57C65F3A76B2D6D905F49E70266EB&fpv=0.0.15&ptcz=0d372124ed637aa9210ef7ebe9af2ee8e09a1e8919d4b5349e6cf34f21ed2d31&callback=_fp_091990")
        val fpSig = OkHttpClientUtils.getJson(firstResponse, "\\{.*\\}").getString("fpsig")
        val secondResponse = OkHttpClientUtils.post("https://t.captcha.qq.com/cap_union_new_verify", OkHttpClientUtils.addForms(
                "aid", appId,
                "captype", "",
                "curenv", "inner",
                "protocol", "https",
                "clientype", "2",
                "disturblevel", "",
                "apptype", "2",
                "noheader", "",
                "color", "",
                "showtype", "embed",
                "fb", "1",
                "theme", "",
                "lang", "2052",
                "ua", UA,
                "enableDarkMode", "0",
                "grayscale", "1",
                "subsid", "2",
                "sess", map["sess"].toString(),
                "fwidth", "0",
                "sid", map["sid"].toString(),
                "forcestyle", "undefined",
                "wxLang", "",
                "tcScale", "1",
                "noBorder", "noborder",
                "uid", map["qq"].toString(),
                "cap_cd", map["sig"].toString(),
                "rnd", Random.nextInt(100000, 999999).toString(),
                "TCapIframeLoadTime", "19",
                "prehandleLoadTime", "93",
                "createIframeStart", Date().time.toString(),
                "cdata", "0",
                "ans", map["ans"] ?: "",
                "vsig", "",
                "websig", "",
                "subcapclass", "",
                map["collectName"].toString(), "uRrIF0FEZOCezVjDjLhfhqvHWSdyLnIq WxEQDM6hgwpsmkQK3rYwgyUAntA 3QjvOdOkNd8CosRtnj iqxvXlyGrv2SOpYxG65Sx6LZXW8XCLV2DU4uF2kWiOWv3OTl8GqZmcoeUgeJIRf8j3rwBlhD2FV2uLXDa79hgibJHpzZAoID3c61vWixWFow0q/9RpnlmL8qEv2sAV3DkERSzH2UTXR9wnMsKBSxrxlgryyigPp2LVQ3Kok6cP9nz3IEpBaxueYPG6828zvpN2Tg1myKSOosOsx/xh0XvGdZ8eAv4wplbPVDrV2sgWGIB1h8KyHLUDc 1uZj6nc3vR5Hgt5qLtwmVKZKC9E6LjyQzA6jaTLyNcUbqsoQ0luuOSDxeGep3iCjf gCBmy7hKDdvLnfx6yS0TgQJRSmKZ/yQVGt4O014jihCE4igBgmBjalgaJWq76qe4/XRKWamxaMke24S6lNxmSu8AhPnkQ4N8btWG/Mudn0Ms2Oim/XnTXydgP56sMJRBAQ6Kp5QiaGcDEhqwyhgoRQbPnSwBEt5zGFVwdoa5d4XgRfVyGhkcxuWl5QTTMNIyIrkew0APXevhcMH HC/6Serk4KfZ9XhYAR0aAWbXneyYUgeKEA1 WXje6o/aSL5rE6pshXsFYekDjln4Pz 3jVipDoZ6HvP87/jO2ev8DacP M7Z6/wNpwEaLGP1lyV3Z2qkyFrHJJR7aH/5XgqmFUqtl86h8F/7erlyPTgRGTI6V4ogx7CtxCAT0AN2D4/iBLmqiRiqzfm/UQ9648FDWp4qgDJKl/90sFvVLC EgsrDgvw1NiRp5liq7KoFGnhiRXCZdZbuT8iAW9UsL4SCysX4CWC2ZBq46I5V62GiHgrV AlgtmQauOOC/DU2JGnmWKrsqgUaeGJMuodO98T9BLBb1SwvhILKwhX5ALTZFOw5rzPEj mRSMxOEZEGUwiukbIs7nIiN2NJeJaCmlxlrYOC/DU2JGnmVfgJYLZkGrjjgvw1NiRp5lOC/DU2JGnmWa8zxI/pkUjDgvw1NiRp5ly6h073xP0EshX5ALTZFOwzgvw1NiRp5lmvM8SP6ZFIxfgJYLZkGrjuR/atoedX9qiOVethoh4K31EPeuPBQ1qTgvw1NiRp5lIV QC02RTsPkf2raHnV/akI1jAHFtdCOiq7KoFGnhiRk3aJKzd67PuXI 7kidWNEy6h073xP0EsaDnIcJdDrE1ELuW49JJ4oiHfvoN91zmOBDmDkN/0jHKYb y5OdQ2UPk jrW83wVoomqS5qOw3MlqNZ 3kqn iN0gpoacHq5kaDnIcJdDrE1ELuW49JJ4oiHfvoN91zmOBDmDkN/0jHBwOah62e2hEjo R7M34Zbg8aCoARWb7ehj60OEWLiMBC22heIjHKQKFQxAzDN jsDizSxpCXPJ/iHfvoN91zmOBDmDkN/0jHKYb y5OdQ2Ujo R7M34ZbgomqS5qOw3Mhj60OEWLiMBmvM8SP6ZFIxYBHqDYKYrAegJxW1z9qadxnoU4Cpl0 qBDmDkN/0jHBPcYAkmmo5rqU9xObqZJZJwosnzddE mSiapLmo7DcyAkRGfdl7pIZ 20osWJXux1gEeoNgpisBOLNLGkJc8n Id  g33XOY8SLHKJ4JTaGsvNtW4YUJ1EcDmoetntoRI6PkezN GW4aSnPdGqnMhuo7DYH8O6ul4quyqBRp4YkWx sjnmiwco4s0saQlzyf9SIMLZ olgrtF5lM4VjoHmBDmDkN/0jHKYb y5OdQ2UjD9bGjbYSGSOj5HszfhluCiapLmo7DcyIqP6mgA7vIOISgC6zprgbVcJl1lu5PyI23zcvrTL3XB6ZOE18wNmd3f8W4v TUjYJtbFikktgAsvz3Em9Z uoLZwPzD/LdMCgQ5g5Df9Ixx8mHdKDVCwdhwOah62e2hEqpsQVeeKZuiOj5HszfhluGkpz3RqpzIb1OoqisChwsqISgC6zprgbcuodO98T9BLvSzutcsL6mNYBHqDYKYrAbZqVXaNf tFVzwnla2Kln7EixyieCU2hvpWZ7U0bKIvgIg6869XOv9iVy8b9BR8nZyK8zaQsUB2iq7KoFGnhiSine81GfkzcFELuW49JJ4oX1F3JbYv6d20XmUzhWOgeYEOYOQ3/SMcphv7Lk51DZRiVbdPXgahZ0rl7cJ8 Gte4V8Didf1Qhaa8zxI/pkUjEU T45/xaH7tmpVdo1/60V6hr9ENG2VQcSLHKJ4JTaGCI1b0cMRfYaOj5HszfhluGJXLxv0FHydOmDaJd1f6TNjwBbk8T1TOFgEeoNgpisBdzBeCPtnKJpQZaQi2n89Sd2COJfXF7lLgQ5g5Df9Ixyvxy9ZBFYlioCIOvOvVzr/1xbSQHnA1gZiVy8b9BR8nVqNZ 3kqn iJW1oGgU7tolfgJYLZkGrjgW9UsL4SCysyipjmsFDj6DcMH3oOMRBbVELuW49JJ4oVzwnla2Kln7dgjiX1xe5S/MeNhipYwl CiQs6jmCh qOj5HszfhluGJXLxv0FHyd90pVWFa426FCNYwBxbXQjmnnyTy3t fgyU1OBiyjMfuId  g33XOY8SLHKJ4JTaGoav/B6xQVGSpT3E5upklko6PkezN GW4rvFx6QZKKdxiVy8b9BR8najsNgfw7q6XOC/DU2JGnmWine81GfkzcLwkrpj6N7sFtmpVdo1/60WA7btzmVh3xwDLchhRfJAMgQ5g5Df9IxwKJCzqOYKH6ow/Wxo22EhkqU9xObqZJZKOj5HszfhluErl7cJ8 Gte8dK6miJxrcH3SlVYVrjboTgvw1NiRp5lzDSTJlA0sOa/HIaCQQgDDFhtzhtnkxv5bFqw UrIfVdfUXclti/p3bOLK2AWCKAO22LMU54oioAKJCzqOYKH6u BXOFuCu1OjD9bGjbYSGSMP1saNthIZI6PkezN GW4SuXtwnz4a17qsNT32eDc9vdKVVhWuNuhQjWMAcW10I4FvVLC EgsrGgfnDABB4UAVgtBZ1uya6dmP3ngNgvyv/ M7Z6/wNpw8mWiDxF0RZwTENDtLXSJ9TGDxCCeeqqqBLOS7n 8JsRpTPxaBnODyCVJj/8qDRsMjAmdbEsNLBSwhIJeNvgUU1bBRIsbAMDb5cDQ JW3jl37gvkDvhmUpSe5 qS t6C0LCFOLVrZERdwW46Cu8JVRvmvJHmFslZ7 XKmuGuTPMcH8oDN0B437P M7Z6/wNpwTRouf0kLTIWZ6HpLG0kXVy3HBbCtR2DVU6CibUR6kwMX417kVxYpiXFu9Orj4CaXb6cUgzFKmwwp7MRXATBWxVlKWQ6H Hdt9JKKldpg05D/jO2ev8DacP M7Z6/wNpw00nBNJrnpgBIKsRLisjxXV97OaJn7/L6gU0Ur xWf817pG7 DVz7MQlod1LcvJ4VK/QhbEYbqCapiJbEhPHp8rQ7Mb/bzbhrNOW61Uv0aGHE9V4VVNA6SifMyFmCRpmzPFo9FV7JMTY3F/YZAFwRhscbh5X9llbj4vE/AlkPQI8xBkTpgXzWrDEGROmBfNasCHf4utnyrqXWNP8OJynVkQh3 LrZ8q6l1jT/Dicp1ZGFHDDlHUlBx LxPwJZD0CPFgrM0u576QbWNP8OJynVkRYKzNLue kG1jT/Dicp1ZGyF5pOzCP8KjEGROmBfNashRww5R1JQceyF5pOzCP8KhYKzNLue kG1jT/Dicp1ZEWCszS7nvpBo9RWl0eTH81MQZE6YF81qyFHDDlHUlBxwh3 LrZ8q6lFgrM0u576QayF5pOzCP8Kgh3 LrZ8q6lCHf4utnyrqXi8T8CWQ9AjwPysJkIUr8aFgrM0u576QbWNP8OJynVkRYKzNLue kGFgrM0u576QbWNP8OJynVkTEGROmBfNasFgrM0u576QbWNP8OJynVkdXtlSUxdWNdFgrM0u576QYWCszS7nvpBoUcMOUdSUHHFgrM0u576QYWCszS7nvpBgZV1pmO DJrs4yqeHRM08oWCszS7nvpBhYKzNLue kGsheaTswj/CoxBkTpgXzWrDEGROmBfNassheaTswj/CoId/i62fKupdY0/w4nKdWR1jT/Dicp1ZEMMeltKaY2mBSsHnk 5YKEsheaTswj/CqFHDDlHUlBx9Y0/w4nKdWRhRww5R1JQcf/x/FICzdfnpB9KPpvCz1Rs4yqeHRM08rWNP8OJynVkRYKzNLue kG1jT/Dicp1ZHWNP8OJynVkY9RWl0eTH81Pzwak/gGedAId/i62fKupRYKzNLue kG1jT/Dicp1ZHWNP8OJynVkY9RWl0eTH81MQZE6YF81qwWCszS7nvpBhYKzNLue kGFgrM0u576QYWCszS7nvpBtY0/w4nKdWRFgrM0u576QbJmXdOWP0KYBYKzNLue kG4vE/AlkPQI8xBkTpgXzWrOLxPwJZD0CPCHf4utnyrqWRzIWkmCPgBuLxPwJZD0CPz17AhkOf8dizTohyjdQMSxw7gsoTmDBU1BooIJFhtb/UGiggkWG1v2wWtrPOCMVfvwqz4vlTVpsTXjfKrwwiPBNeN8qvDCI83DB96DjEQW1JLCwX3C2gQ972wleneon3kno 8/UslzYTXjfKrwwiPNQaKCCRYbW/RrlCmHdEdBoZRS1LcM9oprUPfeQj/kC0q/nMr1vqnk49PZ uBkw6I0INT6GzLAcmD/pynj9gU5D0Fcn1ASdGj/tjdCbnjGwcXIQ0g3PAtToLbMktWdY0/dEFyAaWWo7I9BXJ9QEnRo8ndHHQ gMQ vQVyfUBJ0aP9BXJ9QEnRo/bYsxTniiKgA/6cp4/YFOQ9ZjOey42zQAndHHQ gMQ vtjdCbnjGwc 2N0JueMbBwndHHQ gMQ mEgbGPPckydnRQOJUT0R6XddjCZigTtt7SSCo5VO1ESBtTU6R4rRSTe9sJXp3qJ96tSS0VMLiFzZhYN5dX/5qcChvZmYQmn Uxoy8yPUHCNtc8vCtjj6qw=",
                "fpinfo", "fpsig=$fpSig",
                "eks", "C UMpn82w3lWu7tuZnFNDlczKiwTMsoKGjqv1TwJn 7sZO4fCvAvG2MyJ4o7L/mUxhRBvSWw 3MmlMUxpuvtqyHaD1GeRTT30kAgLR4XHE8Nzuj3fM/DNVHQaJyS4OtsZX723q1Cas1QVw46HfDgJje yVmh7mm0mTnBErZzjmHO2NkZeMPmYZB/n3Dh09Tr vV0eOr9RVYpgM7ZCmqVN21jrdMyOoFdktnPR/Bg5 Ar0dU/Nz9BGLNpiQPbBYY7LtNfZY3hcwHWZik9N5yE5Q==",
                "tlg", "5036",
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
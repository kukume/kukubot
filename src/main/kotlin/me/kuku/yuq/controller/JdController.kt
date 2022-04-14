package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.utils.OkHttpKtUtils
import me.kuku.utils.OkUtils
import me.kuku.utils.base64Decode

@GroupController
@PrivateController
class JdController {

    @Action("京东登录")
    suspend fun jdLogin(qq: Long, context: BotActionContext, session: ContextSession) {
        val qrcodeJsonObject = OkHttpKtUtils.getJson("https://api.kukuqaq.com/jd/qrcode")
        val email = "$qq@qq.com"
        val qrcodeDataJsonObject = qrcodeJsonObject.getJSONObject("data")
        val qqLoginQrcode = qrcodeDataJsonObject.getJSONObject("qqLoginQrcode")
        val imageBase = qqLoginQrcode.getString("imageBase")
        context.source.sendMessage(mif.at(qq).plus(mif.imageByByteArray(imageBase.base64Decode())).plus("请使用qq扫码登录京东"))
        out@while (true) {
            val json = """
                {"sig":"${qqLoginQrcode.getString("sig")}","redirectUrl":"${qrcodeDataJsonObject.getString("redirectUrl")}","state":"${qrcodeDataJsonObject.getString("state")}","tempCookie":"${qrcodeDataJsonObject.getString("tempCookie")}","id":"0","email":"$email"}
            """.trimIndent()
            val jsonObject = OkHttpKtUtils.postJson("https://api.kukuqaq.com/jd/cookie", OkUtils.json(json))
            when (jsonObject.getInteger("code")) {
                200 -> {
                    context.source.sendMessage(mif.at(qq).plus("添加成功"))
                    break
                }
                506 -> {
                    context.source.sendMessage(mif.at(qq).plus("未配置配置文件信息！无法上传至青龙！您的cookie为${jsonObject.getJSONObject("data").getString("cookie")}"))
                    break
                }
                500 -> {
                    context.source.sendMessage(mif.at(qq).plus(jsonObject.getString("message")))
                    break
                }
                555 -> {
                    context.source.sendMessage(mif.at(qq).plus("异常原因：${jsonObject.getString("message")}，异常处理链接：${jsonObject.getJSONObject("data").getString("url")}"))
                    break
                }
                512 -> {
                    val dataJsonObject = jsonObject.getJSONObject("data")
                    for (i in 0..3) {
                        context.source.sendMessage(mif.at(qq).plus("请发送手机号${dataJsonObject.getString("phone")}的验证码"))
                        val code = session.waitNextMessage(1000 * 60 * 3).firstString()
                        dataJsonObject["code"] = code
                        val smsJsonObject =
                            OkHttpKtUtils.postJson("https://api.kukuqaq.com/jd/qqSms", OkUtils.json(dataJsonObject))
                        when (smsJsonObject.getInteger("code")) {
                            200 -> {
                                context.source.sendMessage(mif.at(qq).plus("添加成功"))
                                break@out
                            }
                            506 -> {
                                context.source.sendMessage(mif.at(qq).plus("未配置配置文件信息！无法上传至青龙！您的cookie为${smsJsonObject.getJSONObject("data").getString("cookie")}"))
                            }
                            else -> {
                                context.source.sendMessage(mif.at(qq).plus("${smsJsonObject.getString("message")}，如为验证码错误，请重新发送验证码"))
                            }

                        }
                    }
                    break
                }
                else -> break
            }
        }
    }

}
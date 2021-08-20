package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.alibaba.fastjson.JSONObject
import me.kuku.pojo.QqLoginQrcode
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.utils.OkHttpUtils
import me.kuku.utils.QqQrCodeLoginUtils
import me.kuku.yuq.entity.BaiduEntity

@AutoBind
interface BaiduLogic{
    fun getQrcode(): QqLoginQrcode
    fun checkQrcode(qrcode: QqLoginQrcode): Result<BaiduEntity>
    fun ybbSign(baiduEntity: BaiduEntity): Result<Void>
}

class BaiduLogicImpl: BaiduLogic{

    private val appId = 716027609L
    private val daId = 383
    private val ptAid = 100312028L

    override fun getQrcode(): QqLoginQrcode {
        return QqQrCodeLoginUtils.getQrCode(appId, daId, ptAid)
    }

    override fun checkQrcode(qrcode: QqLoginQrcode): Result<BaiduEntity> {
        val checkRes = QqQrCodeLoginUtils.checkQrCode(appId, daId, ptAid,
            "https://graph.qq.com/oauth2.0/login_jump", qrcode.sig)
        return if (checkRes.isFailure) Result.failure(checkRes.code, checkRes.message)
        else {
            val response =
                OkHttpUtils.get("https://passport.baidu.com/phoenix/account/startlogin?type=15&tpl=mn&u=https%3A%2F%2Fwww.baidu.com%2F&display=page&act=implicit&xd=https%3A%2F%2Fwww.baidu.com%2Fcache%2Fuser%2Fhtml%2Fxd.html%23display%3Dpopup&fire_failure=1")
            response.close()
            val cookie = OkHttpUtils.getCookie(response)
            val mKey = OkHttpUtils.getCookie(cookie, "mkey")
            val qqLoginPojo = checkRes.data
            val result = QqQrCodeLoginUtils.authorize(
                qqLoginPojo,
                ptAid,
                System.currentTimeMillis().toString(),
                "https://passport.baidu.com/phoenix/account/afterauth?mkey=$mKey&tpl=mn"
            )
            if (result.isFailure) return Result.failure(result.message)
            else {
                val url = result.data
                val baiduResponse = OkHttpUtils.get(url, mapOf("referer" to "https://graph.qq.com/",
                        "cookie" to cookie, "user-agent" to UA.PC.value))
                baiduResponse.close()
                val resCookie = OkHttpUtils.getCookie(baiduResponse)
                val sToken = OkHttpUtils.getCookie(resCookie, "STOKEN")
                val bdUss = OkHttpUtils.getCookie(resCookie, "BDUSS")
                Result.success(BaiduEntity(cookie = resCookie, sToken = sToken, bdUss = bdUss))
            }
        }
    }

    override fun ybbSign(baiduEntity: BaiduEntity): Result<Void> {
        val time = System.currentTimeMillis()
        val tenTime = time / 1000
        val jsonObject = JSONObject()
        jsonObject["end_time"] = tenTime
        jsonObject["start_time"] = tenTime
        jsonObject["task"] = 1
        val map: MutableMap<String, String> = HashMap()
        map["X-Channel-Name"] = "vivo"
        map["X-Device-Name"] = "android"
        map["X-Client-Version"] = "2.0.13"
        map["X-System-Version"] = "29"
        map["X-Auth-Timestamp"] = time.toString()
        map["cookie"] = baiduEntity.cookie
        val resultJsonObject = OkHttpUtils.postJson(
            "https://api-gt.baidu.com/v1/server/task",
            OkHttpUtils.addJson(jsonObject), OkHttpUtils.addHeaders(map)
        )
        return if (resultJsonObject.getBoolean("success")) Result.success("观看广告成功！", null)
        else Result.failure(resultJsonObject.getJSONObject("errors").getString("message_cn"))
    }
}
package me.kuku.yuq.logic.impl

import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.logic.LeXinMotionLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.MD5Utils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.QQUtils
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.*

class LeXinMotionLogicImpl: LeXinMotionLogic {
    override fun getCaptchaImage(phone: String): ByteArray {
        val response = OkHttpClientUtils.get("https://sports.lifesense.com/sms_service/verify/getValidateCode?requestId=1000&sessionId=nosession&mobile=$phone")
        return OkHttpClientUtils.getBytes(response)
    }

    override fun getCaptchaCode(phone: String, captchaImageCode: String): CommonResult<String> {
        val response = OkHttpClientUtils.post("https://sports.lifesense.com/sms_service/verify/sendCodeWithOptionalValidate?requestId=1000&sessionId=nosession",
                OkHttpClientUtils.addJson("{\"code\":\"$captchaImageCode\",\"mobile\":\"$phone\"}"))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when(jsonObject.getInteger("code")){
            200 -> CommonResult(200, "验证码发送成功，请输入验证码")
            416 -> CommonResult(416, "验证码已失效！！")
            412 -> CommonResult(412, "验证码错误！！")
            else -> CommonResult(500, jsonObject.getString("msg"))
        }
    }

    override fun loginByPassword(phone: String, password: String): CommonResult<MotionEntity> {
        val response = OkHttpClientUtils.post("https://sports.lifesense.com/sessions_service/login?screenHeight=2267&screenWidth=1080&systemType=2&version=4.5",
                OkHttpClientUtils.addJson("{\"password\":\"$password\",\"clientId\":\"${BotUtils.randomStr(32)}\",\"appType\":6,\"loginName\":\"$phone\",\"roleType\":0}"))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200){
            CommonResult(200, "", MotionEntity(
                    null, 0L, phone, password, OkHttpClientUtils.getCookie(response), jsonObject.getJSONObject("data").getString("userId"),
                    jsonObject.getJSONObject("data").getString("accessToken")
            ))
        }else CommonResult(500, jsonObject.getString("msg"))
    }

    override fun loginByPhoneCaptcha(phone: String, captchaPhoneCode: String): CommonResult<MotionEntity> {
        val response: Response = OkHttpClientUtils.post("https://sports.lifesense.com/sessions_service/loginByAuth?screenHeight=2267&screenWidth=1080&systemType=2&version=4.5",
                OkHttpClientUtils.addJson("{\"clientId\": \"${BotUtils.randomStr(32)}\",\"authCode\": \"$captchaPhoneCode\",\"appType\": \"6\",\"loginName\": \"$phone\"}"))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when {
            jsonObject.getInteger("code") == 200 -> {
                CommonResult(200, "登录成功", MotionEntity(null , 0L, "", "", OkHttpClientUtils.getCookie(response),
                        jsonObject.getJSONObject("data").getString("userId"),
                        jsonObject.getJSONObject("data").getString("accessToken")))
            }
            jsonObject.getInteger("code") == 412 -> CommonResult(412, "验证码错误！！")
            else -> CommonResult(500, jsonObject.getString("msg"))
        }
    }

    override fun loginByQQ(qqEntity: QQEntity): CommonResult<MotionEntity> {
        val firstResponse = OkHttpClientUtils.get("https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=1101774620&daid=381&pt_skey_valid=1&style=35&s_url=http://connect.qq.com&refer_cgi=m_authorize&ucheck=1&fall_to_wv=1&status_os=9.3.2&redirect_uri=auth://www.qq.com&client_id=1104904286&response_type=token&scope=all&sdkp=i&sdkv=2.9&state=test&status_machine=iPhone8,1&switch=1", OkHttpClientUtils.addCookie(qqEntity.getCookieWithSuper()))
        firstResponse.close()
        val addCookie = OkHttpClientUtils.getCookie(firstResponse)
        val qqResponse = OkHttpClientUtils.get("https://ssl.ptlogin2.qq.com/pt_open_login?openlogin_data=appid%3D716027609%26pt_3rd_aid%3D1101774620%26daid%3D381%26pt_skey_valid%3D1%26style%3D35%26s_url%3Dhttp%3A%2F%2Fconnect.qq.com%26refer_cgi%3Dm_authorize%26ucheck%3D1%26fall_to_wv%3D1%26status_os%3D9.3.2%26redirect_uri%3Dauth%3A%2F%2Fwww.qq.com%26client_id%3D1104904286%26response_type%3Dtoken%26scope%3Dall%26sdkp%3Di%26sdkv%3D2.9%26state%3Dtest%26status_machine%3DiPhone8%2C1%26switch%3D1%26pt_flex%3D1&auth_token=${QQUtils.getToken2(qqEntity.superToken)}&pt_vcode_v1=0&pt_verifysession_v1=&verifycode=&u=${qqEntity.qq}&pt_randsalt=0&ptlang=2052&low_login_enable=0&u1=http%3A%2F%2Fconnect.qq.com&from_ui=1&fp=loginerroralert&device=2&aid=716027609&daid=381&pt_3rd_aid=1101774620&ptredirect=1&h=1&g=1&pt_uistyle=35&regmaster=&", OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookieWithSuper() + addCookie,
                "referer", "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=1101774620&daid=381&pt_skey_valid=1&style=35&s_url=http://connect.qq.com&refer_cgi=m_authorize&ucheck=1&fall_to_wv=1&status_os=9.3.2&redirect_uri=auth://www.qq.com&client_id=1104904286&response_type=token&scope=all&sdkp=i&sdkv=2.9&state=test&status_machine=iPhone8,1&switch=1",
                "user-agent", OkHttpClientUtils.PC_UA
        ))
        val str = OkHttpClientUtils.getStr(qqResponse)
        val commonResult = QQUtils.getResultUrl(str)
        return if (commonResult.code == 200){
            val url = commonResult.t!!
            val openId = BotUtils.regex("openid=", "&", url)
            val accessToken = BotUtils.regex("access_token=", "&", url)
            val response = OkHttpClientUtils.post("https://sports.lifesense.com/sessions_service/loginFromOpenId?systemType=2&version=3.7.5",
                    OkHttpClientUtils.addJson("{\"openAccountType\":2,\"clientId\":\"${BotUtils.randomStr(33)}\",\"expireTime\":${Date().time + 1000L * 60 * 60 * 24 * 90},\"appType\":6,\"openId\":\"$openId\",\"roleType\":0,\"openAccessToken\":\"$accessToken\"}"))
            val jsonObject = OkHttpClientUtils.getJson(response)
            val cookie = OkHttpClientUtils.getCookie(response)
            if (jsonObject.getInteger("code") == 200) {
                CommonResult(200, "登录成功", MotionEntity(null , 0L, "", cookie,
                        jsonObject.getJSONObject("data").getString("userId"),
                        jsonObject.getJSONObject("data").getString("accessToken")))
            } else CommonResult(500, "您没有使用qq绑定lexin运动，登录失败！！")
        }else CommonResult(500, "您的QQ已失效，请更新QQ！！")
    }

    override fun modifyStepCount(step: Int, motionEntity: MotionEntity): String {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        val response = OkHttpClientUtils.post("https://sports.lifesense.com/sport_service/sport/sport/uploadMobileStepV2",
                OkHttpClientUtils.addJson("{\"list\": [{\"active\": 1,\"calories\": ${step / 3415},\"created\": \"${dateTimeFormat.format(date)}\",\"dataSource\": 2,\"dayMeasurementTime\": \"${dateFormat.format(date)}\",\"deviceId\": \"M_NULL\",\"distance\": ${step / 100},\"id\": \"${motionEntity.accessToken}\",\"isUpload\": 0,\"measurementTime\": \"${dateTimeFormat.format(date)}\",\"priority\": 0,\"step\": $step,\"type\": 2,\"updated\": ${date.time},\"userId\":\"${motionEntity.userId}\",\"DataSource\": 2,\"exerciseTime\": 0}]}"),
                OkHttpClientUtils.addCookie(motionEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200) "步数修改成功！！" else jsonObject.getString("msg")
    }
}
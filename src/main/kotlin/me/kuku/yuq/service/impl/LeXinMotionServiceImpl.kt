package me.kuku.yuq.service.impl

import com.alibaba.fastjson.JSON
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.service.LeXinMotionService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.*

class LeXinMotionServiceImpl: LeXinMotionService {
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
            416 -> CommonResult(416, "已失效")
            else -> CommonResult(500, jsonObject.getString("msg"))
        }
    }

    override fun loginByPhoneCaptcha(phone: String, captchaPhoneCode: String): CommonResult<Map<String, String>> {
        val response: Response = OkHttpClientUtils.post("https://sports.lifesense.com/sessions_service/loginByAuth?screenHeight=2267&screenWidth=1080&systemType=2&version=4.5",
                OkHttpClientUtils.addJson("{\"clientId\": \"${BotUtils.randomStr(32)}\",\"authCode\": \"$captchaPhoneCode\",\"appType\": \"6\",\"loginName\": \"$phone\"}"))
        val str: String = OkHttpClientUtils.getStr(response)
        val jsonObject = JSON.parseObject(str)
        return if (jsonObject.getInteger("code") == 200) {
            CommonResult(200, "登录成功", mapOf("cookie" to OkHttpClientUtils.getCookie(response),
                    "userId" to jsonObject.getJSONObject("data").getString("userId"),
                    "accessToken" to jsonObject.getJSONObject("data").getString("accessToken")))
        } else CommonResult(500, jsonObject.getString("msg"))
    }

    override fun modifyStepCount(step: Int, motionEntity: MotionEntity): String {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        val response = OkHttpClientUtils.post("https://sports.lifesense.com/sport_service/sport/sport/uploadMobileStepV2",
                OkHttpClientUtils.addJson("{\"list\": [{\"active\": 1,\"calories\": ${step / 3415},\"created\": \"${dateTimeFormat.format(date)}\",\"dataSource\": 2,\"dayMeasurementTime\": \"${dateFormat.format(date)}\",\"deviceId\": \"M_NULL\",\"distance\": ${step / 100},\"id\": \"${motionEntity.accessToken}\",\"isUpload\": 0,\"measurementTime\": \"${dateTimeFormat.format(date)}\",\"priority\": 0,\"step\": $step,\"type\": 2,\"updated\": ${date.time},\"userId\":\"${motionEntity.userId}\",\"DataSource\": 2,\"exerciseTime\": 0}]}"),
                OkHttpClientUtils.addCookie(motionEntity.cookie))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200) "提交成功，如若没有修改成功，请稍后再试" else jsonObject.getString("msg")
    }
}
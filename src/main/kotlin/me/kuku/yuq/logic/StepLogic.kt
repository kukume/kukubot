package me.kuku.yuq.logic

import me.kuku.yuq.entity.StepEntity
import me.kuku.pojo.Result
import me.kuku.utils.*

object LeXinStepLogic {

    fun login(phone: String, password: String): Result<StepEntity> {
        val newPassword = if (password.length == 32) password else MD5Utils.toMD5(password)
        val response = OkHttpUtils.post("https://sports.lifesense.com/sessions_service/login?screenHeight=2267&screenWidth=1080&systemType=2&version=4.5",
            OkUtils.json("{\"password\":\"$newPassword\",\"clientId\":\"${MyUtils.randomNum(32)}\",\"appType\":6,\"loginName\":\"$phone\",\"roleType\":0}"))
        val jsonObject = OkUtils.json(response)
        return if (jsonObject.getInteger("code") == 200) {
            val cookie = OkUtils.cookie(response)
            Result.success(StepEntity().also {
                it.leXinCookie = cookie
                it.leXinUserid = jsonObject.getJSONObject("data").getString("userId")
                it.leXinAccessToken = jsonObject.getJSONObject("data").getString("accessToken")
            })
        } else Result.failure(jsonObject.getString("msg"))
    }

    fun modifyStepCount(stepEntity: StepEntity, step: Int): Result<Void> {
        val dateTimePattern = "yyyy-MM-dd hh:mm:ss"
        val datePattern = "yyyy-MM-dd"
        val time = System.currentTimeMillis()
        val second = System.currentTimeMillis() / 1000
        val jsonObject = OkHttpUtils.postJson("https://sports.lifesense.com/sport_service/sport/sport/uploadMobileStepV2?country=%E4%B8%AD%E5%9B%BD&city=%E6%9F%B3%E5%B7%9E&cityCode=450200&timezone=Asia%2FShanghai&latitude=24.368694&os_country=CN&channel=qq&language=zh&openudid=&platform=android&province=%E5%B9%BF%E8%A5%BF%E5%A3%AE%E6%97%8F%E8%87%AA%E6%B2%BB%E5%8C%BA&appType=6&requestId=${MyUtils.randomLetter(32)}&countryCode=&systemType=2&longitude=109.532216&devicemodel=V1914A&area=CN&screenwidth=1080&os_langs=zh&provinceCode=450000&promotion_channel=qq&rnd=3d51742c&version=4.6.7&areaCode=450203&requestToken=${MyUtils.randomLetter(32)}&network_type=wifi&osversion=10&screenheight=2267&ts=$second",
            OkUtils.json("{\"list\":[{\"active\":1,\"calories\":${step / 4},\"created\":\"${DateTimeFormatterUtils.format(time, dateTimePattern)}\",\"dataSource\":2,\"dayMeasurementTime\":\"${DateTimeFormatterUtils.format(time, datePattern)}\",\"deviceId\":\"M_NULL\",\"distance\":${step / 3},\"id\":\"${MyUtils.randomLetter(32)}\",\"isUpload\":0,\"measurementTime\":\"${DateTimeFormatterUtils.format(time, dateTimePattern)}\",\"priority\":0,\"step\":$step,\"type\":2,\"updated\":$time,\"userId\":\"${stepEntity.leXinUserid}\",\"DataSource\":2,\"exerciseTime\":0}]}"),
            OkUtils.cookie(stepEntity.leXinCookie))
        return if (jsonObject.getInteger("code") == 200)
            Result.success()
        else Result.failure(jsonObject.getString("msg"))
    }

}
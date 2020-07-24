package me.kuku.yuq.logic.impl

import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.logic.SuperCuteLogic
import me.kuku.yuq.utils.OkHttpClientUtils
import okhttp3.Headers
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.random.Random

class SuperCuteLogicImpl: SuperCuteLogic {

    private val cookieName = "Authorization"

    private fun addHeader(token: String): Headers{
        return OkHttpClientUtils.addHeader(cookieName, "Bearer $token")
    }

    override fun getInfo(token: String): CommonResult<Map<String, String>> {
        val response = OkHttpClientUtils.get("https://qqpet.jwetech.com/api/users/profile", this.addHeader(token))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (!jsonObject.containsKey("code")){
            val userId = jsonObject.getString("id")
            val sessResponse = OkHttpClientUtils.post("https://qqstore.jwetech.com/mall/login/index", OkHttpClientUtils.addForms(
                    "openudid", userId,
                    "token", token,
                    "nick", jsonObject.getString("nick"),
                    "avatar", jsonObject.getString("avatar")
            ))
            val sessJsonObject = OkHttpClientUtils.getJson(sessResponse)
            CommonResult(200, "成功",
                    mapOf("userId" to userId, "token" to token, "sessId" to sessJsonObject.getJSONObject("data").getString("sessid"))
                    )
        }else CommonResult(500, jsonObject.getString("msg"))
    }

    override fun dailySign(map: Map<String, String>): String {
        val now = LocalDate.now()
        val num = now.dayOfWeek.value - 1
        val response = OkHttpClientUtils.post("https://qqpet.jwetech.com/api/v2/daily_signs",
                OkHttpClientUtils.addJson("{\"ad\":true,\"day\":$num}"), this.addHeader(map.getValue("token")))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.size != 0) "萌宠每日签到成功" else "萌宠今日已签到"
    }

    override fun dailyVitality(map: Map<String, String>): String {
        val response = OkHttpClientUtils.post("https://qqstore.jwetech.com/mall/vitality/sign-in", OkHttpClientUtils.addForms(
                "type", "1",
                "openudid", map.getValue("userId"),
                "token", map.getValue("token"),
                "sessid", map.getValue("sessId")
        ), this.addHeader(map.getValue("token")))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("iRet")){
            1 -> "领取20元气成功！"
            -1 -> "20元气今日已领取"
            else -> "领取失败！${jsonObject.getString("sMsg")}"
        }
    }

    override fun moreVitality(map: Map<String, String>): String {
        for (i in 0 until 20){
            val response = OkHttpClientUtils.post("https://qqstore.jwetech.com/guess/home/ad-play", OkHttpClientUtils.addForms(
                    "ad_type", "0",
                    "openudid", map.getValue("userId"),
                    "token", map.getValue("token"),
                    "sessid", map.getValue("sessId")
            ), this.addHeader(map.getValue("token")))
            val jsonObject = OkHttpClientUtils.getJson(response)
            if (jsonObject.getInteger("iRet") == 1){
                TimeUnit.SECONDS.sleep(2)
                val closeResponse = OkHttpClientUtils.post("https://qqstore.jwetech.com/guess/home/ad-close", OkHttpClientUtils.addForms(
                        "ad_type", "0",
                        "openudid", map.getValue("userId"),
                        "token", map.getValue("token"),
                        "sessid", map.getValue("sessId")
                ), this.addHeader(map.getValue("token")))
                closeResponse.close()
            }else return "200元气今日已领取！！"
        }
        return "200元气领取成功！！"
    }

    private fun useProps(map: Map<String, String>, id: Int){
        val response = OkHttpClientUtils.put("https://qqpet.jwetech.com/api/cards/$id",
                OkHttpClientUtils.addJson("{\"userId\":\"${map.getValue("userId")}\"}"), this.addHeader(map.getValue("token")))
        response.close()
    }

    override fun feeding(map: Map<String, String>): String {
        val token = map.getValue("token")
        val response = OkHttpClientUtils.get("https://qqpet.jwetech.com/api/user_foods", this.addHeader(token))
        val foodsJsonArray = OkHttpClientUtils.getJson(response).getJSONArray("foods")
        val id: Int
        val foodJsonObject = foodsJsonArray.getJSONObject(1)
        id = if (foodJsonObject.getBoolean("ad") || foodJsonObject.getInteger("count") != 0) 2
        else 3
        // 洗澡
        this.useProps(map, 12)
        //喂食
        OkHttpClientUtils.post("https://qqpet.jwetech.com/api/pet_feeds",
                OkHttpClientUtils.addJson("{\"ad\":true,\"foodId\":\"$id\"}"), this.addHeader(token)).close()
        TimeUnit.SECONDS.sleep(2)
        this.useProps(map, 10)
        val secondResponse = OkHttpClientUtils.get("https://qqpet.jwetech.com/api/vigours", this.addHeader(token))
        val jsonArray = OkHttpClientUtils.getJson(secondResponse).getJSONArray("uncollectedVigours")
        return if (jsonArray.size != 0) {
            var num = 0
            jsonArray.forEach {
                val jsonObject = it as JSONObject
                val vitalityId = jsonObject.getInteger("id")
                do {
                    val thirdResponse = OkHttpClientUtils.put("https://qqpet.jwetech.com/api/vigours/$vitalityId",
                            OkHttpClientUtils.addJson("{\"ad\":true,\"userId\":\"${map.getValue("userId")}\"}"), this.addHeader(token))
                    val thirdJsonObject = OkHttpClientUtils.getJson(thirdResponse)
                    num = thirdJsonObject.getInteger("count") ?: 0
                } while (thirdJsonObject.getInteger("countdown") != null)
            }
            "您的宠物已成功喂食，并且获得了${num}元气"
        }else "您的宠物不需要喂食！"
    }

    private fun receiveCoin(map: Map<String, String>, id: String): Int{
        val response = OkHttpClientUtils.post("https://qqpet.jwetech.com/api/counters",
                OkHttpClientUtils.addJson("{\"ad\":true,\"userId\":\"$id\"}"), this.addHeader(map.getValue("token")))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return jsonObject.getInteger("collected") ?: 0
    }

    override fun receiveCoin(map: Map<String, String>) = this.receiveCoin(map, map.getValue("userId"))

    override fun finishTask(map: Map<String, String>): String {
        val response = OkHttpClientUtils.get("https://qqpet.jwetech.com/api/daily_missions", this.addHeader(map.getValue("token")))
        val jsonArray = OkHttpClientUtils.getJson(response).getJSONArray("missions")
        thread {
            jsonArray.forEach {
                val jsonObject = it as JSONObject
                if (!jsonObject.getBoolean("taked")) {
                    TimeUnit.SECONDS.sleep(4)
                    val id = jsonObject.getInteger("id")
                    OkHttpClientUtils.put("https://qqpet.jwetech.com/api/daily_missions/$id",
                            headers = this.addHeader(map.getValue("token"))).close()
                    //领取任务
                    OkHttpClientUtils.post("https://qqpet.jwetech.com/api/daily_missions",
                            OkHttpClientUtils.addJson("{\"missionId\":$id}"), this.addHeader(map.getValue("token"))).close()
                }
            }
        }
        return "萌宠任务已在后台执行中！！！"
    }

    override fun steal(map: Map<String, String>): String {
        val response = OkHttpClientUtils.get("https://qqpet.jwetech.com/api/rankings", this.addHeader(map.getValue("token")))
        val friendJsonObject = OkHttpClientUtils.getJson(response)
        val myLevel = friendJsonObject.getJSONObject("me").getJSONObject("pet").getInteger("level")
        val jsonArray = friendJsonObject.getJSONArray("friends")
        var status = true
        thread {
            for (i in jsonArray.indices) {
                val jsonObject = jsonArray.getJSONObject(i)
                if (jsonObject.getBoolean("hasCoins")) {
                    TimeUnit.SECONDS.sleep(4)
                    this.receiveCoin(map, jsonObject.getString("id"))
                }
                if (jsonObject.getJSONObject("pet").getInteger("level") > myLevel) continue
                if (!jsonObject.getBoolean("canCapture")) continue
                if (status) {
                    val arrestResponse = OkHttpClientUtils.post("https://qqpet.jwetech.com/api/captures",
                            OkHttpClientUtils.addJson("{\"userId\":\"${jsonObject.getString("id")}\",\"type\":${Random.nextInt(2)},\"ad\":true}"), this.addHeader(map.getValue("token")))
                    val arrestJsonObject = OkHttpClientUtils.getJson(arrestResponse)
                    if (arrestJsonObject.containsKey("code")) status = false
                }
            }
        }
        return "偷取金币和抓捕好友已在后台执行中！！"
    }

    override fun findCute(map: Map<String, String>): String {
        val response = OkHttpClientUtils.post("https://qqpet.jwetech.com/api/captures/free", OkHttpClientUtils.addJson("{}"),
                this.addHeader(map.getValue("token")))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getBoolean("isCapturedBy") == true) "已找回您的萌宠" else "您的萌宠在家很安全呢！"
    }

    override fun dailyLottery(map: Map<String, String>): String {
        // 1、50万元气  2、50元气  3、1000元气  4、30元气  5、25元气  6、100元气  7、5元气  8、10000元气
        var num = 0
        for (i in 0 until 20) {
            val response = OkHttpClientUtils.post("https://qqstore.jwetech.com/mall/vitality/play", OkHttpClientUtils.addForms(
                    "openudid", map.getValue("userId"),
                    "token", map.getValue("token"),
                    "sessid", map.getValue("sessId")
            ), this.addHeader(map.getValue("token")))
            val jsonObject = OkHttpClientUtils.getJson(response)
            if (jsonObject.getInteger("iRet") != 1) break
            num += when (jsonObject.getJSONObject("data").getInteger("gid")){
                1 -> 500000
                2 -> 50
                3 -> 100
                4 -> 30
                5 -> 25
                6 -> 100
                7 -> 5
                8 -> 10000
                else -> 0
            }
        }
        return "抽奖成功，已抽取到元气$num"
    }

    override fun getProfile(map: Map<String, String>): String {
        val response = OkHttpClientUtils.get("https://qqpet.jwetech.com/api/users/profile", this.addHeader(map.getValue("token")))
        val jsonObject = OkHttpClientUtils.getJson(response)
        val petJsonObject = jsonObject.getJSONObject("pet")
        val sb = StringBuilder()
        sb.appendln("昵称：${jsonObject.getString("nick")}")
                .appendln("等级：${petJsonObject.getInteger("level")}")
                .appendln("当前经验：${petJsonObject.getInteger("expirenece")}")
                .append("食物：")
        if (petJsonObject.getJSONObject("feed")["food"] != null)
            sb.appendln("${petJsonObject.getJSONObject("feed").getJSONObject("food").getString("name")}，还剩${petJsonObject.getJSONObject("feed").getString("countdown")}秒")
        else sb.appendln("没有食物")
        sb.append("称号：")
        if (jsonObject.getJSONObject("honor") != null)
            sb.appendln(jsonObject.getJSONObject("honor").getString("title"))
        else sb.appendln("没有称号")
        sb.appendln("奴隶：${jsonObject.getJSONArray("capturedPets").size}个")
                .appendln("金币：${jsonObject.getInteger("coins")}")
                .append("元气：${jsonObject.getString("vigours")}")
        return sb.toString()
    }
}
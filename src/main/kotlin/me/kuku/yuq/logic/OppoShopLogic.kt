package me.kuku.yuq.logic

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.delay
import me.kuku.pojo.CommonResult
import me.kuku.yuq.entity.OppoShopEntity
import me.kuku.pojo.UA
import me.kuku.utils.*

object OppoShopLogic {

    private fun taskCenter(oppoShopEntity: OppoShopEntity): JsonNode {
        return OkHttpUtils.getJson("https://store.oppo.com/cn/oapi/credits/web/credits/show",
            OkUtils.headers(oppoShopEntity.cookie, "https://store.oppo.com/cn/app/taskCenter/index",
                UA.OPPO))
    }

    fun sign(oppoShopEntity: OppoShopEntity): CommonResult<Void> {
        val jsonObject = taskCenter(oppoShopEntity)
        if (jsonObject.getInteger("code") != 200) return CommonResult.failure(jsonObject.getString("errorMessage"))
        val dataJsonObject = jsonObject["data"]
        return if (dataJsonObject["userReportInfoForm"].getInteger("status") == 0) {
            val jsonArray = dataJsonObject["userReportInfoForm"]["gifts"]
            val now = DateTimeFormatterUtils.formatNow("yyyy-MM-dd")
            var qd: JsonNode? = null
            for (obj in jsonArray) {
                if (obj.getString("date") == now) {
                    qd = obj
                    break
                }
            }
            if (qd != null) {
                val params = mutableMapOf("amount" to qd.getString("credits"))
                if (qd.getBoolean("today") == true) {
                    if (qd.getString("type").isNotEmpty()) {
                        params["amout"] = qd.getString("credits")
                        params["type"] = qd.getString("type")
                        params["gift"] = qd.getString("gift")
                    }
                }
                val resultJsonObject = OkHttpUtils.postJson("https://store.oppo.com/cn/oapi/credits/web/report/immediately", params,
                    OkUtils.headers(oppoShopEntity.cookie, "https://store.oppo.com/cn/app/taskCenter/index", UA.OPPO))
                if (resultJsonObject.getInteger("code") == 200) CommonResult.success(message = "签到成功")
                else CommonResult.failure("签到失败，${resultJsonObject.getString("errorMessage")}")
            } else CommonResult.failure("签到失败，请重试")
        } else CommonResult.success(message = "今日已签到！")
    }

    suspend fun viewGoods(oppoShopEntity: OppoShopEntity): CommonResult<Void> {
        val jsonObject = taskCenter(oppoShopEntity)
        val jsonArray = jsonObject["data"]["everydayList"]
        var qd: JsonNode? = null
        for (obj in jsonArray) {
            if (obj.getString("name") == "浏览商品") {
                qd = obj
                break
            }
        }
        return if (qd != null) {
            val status = qd.getInteger("completeStatus")
            if (status == 0) {
                val shopJsonObject = OkHttpUtils.getJson("https://msec.opposhop.cn/goods/v1/SeckillRound/goods/115?pageSize=12&currentPage=1",
                    OkUtils.cookie(oppoShopEntity.cookie))
                if (shopJsonObject["meta"].getInteger("code") == 200) {
                    for (o in shopJsonObject["detail"]) {
                        val skuId = o.getString("skuid")
                        OkHttpUtils.get(
                            "https://msec.opposhop.cn/goods/v1/info/sku?skuId=$skuId",
                            OkUtils.headers(oppoShopEntity.cookie, "", UA.OPPO)).close()
                        delay(3000)
                    }
                } else return CommonResult.failure("每日浏览商品失败，获取商品列表失败")
            } else if (status == 2) {
                return CommonResult.success(message = "每日浏览商品已完成")
            }
            val b = cashingCredits(oppoShopEntity, qd.getString("marking"), qd.getString("type"), qd.getString("credits"))
            if (b) CommonResult.success(message = "每日浏览商品成功")
            else CommonResult.failure("每日浏览商品失败！")
        } else CommonResult.failure("浏览商品失败，请重试！")
    }

    private fun cashingCredits(oppoShopEntity: OppoShopEntity, infoMarking: String, infoType: String, infoCredits: String): Boolean {
        val params = mapOf("marking" to infoMarking, "type" to infoType, "amount" to infoCredits)
        val response = OkHttpUtils.post("https://store.oppo.com/cn/oapi/credits/web/credits/cashingCredits", params,
            OkUtils.headers(oppoShopEntity.cookie, "https://store.oppo.com/cn/app/taskCenter/index?us=gerenzhongxin&um=hudongleyuan&uc=renwuzhongxin", UA.OPPO))
        response.close()
        return response.code == 200
    }

    fun shareGoods(oppoShopEntity: OppoShopEntity): CommonResult<Void> {
        val jsonObject = taskCenter(oppoShopEntity)
        val jsonArray = jsonObject["data"]["everydayList"]
        var qd: JsonNode? = null
        for (o in jsonArray) {
            if (o.getString("name") == "分享商品到微信") {
                qd = o
                break
            }
        }
        return if (qd != null) {
            val status = qd.getInteger("completeStatus")
            if (status == 0) {
                var readCount = qd.getInteger("readCount")
                val endCount = qd.getInteger("times")
                while (readCount < endCount) {
                    OkHttpUtils.get("https://msec.opposhop.cn/users/vi/creditsTask/pushTask?marking=daily_sharegoods",
                        OkUtils.cookie(oppoShopEntity.cookie)).close()
                    readCount++
                }
            } else if (status == 2) return CommonResult.success(message = "每日分享商品已完成")
            val b = cashingCredits(oppoShopEntity, qd.getString("marking"), qd.getString("type"), qd.getString("credits"))
            if (b) CommonResult.success(message = "每日分享商品成功")
            else CommonResult.failure<Unit>("每日分享商品失败！")
            CommonResult.success()
        }else CommonResult.failure("分享商品失败，请重试！")
    }

    fun earlyBedRegistration(oppoShopEntity: OppoShopEntity): CommonResult<Void> {
        val jsonObject = OkHttpUtils.getJson("https://store.oppo.com/cn/oapi/credits/web/clockin/applyOrClockIn",
            OkUtils.headers(oppoShopEntity.cookie, "https://store.oppo.com/cn/app/cardingActivities?us=gerenzhongxin&um=hudongleyuan&uc=zaoshuidaka",
                UA.OPPO))
        return if (jsonObject.getInteger("code") == 200) CommonResult.success(message = "报名或打卡成功")
        else CommonResult.failure("报名或打卡失败，${jsonObject.getString("errorMessage")}")
    }


}
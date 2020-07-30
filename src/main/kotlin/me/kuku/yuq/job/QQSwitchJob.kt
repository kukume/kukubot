package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.alibaba.fastjson.JSONArray
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.logic.QQZoneLogic
import me.kuku.yuq.service.QQJobService
import me.kuku.yuq.service.QQService
import javax.inject.Inject

@JobCenter
class QQSwitchJob {
    @Inject
    private lateinit var qqJobService: QQJobService
    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var qqZoneLogic: QQZoneLogic

    @Cron("30m")
    fun groupSign(){
        val list = qqJobService.findByType("groupSign")
        list.forEach {
            val qqJobEntity = it
            val jsonObject = qqJobEntity.getJsonObject()
            if (jsonObject.getBoolean("status")){
                //如果开启了群签到
                val qqEntity = qqService.findByQQ(qqJobEntity.qq) ?: return@forEach
                if (qqEntity.status){
                    //如果cookie没过期
                    val groupList = jsonObject.getJSONArray("group")
                    var num = jsonObject.getInteger("num")
                    if (num < groupList.size) {
                        do {
                            val group = groupList.getString(num++)
                            val excludeJsonArray = jsonObject.getJSONArray("exclude")
                            var result: String? = null
                            if (!excludeJsonArray.contains(group))
                                result = qqLogic.groupSign(qqEntity, group.toLong(), "火星", "签到", "{\"category_id\":9,\"page\":0,\"pic_id\":125}")
                        } while (num < groupList.size &&(result == null || "签到成功" in result || "已被禁言" in result))
                        jsonObject["num"] = num
                        qqJobEntity.data = jsonObject.toString()
                        qqJobService.save(qqJobEntity)
                    }
                }
            }
        }
    }

    @Cron("At::d::00:01")
    fun resetGroup(){
        val list = qqJobService.findByType("groupSign")
        list.forEach {
            val qqJobEntity = it
            val qq = qqJobEntity.qq
            val qqEntity = qqService.findByQQ(qq)?: return@forEach
            if (qqEntity.status) {
                val commonResult = qqZoneLogic.queryGroup(qqEntity)
                if (commonResult.code == 200) {
                    val jsonObject = qqJobEntity.getJsonObject()
                    jsonObject["num"] = 0
                    val jsonArray = JSONArray()
                    val groupList = commonResult.t
                    groupList.forEach { jsonArray.add(it.getValue("group")) }
                    jsonObject["group"] = jsonArray
                    qqJobEntity.data = jsonObject.toString()
                    qqJobService.save(qqJobEntity)
                }else{
                    qqEntity.status = false
                    qqService.save(qqEntity)
                }
            }
        }
    }

    @Cron("4h")
    fun qqSign(){
        val list = qqJobService.findByType("autoSign")
        list.forEach {
            try {
                val qqEntity = qqService.findByQQ(it.qq) ?: return@forEach
                val str = qqLogic.qqSign(qqEntity)
                if (!str.contains("更新QQ")){
                    qqLogic.anotherSign(qqEntity)
                    qqLogic.groupLottery(qqEntity, 1132123L)
                    qqLogic.vipSign(qqEntity)
                    qqLogic.phoneGameSign(qqEntity)
                    qqLogic.yellowSign(qqEntity)
                    qqLogic.qqVideoSign1(qqEntity)
                    qqLogic.qqVideoSign2(qqEntity)
                    qqLogic.bigVipSign(qqEntity)
                    qqLogic.qqMusicSign(qqEntity)
                    qqLogic.gameSign(qqEntity)
                    qqLogic.qPetSign(qqEntity)
                    qqLogic.tribeSign(qqEntity)
                    qqLogic.motionSign(qqEntity)
                    qqLogic.blueSign(qqEntity)
                    qqLogic.sVipMornSign(qqEntity)
                    qqLogic.weiYunSign(qqEntity)
                    qqLogic.weiShiSign(qqEntity)
                    qqLogic.growthLike(qqEntity)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    @Cron("1m")
    fun mz(){
        val list = qqJobService.findByType("mz")
        list.forEach {
            val qqJobEntity = it
            if (qqJobEntity.getJsonObject().getBoolean("status")){
                val qqEntity = qqService.findByQQ(qqJobEntity.qq)?: return@forEach
                if (qqEntity.status){
                    qqZoneLogic.friendTalk(qqEntity)?.forEach { map ->
                        if (map["like"] == null || map["like"] != "1") {
                            qqZoneLogic.likeTalk(qqEntity, map)
                        }
                    }
                }
            }
        }
    }

    @Cron("30s")
    fun bubble(){
        val list = qqJobService.findByType("bubble")
        list.forEach {
            if (it.getJsonObject().getBoolean("status")){
                val qqEntity = qqService.findByQQ(it.qq)?: return@forEach
                if (qqEntity.status){
                    qqLogic.diyBubble(qqEntity, it.getJsonObject().getString("text"), null)
                }
            }
        }
    }

}
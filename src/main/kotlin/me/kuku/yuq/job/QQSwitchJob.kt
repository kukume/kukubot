package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.alibaba.fastjson.JSONArray
import me.kuku.yuq.entity.QQJobEntity
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.QQService
import me.kuku.yuq.service.QQZoneService
import javax.inject.Inject

@JobCenter
class QQSwitchJob {
    @Inject
    private lateinit var daoService: DaoService
    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var qqZoneService: QQZoneService

    @Cron("30m")
    fun groupSign(){
        val list = daoService.findQQJobByType("groupSign")
        list?.forEach {
            val qqJobEntity = it as QQJobEntity
            val jsonObject = qqJobEntity.getJsonObject()
            if (jsonObject.getBoolean("status")){
                //如果开启了群签到
                val qqEntity = daoService.findQQByQQ(qqJobEntity.qq)!!
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
                                result = qqService.groupSign(qqEntity, group.toLong(), "火星", "签到", "{\"category_id\":9,\"page\":0,\"pic_id\":125}")
                        } while (num < groupList.size &&(result == null || "签到成功" in result || "已被禁言" in result))
                        jsonObject["num"] = num
                        qqJobEntity.data = jsonObject.toString()
                        daoService.saveOrUpdateQQJob(qqJobEntity)
                    }
                }
            }
        }
    }

    @Cron("At::d::00:01")
    fun resetGroup(){
        val list = daoService.findQQJobByType("groupSign")
        list?.forEach {
            val qqJobEntity = it as QQJobEntity
            val qq = qqJobEntity.qq
            val qqEntity = daoService.findQQByQQ(qq)!!
            if (qqEntity.status) {
                val commonResult = qqZoneService.queryGroup(qqEntity)
                if (commonResult.code == 200) {
                    val jsonObject = qqJobEntity.getJsonObject()
                    jsonObject["num"] = 0
                    val jsonArray = JSONArray()
                    val groupList = commonResult.t
                    groupList.forEach { jsonArray.add(it.getValue("group")) }
                    jsonObject["group"] = jsonArray
                    qqJobEntity.data = jsonObject.toString()
                    daoService.saveOrUpdateQQJob(qqJobEntity)
                }else{
                    qqEntity.status = false
                    daoService.saveOrUpdateQQ(qqEntity)
                }
            }
        }
    }

    @Cron("1m")
    fun mz(){
        val list = daoService.findQQJobByType("mz")
        list?.forEach {
            val qqJobEntity = it as QQJobEntity
            if (qqJobEntity.getJsonObject().getBoolean("status")){
                val qqEntity = daoService.findQQByQQ(qqJobEntity.qq)!!
                if (qqEntity.status){
                    qqZoneService.friendTalk(qqEntity)?.forEach { map ->
                        if (map["like"] == null || map["like"] != "1") {
                            qqZoneService.likeTalk(qqEntity, map)
                        }
                    }
                }
            }
        }
    }

}
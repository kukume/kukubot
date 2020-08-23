package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.service.BiliBiliService
import me.kuku.yuq.service.QQGroupService
import javax.inject.Inject

@JobCenter
class BiliBiliJob {
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic
    @Inject
    private lateinit var biliBiliService: BiliBiliService

    private val groupMap = mutableMapOf<Long, MutableMap<Long, Long>>()
    private val qqMap = mutableMapOf<Long, Long>()
    private val liveMap = mutableMapOf<Long, MutableMap<Long, Boolean>>()

    @Cron("1m")
    fun biliBiliGroupMonitor(){
        val groupList = qqGroupService.findAll()
        groupList.forEach { qqGroupEntity ->
            val group = qqGroupEntity.group_
            if (!groupMap.containsKey(group)) groupMap[group] = mutableMapOf()
            val map = groupMap[group]!!
            val biliBiliJsonArray = qqGroupEntity.getBiliBiliJsonArray()
            for (i in biliBiliJsonArray.indices){
                val jsonObject = biliBiliJsonArray.getJSONObject(i)
                val biliBiliId = jsonObject.getLong("id")
                val commonResult = biliBiliLogic.getDynamicById(biliBiliId.toString())
                val biliBiliList = commonResult.t ?: continue
                val firstBiliBiliPojo = if (biliBiliList.isNotEmpty()) biliBiliList[0] else continue
                if (map.containsKey(biliBiliId)){
                    val oldId = map.getValue(biliBiliId)
                    if (firstBiliBiliPojo.id.toLong() > oldId)
                        map[biliBiliId] = firstBiliBiliPojo.id.toLong()
                    biliBiliList.forEach inner@{ biliBiliPojo ->
                        if (biliBiliPojo.id.toLong() > oldId)
                            yuq.groups[group]?.sendMessage(biliBiliLogic.convertStr(biliBiliPojo).toMessage())
                        else return@inner
                    }
                }else map[biliBiliId] = firstBiliBiliPojo.id.toLong()
            }
        }
    }

    @Cron("1m")
    fun biliBiliQQMonitor(){
        val list = biliBiliService.findByMonitor(true)
        list.forEach { biliBiliEntity ->
            val qq = biliBiliEntity.qq
            val commonResult = biliBiliLogic.getFriendDynamic(biliBiliEntity)
            val biliBiliList = commonResult.t ?: return@forEach
            if (biliBiliList.isEmpty()) return@forEach
            val firstId = biliBiliList[0].id.toLong()
            if (!qqMap.containsKey(qq)) qqMap[qq] = firstId
            val oldId = qqMap[qq]!!
            if (firstId > oldId) qqMap[qq] = firstId
            biliBiliList.forEach inner@{ biliBiliPojo ->
                if (biliBiliPojo.id.toLong() > oldId){
                    val group = biliBiliEntity.group_
                    yuq.groups[group]?.get(qq)?.sendMessage(biliBiliLogic.convertStr(biliBiliPojo).toMessage())
                }else return@inner
            }
        }
    }

    @Cron("1m")
    fun liveMonitor(){
        val list = biliBiliService.findAll()
        list.forEach { biliBiliEntity ->
            val qq = biliBiliEntity.qq
            val liveJsonArray = biliBiliEntity.getLiveJsonArray()
            if (!liveMap.containsKey(qq)) liveMap[qq] = mutableMapOf()
            val map = liveMap[qq]!!
            liveJsonArray.forEach {
                val jsonObject = it as JSONObject
                val id = jsonObject.getLong("id")
                val b = biliBiliLogic.isLiveOnline(id.toString())
                if (map.containsKey(id)){
                    if (map.getValue(id) != b){
                        val msg = if (b) "直播啦！！" else "下播了！！"
                        yuq.groups[biliBiliEntity.group_]?.get(qq)?.sendMessage("${jsonObject.getString("name")}$msg".toMessage())
                        map[id] = b
                    }
                }else map[id] = b
            }
        }
    }

}
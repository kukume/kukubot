@file:Suppress("unused")

package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.pojo.BiliBiliPojo
import me.kuku.yuq.service.BiliBiliService
import me.kuku.yuq.service.GroupService
import javax.inject.Inject

@JobCenter
class BiliBiliJob {
    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic
    @Inject
    private lateinit var biliBiliService: BiliBiliService

    private val groupMap = mutableMapOf<Long, MutableMap<Long, Long>>()
    private val userMap = mutableMapOf<Long, Long>()
    private val liveMap = mutableMapOf<Long, MutableMap<Long, Boolean>>()

    @Cron("30s")
    fun biliBiliGroupMonitor(){
        val groupList = groupService.findAll()
        for (groupEntity in groupList){
            val biliBiliJsonArray = groupEntity.biliBiliJsonArray
            val group = groupEntity.group
            if (biliBiliJsonArray.isEmpty()) continue
            if (!groupMap.containsKey(group)){
                groupMap[group] = mutableMapOf()
            }
            val biMap = groupMap[group]!!
            for (any in biliBiliJsonArray){
                val jsonObject = any as JSONObject
                val userId = jsonObject.getLong("id")
                val commonResult = biliBiliLogic.getDynamicById(userId.toString())
                val list = commonResult.t ?: continue
                if (biMap.containsKey(userId)) {
                    val newList = mutableListOf<BiliBiliPojo>()
                    for (biliBiliPojo in list){
                        if (biliBiliPojo.id.toLong() <= biMap.getValue(userId)) break
                        newList.add(biliBiliPojo)
                    }
                    newList.forEach {
                        yuq.groups[group]?.sendMessage(mif.text("哔哩哔哩有新动态了\n")
                                .plus(biliBiliLogic.convertStr(it)))
                    }
                }
                biMap[userId] = list[0].id.toLong()
            }
        }
    }

    @Cron("30s")
    fun biliBiliQQMonitor(){
        val biliBiliList = biliBiliService.findByMonitor(true)
        for (biliBiliEntity in biliBiliList){
            val qq = biliBiliEntity.qq
            val commonResult = biliBiliLogic.getFriendDynamic(biliBiliEntity)
            val list = commonResult.t ?: continue
            val newList = mutableListOf<BiliBiliPojo>()
            if (userMap.containsKey(qq)){
                val oldId = userMap[qq]!!
                for (biliBiliPojo in list) {
                    if (biliBiliPojo.id.toLong() <= oldId) break
                    newList.add(biliBiliPojo)
                }
                for (biliBiliPojo in newList){
                    val userId = biliBiliPojo.userId
                    val likeList = match(biliBiliEntity.likeJsonArray, userId)
                    if (likeList.isNotEmpty())  biliBiliLogic.like(biliBiliEntity, biliBiliPojo.id, true)
                    val commentList = match(biliBiliEntity.commentJsonArray, userId)
                    for (jsonObject in commentList)  biliBiliLogic.comment(biliBiliEntity, biliBiliPojo.rid, biliBiliPojo.type.toString(), jsonObject.getString("content"))
                    val forwardList = match(biliBiliEntity.forwardJsonArray, userId)
                    for (jsonObject in forwardList) biliBiliLogic.forward(biliBiliEntity, biliBiliPojo.id, jsonObject.getString("content"))
                    val bvId = biliBiliPojo.bvId
                    if (bvId != null) {
                        val tossCoinList = match(biliBiliEntity.tossCoinJsonArray, userId)
                        if (tossCoinList.isNotEmpty()) biliBiliLogic.tossCoin(biliBiliEntity, biliBiliPojo.rid, 2)
                        val favoritesList = match(biliBiliEntity.favoritesJsonArray, userId)
                        for (jsonObject in favoritesList) biliBiliLogic.favorites(biliBiliEntity, biliBiliPojo.rid, jsonObject.getString("content"))
                    }
                    yuq.groups[biliBiliEntity.group_]?.members?.get(qq)?.sendMessage(mif.text("哔哩哔哩有新动态了！！\n")
                            .plus(biliBiliLogic.convertStr(biliBiliPojo)))
                }
            }
            userMap[qq] = list[0].id.toLong()
        }
    }

    @Cron("30s")
    fun liveMonitor(){
        val list = biliBiliService.findAll()
        list.forEach { biliBiliEntity ->
            val qq = biliBiliEntity.qq
            val liveJsonArray = biliBiliEntity.liveJsonArray
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

    private fun match(jsonArray: JSONArray, userId: String): List<JSONObject>{
        val list = mutableListOf<JSONObject>()
        for (i in jsonArray.indices){
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getString("id") == userId) list.add(jsonObject)
        }
        return list
    }

    @Cron("At::d::08")
    fun biliBilliTask(){
        val list = biliBiliService.findByTask(true)
        for (biliBiliEntity in list){
            val ranking = biliBiliLogic.getRanking()
            val firstRank = ranking[0]
                biliBiliLogic.report(biliBiliEntity, firstRank.getValue("aid"), firstRank.getValue("cid"), 300)
                biliBiliLogic.share(biliBiliEntity, firstRank.getValue("aid"))
                biliBiliLogic.liveSign(biliBiliEntity)
                for (i in 0 until 2) {
                    val randomMap = ranking.random()
                    biliBiliLogic.tossCoin(biliBiliEntity, randomMap.getValue("aid"), 2)
                }
        }
    }
}

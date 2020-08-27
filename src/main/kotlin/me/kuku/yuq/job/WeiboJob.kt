package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.service.WeiboService
import javax.inject.Inject

@JobCenter
class WeiboJob {
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var weiboService: WeiboService

    private val groupMap = mutableMapOf<Long, MutableMap<Long, Long>>()
    private val qqMap = mutableMapOf<Long, Long>()

    @Cron("1m")
    fun groupWeibo(){
        for (qqGroupEntity in qqGroupService.findAll()) {
            val group = qqGroupEntity.group_
            if (!groupMap.containsKey(group)) groupMap[group] = mutableMapOf()
            val map = groupMap[group]!!
            val weiboJsonArray = qqGroupEntity.getWeiboJsonArray()
            for (i in weiboJsonArray.indices){
                val jsonObject = weiboJsonArray.getJSONObject(i)
                val weiboId = jsonObject.getLong("id")
                val commonResult = weiboLogic.getWeiboById(weiboId.toString())
                val list = commonResult.t ?: continue
                val firstWeiboPojo = if (list.isNotEmpty()) list[0] else continue
                if (map.containsKey(weiboId)){
                    val oldMBlogId = map.getValue(weiboId)
                    if (firstWeiboPojo.id.toLong() > oldMBlogId)
                        map[weiboId] = firstWeiboPojo.id.toLong()
                    list.forEach { weiboPojo ->
                        if (weiboPojo.id.toLong() > oldMBlogId){
                            yuq.groups[group]?.sendMessage(weiboLogic.convertStr(weiboPojo).toMessage())
                        }else return@forEach
                    }
                }else map[weiboId] = firstWeiboPojo.id.toLong()
            }
        }
    }

    @Cron("1m")
    fun qqWeibo(){
        val list = weiboService.findByMonitor(true)
        for (weiboEntity in list) {
            val qq = weiboEntity.qq
            val commonResult = weiboLogic.getFriendWeibo(weiboEntity)
            val weiboList = commonResult.t ?: continue
            if (weiboList.isEmpty()) continue
            val firstWeiboPojo = weiboList[0]
            val firstMBlogId = firstWeiboPojo.id.toLong()
            if (!qqMap.containsKey(qq)) qqMap[qq] = firstMBlogId
            val oldMBlogId = qqMap[qq]!!
            if (firstMBlogId > oldMBlogId) qqMap[qq] = firstMBlogId
            weiboList.forEach { weiboPojo ->
                if (weiboPojo.id.toLong() > oldMBlogId) {
                    val userId = weiboPojo.userId
                    val id = weiboPojo.id
                    val likeJsonArray = weiboEntity.getLikeJsonArray()
                    val likeList = this.isMatch(likeJsonArray, userId)
                    if (likeList.isNotEmpty())
                        weiboLogic.like(weiboEntity, id)
                    val commentJsonArray = weiboEntity.getCommentJsonArray()
                    this.isMatch(commentJsonArray, userId).forEach {
                        weiboLogic.comment(weiboEntity, id, it.getString("content"))
                    }
                    val forwardJsonArray = weiboEntity.getForwardJsonArray()
                    this.isMatch(forwardJsonArray, userId).forEach {
                        weiboLogic.forward(weiboEntity, id, it.getString("content"), null)
                    }
                    val group = weiboEntity.group_
                    if (group == null || group == 0L)
                        yuq.friends[qq]?.sendMessage(weiboLogic.convertStr(weiboPojo).toMessage())
                    else yuq.groups[group]?.get(qq)?.sendMessage(weiboLogic.convertStr(weiboPojo).toMessage())
                }else return@forEach
            }
        }
    }

    private fun isMatch(jsonArray: JSONArray, userId: String): List<JSONObject>{
        val list = mutableListOf<JSONObject>()
        for (i in jsonArray.indices){
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getString("id") == userId) list.add(jsonObject)
        }
        return list
    }

}
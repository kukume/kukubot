@file:Suppress("unused")

package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.pojo.WeiboPojo
import me.kuku.yuq.service.GroupService
import me.kuku.yuq.service.WeiboService
import java.lang.Exception
import javax.inject.Inject

@JobCenter
class WeiboJob {
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var weiboService: WeiboService

    private val groupMap = mutableMapOf<Long, MutableMap<Long, Long>>()
    private val userMap = mutableMapOf<Long, Long>()

    @Cron("30s")
    fun groupWeibo(){
        for (groupEntity in groupService.findAll()) {
            val group = groupEntity.group
            val weiboJsonArray = groupEntity.weiboJsonArray
            if (weiboJsonArray.isEmpty()) continue
            if (!groupMap.containsKey(group)){
                groupMap[group] = mutableMapOf()
            }
            val wbMap = groupMap[group]!!
            for (any in weiboJsonArray){
                val jsonObject = any as JSONObject
                val userId = jsonObject.getLong("id")
                val commonResult = weiboLogic.getWeiboById(userId.toString())
                val list = commonResult.t ?: continue
                if (wbMap.containsKey(userId)){
                    val newList = mutableListOf<WeiboPojo>()
                    for (weiboPojo in list){
                        if (weiboPojo.id.toLong() <= wbMap.getValue(userId)) break
                        newList.add(weiboPojo)
                    }
                    newList.forEach {
                            yuq.groups[group]?.sendMessage(mif.text("有新微博了\n").plus(weiboLogic.convertStr(it)))
                    }
                }
                val newId = list[0].id.toLong()
                if (!wbMap.containsKey(userId) || newId > wbMap.getValue(userId))
                    wbMap[userId] = list[0].id.toLong()
            }
        }
    }

    @Cron("30s")
    fun qqWeibo(){
        val weiboList = weiboService.findByMonitor(true)
        for (weiboEntity in weiboList) {
            val qq = weiboEntity.qq
            val commonResult = weiboLogic.getFriendWeibo(weiboEntity)
            val list = commonResult.t ?: break
            val newList: MutableList<WeiboPojo> = mutableListOf()
            if (userMap.containsKey(qq)){
                for (weiboPojo in list){
                    if (weiboPojo.id.toLong() <= userMap.getValue(qq)) break
                    newList.add(weiboPojo)
                }
                newList.forEach {
                    val userId = it.userId
                    val id = it.id
                    val likeList = match(weiboEntity.likeJsonArray, userId)
                    if (likeList.isNotEmpty()) weiboLogic.like(weiboEntity, id)
                    val commentList = match(weiboEntity.commentJsonArray, userId)
                    for (jsonObject in commentList) weiboLogic.comment(weiboEntity, id, jsonObject.getString("content"))
                    val forwardList = match(weiboEntity.forwardJsonArray, userId)
                    for (jsonObject in forwardList) weiboLogic.forward(weiboEntity, id, jsonObject.getString("content"), null)
                    val group = weiboEntity.group
                    val msg = "有新微博了！！\n" + weiboLogic.convertStr(it)
                    if (group == 0L){
                        try {
                            yuq.friends[qq]?.sendMessage(msg.toMessage())
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                    }else{
                        yuq.groups[group]?.members?.get(qq)?.sendMessage(msg.toMessage())
                    }
                }
            }
            userMap[qq] = list[0].id.toLong()
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

}
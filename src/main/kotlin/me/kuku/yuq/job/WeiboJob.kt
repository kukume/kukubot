package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
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
                val weiboPojo = commonResult.t?.get(0) ?: continue
                if (map.containsKey(weiboId)){
                    val oldMBlogId = map.getValue(weiboId)
                    if (weiboPojo.id.toLong() > oldMBlogId){
                        map[weiboId] = weiboPojo.id.toLong()
                        yuq.groups[group]?.sendMessage(weiboLogic.convertStr(weiboPojo).toMessage())
                    }
                }else map[weiboId] = weiboPojo.id.toLong()
            }
        }
    }

    @Cron("1m")
    fun qqWeibo(){
        val list = weiboService.findByMonitor(true)
        for (weiboEntity in list) {
            val qq = weiboEntity.qq
            val commonResult = weiboLogic.getFriendWeibo(weiboEntity)
            val weiboPojo = commonResult.t?.get(0) ?: continue
            val newMBlogId = weiboPojo.id.toLong()
            if (!qqMap.containsKey(qq)) qqMap[qq] = newMBlogId
            val oldMBlogId = qqMap[qq]!!
            if (newMBlogId > oldMBlogId){
                qqMap[qq] = newMBlogId
                val group = weiboEntity.group_
                if (group == null || group == 0L)
                    yuq.friends[qq]?.sendMessage(weiboLogic.convertStr(weiboPojo).toMessage())
                else yuq.groups[group]?.get(qq)?.sendMessage(weiboLogic.convertStr(weiboPojo).toMessage())
            }
        }
    }

}
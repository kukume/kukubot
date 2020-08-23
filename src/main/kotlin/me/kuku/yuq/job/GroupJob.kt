package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.QQGroupService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@JobCenter
class GroupJob {

    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var toolLogic: ToolLogic

    private val locMap = mutableMapOf<Long, String>()

    @Cron("At::h::00")
    fun onTimeAlarm(){
        val list = qqGroupService.findByOnTimeAlarm(true)
        val sdf = SimpleDateFormat("HH", Locale.CHINA)
        var hour = sdf.format(Date()).toInt()
        if (hour == 0) hour = 12
        if (hour > 12) hour -= 12
        val url = "https://u.iheit.com/kuku/bot/time/$hour.jpg"
        list.forEach { yuq.groups[it.group_]?.sendMessage(mif.image(url).toMessage()) }
    }

    @Cron("1m")
    fun locMonitor(){
        val list = qqGroupService.findByLocMonitor(true)
        list.forEach { qqGroupEntity ->
            val group = qqGroupEntity.group_
            val locList = toolLogic.hostLocPost()
            if (locList.isEmpty()) return@forEach
            val map = locList[0]
            if (locMap.containsKey(group)){
                val url = locMap[group]!!
                if (url != map["url"]){
                    locMap[group] = map.getValue("url")
                    yuq.groups[group]?.sendMessage("""
                        Loc有新帖了！！
                        标题：${map["title"]}
                        链接：${map["url"]}
                    """.trimIndent().toMessage())
                }
            }else locMap[group] = map.getValue("url")
        }
    }

}
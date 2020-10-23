@file:Suppress("unused")

package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.GroupService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@JobCenter
class GroupJob {

    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var toolLogic: ToolLogic

    private var locId = 0

    @Cron("At::h::00")
    fun onTimeAlarm(){
        val list = groupService.findByOnTimeAlarm(true)
        val sdf = SimpleDateFormat("HH", Locale.CHINA)
        var hour = sdf.format(Date()).toInt()
        if (hour == 0) hour = 12
        if (hour > 12) hour -= 12
        val url = "https://u.iheit.com/kuku/bot/time/$hour.jpg"
        list.forEach { yuq.groups[it.group]?.sendMessage(mif.imageByUrl(url).toMessage()) }
    }

    @Cron("1m")
    fun locMonitor(){
        val groupList = groupService.findByLocMonitor(true)
        for (groupEntity in groupList){
            val list = toolLogic.hostLocPost()
            val newList = mutableListOf<Map<String, String>>()
            if (list.isEmpty()) return
            if (locId != 0){
                for (map in list){
                    if (map.getValue("id").toInt() <= locId) break
                    newList.add(map)
                }
            }
            locId = list[0].getValue("id").toInt()
            newList.forEach { locMap ->
                yuq.groups[groupEntity.group]?.sendMessage(
                        """
                    Loc有新帖了！！
                    标题：${locMap["title"]}
                    昵称：${locMap["name"]}
                    链接：${locMap["url"]}
                """.trimIndent().toMessage()
                )
            }
        }
    }

}
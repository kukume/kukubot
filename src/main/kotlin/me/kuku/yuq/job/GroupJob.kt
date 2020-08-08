package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.service.QQGroupService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@JobCenter
class GroupJob {

    @Inject
    private lateinit var qqGroupService: QQGroupService

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

}
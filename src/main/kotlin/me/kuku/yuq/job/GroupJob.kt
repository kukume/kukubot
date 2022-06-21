package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.mif
import me.kuku.utils.DateTimeFormatterUtils
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.utils.YuqUtils
import org.springframework.stereotype.Component

@Component
@JobCenter
class GroupJob(
    private val groupService: GroupService
) {

    @Cron("At::h::00")
    suspend fun timekeeping() {
        val list = groupService.findAll().filter { it.config.timekeeping == Status.ON }
        for (groupEntity in list) {
            YuqUtils.sendMessage(groupEntity, mif.imageByUrl("https://api.kukuqaq.com/hour/${DateTimeFormatterUtils.formatNow("hh")}").toMessage())
        }
    }

}
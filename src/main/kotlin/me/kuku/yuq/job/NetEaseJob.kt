package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.NetEaseService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.NetEaseLogic
import javax.inject.Inject

@JobCenter
class NetEaseJob @Inject constructor(
    private val netEaseService: NetEaseService
) {


    @Cron("07:12")
    fun sign() {
        val list = netEaseService.findAll().filter { it.config.sign == Status.ON }
        for (netEaseEntity in list) {
            kotlin.runCatching {
                NetEaseLogic.sign(netEaseEntity)
                NetEaseLogic.listenMusic(netEaseEntity)
                NetEaseLogic.musicianSign(netEaseEntity)
            }
        }
    }

}
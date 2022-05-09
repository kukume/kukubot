package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import kotlinx.coroutines.delay
import me.kuku.yuq.entity.NetEaseService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.NetEaseLogic
import org.springframework.stereotype.Component

@JobCenter
@Component
class NetEaseJob (
    private val netEaseService: NetEaseService
) {


    @Cron("07:12")
    suspend fun sign() {
        val list = netEaseService.findAll().filter { it.config.sign == Status.ON }
        for (netEaseEntity in list) {
            kotlin.runCatching {
                delay(3000)
                NetEaseLogic.sign(netEaseEntity)
                delay(3000)
                NetEaseLogic.listenMusic(netEaseEntity)
            }
        }
    }

    @Cron("08:32")
    suspend fun musicianSign() {
        val list = netEaseService.findAll().filter { it.config.musicianSign == Status.ON }
        for (netEaseEntity in list) {
            kotlin.runCatching {
                for (i in 0..1) {
                    delay(3000)
                    NetEaseLogic.musicianSign(netEaseEntity)
                    delay(3000)
                    NetEaseLogic.publish(netEaseEntity)
                    delay(3000)
                    NetEaseLogic.publishMLog(netEaseEntity)
                }
            }
        }
    }

}
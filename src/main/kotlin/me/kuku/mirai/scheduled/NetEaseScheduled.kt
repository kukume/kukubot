package me.kuku.mirai.scheduled

import kotlinx.coroutines.delay
import me.kuku.mirai.entity.NetEaseService
import me.kuku.mirai.entity.Status
import me.kuku.mirai.logic.NetEaseLogic
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NetEaseScheduled(
    private val netEaseService: NetEaseService
) {

    @Scheduled(cron = "0 12 7 * * ?")
    suspend fun sign() {
        val list = netEaseService.findBySign(Status.ON)
        for (netEaseEntity in list) {
            kotlin.runCatching {
                delay(3000)
                NetEaseLogic.sign(netEaseEntity)
                delay(3000)
                NetEaseLogic.listenMusic(netEaseEntity)
            }
        }
    }

    @Scheduled(cron = "0 32 8 * * ?")
    suspend fun musicianSign() {
        val list = netEaseService.findByMusicianSign(Status.ON)
        for (netEaseEntity in list) {
            kotlin.runCatching {
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

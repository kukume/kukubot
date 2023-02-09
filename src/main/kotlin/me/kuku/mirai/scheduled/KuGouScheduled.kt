package me.kuku.mirai.scheduled

import kotlinx.coroutines.delay
import me.kuku.mirai.entity.KuGouService
import me.kuku.mirai.entity.Status
import me.kuku.mirai.logic.KuGouLogic
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class KuGouScheduled(
    private val kuGouService: KuGouService,
) {

    @Scheduled(cron = "0 41 3 * * ?")
    suspend fun sign() {
        val list = kuGouService.findBySign(Status.ON)
        for (kuGouEntity in list) {
            kotlin.runCatching {
                KuGouLogic.musicianSign(kuGouEntity)
                delay(2000)
                KuGouLogic.listenMusic(kuGouEntity)
                delay(2000)
            }
        }
    }

}

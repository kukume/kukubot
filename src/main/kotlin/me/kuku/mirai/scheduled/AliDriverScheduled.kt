package me.kuku.mirai.scheduled

import me.kuku.mirai.entity.AliDriverService
import me.kuku.mirai.entity.Status
import me.kuku.mirai.logic.AliDriverLogic
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AliDriverScheduled(
    private val aliDriverService: AliDriverService
) {

    @Scheduled(cron = "2 23 4 * * ?")
    suspend fun sign() {
        val list = aliDriverService.findBySign(Status.ON)
        for (aliDriverEntity in list) {
            kotlin.runCatching {
                AliDriverLogic.sign(aliDriverEntity)
            }
        }
    }




}

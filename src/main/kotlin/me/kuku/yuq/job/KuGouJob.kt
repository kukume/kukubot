package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.KuGouService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.KuGouLogic
import org.springframework.stereotype.Component

@JobCenter
@Component
class KuGouJob (
    private val kuGouService: KuGouService,
    private val kuGouLogic: KuGouLogic
) {


    @Cron("03:41")
    suspend fun sign() {
        val list = kuGouService.findAll().filter { it.config.sign == Status.ON }
        for (kuGouEntity in list) {
            kuGouLogic.musicianSign(kuGouEntity)
            kuGouLogic.listenMusic(kuGouEntity)
        }
    }

}
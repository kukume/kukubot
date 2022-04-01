package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.utils.JobManager
import me.kuku.yuq.entity.KuGouService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.KuGouLogic
import javax.inject.Inject

@JobCenter
class KuGouJob @Inject constructor(
    private val kuGouService: KuGouService,
    private val kuGouLogic: KuGouLogic
) {


    @Cron("03:41")
    fun sign() {
        val list = kuGouService.findAll().filter { it.config.sign == Status.ON }
        for (kuGouEntity in list) {
            JobManager.now {
                kuGouLogic.musicianSign(kuGouEntity)
                kuGouLogic.listenMusic(kuGouEntity)
            }
        }
    }

}
package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.MiHoYoService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.MiHoYoLogic
import javax.inject.Inject

@JobCenter
class MiHoYoJob @Inject constructor(
    private val miHoYoService: MiHoYoService
) {


    @Cron("05:13")
    suspend fun genShinSign() {
        val list = miHoYoService.findAll().filter { it.config.sign == Status.ON }
        for (miHoYoEntity in list) {
            MiHoYoLogic.sign(miHoYoEntity)
        }
    }

}
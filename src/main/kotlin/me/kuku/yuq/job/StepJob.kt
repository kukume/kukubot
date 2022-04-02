package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.StepService
import me.kuku.yuq.logic.LeXinStepLogic
import me.kuku.yuq.logic.XiaomiStepLogic
import javax.inject.Inject

@JobCenter
class StepJob @Inject constructor(
    private val stepService: StepService
) {

    @Cron("05:12")
    fun ss() {
        val list = stepService.findAll().filter { it.config.step > 0 }
        for (stepEntity in list) {
            val step = stepEntity.config.step
            XiaomiStepLogic.modifyStepCount(stepEntity, step)
            LeXinStepLogic.modifyStepCount(stepEntity, step)
        }
    }


}
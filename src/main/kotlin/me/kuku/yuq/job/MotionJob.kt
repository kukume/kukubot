package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.LeXinMotionService
import javax.inject.Inject

@JobCenter
class MotionJob {

    @Inject
    private lateinit var daoService: DaoService
    @Inject
    private lateinit var motionService: LeXinMotionService

    @Cron("At::h::8")
    fun motion(){
        val list = daoService.findMotionByAll()
        list?.forEach {
            val motionEntity = it as MotionEntity
            if (motionEntity.step != 0){
                motionService.modifyStepCount(motionEntity.step, motionEntity)
            }
        }
    }

}
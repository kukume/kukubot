package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.dao.MotionDao
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.service.impl.LeXinMotionServiceImpl
import javax.inject.Inject

@JobCenter
class MotionJob {

    @Inject
    private lateinit var motionDao: MotionDao
    @Inject
    private lateinit var motionService: LeXinMotionServiceImpl

    @Cron("At::h::8")
    fun motion(){
        val list = motionDao.findAll()
        list?.forEach {
            val motionEntity = it as MotionEntity
            if (motionEntity.step != 0){
                motionService.modifyStepCount(motionEntity.step, motionEntity)
            }
        }
    }

}
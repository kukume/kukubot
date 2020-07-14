package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.logic.LeXinMotionLogic
import me.kuku.yuq.service.MotionService
import javax.inject.Inject

@JobCenter
class MotionJob {

    @Inject
    private lateinit var motionService: MotionService
    @Inject
    private lateinit var leXinMotionLogic: LeXinMotionLogic

    @Cron("At::d::08")
    fun motion(){
        val list = motionService.findAll()
        list.forEach {
            if (it.step != 0){
                leXinMotionLogic.modifyStepCount(it.step, it)
            }
        }
    }

}
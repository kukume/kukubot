@file:Suppress("unused")

package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.logic.NeTeaseLogic
import me.kuku.yuq.service.NeTeaseService
import javax.inject.Inject

@JobCenter
class NeTeaseJob {

    @Inject
    private lateinit var neTeaseLogic: NeTeaseLogic
    @Inject
    private lateinit var neTeaseService: NeTeaseService

    @Cron("At::d::09")
    fun ne(){
        val list = neTeaseService.findAll()
        list.forEach {
            neTeaseLogic.sign(it)
            neTeaseLogic.listeningVolume(it)
        }
    }

}
package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.service.impl.QQServiceImpl
import javax.inject.Inject

@JobCenter
class QQJob {

    @Inject
    private lateinit var qqDao: QQDao
    @Inject
    private lateinit var qqService: QQServiceImpl

    @Cron("At::d::00:00")
    fun qqSign(){
        val list = qqDao.findAll()
        list?.forEach {
            val qqEntity = it as QQEntity
            if (qqEntity.status) {
                qqService.qqSign(qqEntity)
                qqService.anotherSign(qqEntity)
                qqService.groupLottery(qqEntity, 1132123L)
                qqService.vipSign(qqEntity)
                qqService.phoneGameSign(qqEntity)
                qqService.yellowSign(qqEntity)
                qqService.qqVideoSign1(qqEntity)
                qqService.qqVideoSign2(qqEntity)
                qqService.bigVipSign(qqEntity)
                qqService.qqMusicSign(qqEntity)
                qqService.gameSign(qqEntity)
                qqService.qPetSign(qqEntity)
                qqService.tribeSign(qqEntity)
                qqService.motionSign(qqEntity)
                qqService.blueSign(qqEntity)
                qqService.sVipMornSign(qqEntity)
                qqService.weiYunSign(qqEntity)
            }
        }
    }

    @Cron("At::h::6")
    fun sVipMorn() {
        val list = qqDao.findAll()
        list?.forEach {
            val qqEntity = it as QQEntity
            if (qqEntity.status) qqService.sVipMornClock(qqEntity)
        }
    }

}
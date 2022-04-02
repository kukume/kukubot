package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.BiliBiliService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.BiliBiliLogic
import javax.inject.Inject

@JobCenter
class BiliBilliJob @Inject constructor(
    private val biliBiliService: BiliBiliService
) {


    @Cron("03:23")
    fun sign() {
        val list = biliBiliService.findAll().filter { it.config.sign == Status.ON }
        for (biliBiliEntity in list) {
            val firstRank = BiliBiliLogic.ranking()[0]
            BiliBiliLogic.report(biliBiliEntity, firstRank.aid, firstRank.cid, 300)
            BiliBiliLogic.share(biliBiliEntity, firstRank.aid)
            BiliBiliLogic.liveSign(biliBiliEntity)
        }
    }


}
package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.Status
import me.kuku.yuq.entity.WeiboService
import me.kuku.yuq.logic.WeiboLogic
import javax.inject.Inject

@JobCenter
class WeiboJob @Inject constructor(
    private val weiboService: WeiboService
) {

    @Cron("04:51")
    fun sign() {
        val list = weiboService.findAll().filter { it.config.sign == Status.ON }
        for (weiboEntity in list) {
            WeiboLogic.superTalkSign(weiboEntity)
        }
    }


}
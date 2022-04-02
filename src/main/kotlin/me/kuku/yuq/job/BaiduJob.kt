package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.BaiduService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.BaiduLogic
import javax.inject.Inject

@JobCenter
class BaiduJob @Inject constructor(
    private val baiduService: BaiduService,
    private val baiduLogic: BaiduLogic
) {


    @Cron("02:41")
    suspend fun sign() {
        val list = baiduService.findAll().filter { it.config.sign == Status.ON }
        for (baiduEntity in list) {
            baiduLogic.tieBaSign(baiduEntity)
        }
    }


}
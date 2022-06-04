package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import kotlinx.coroutines.delay
import me.kuku.yuq.entity.BaiduService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.BaiduLogic
import org.springframework.stereotype.Component

@JobCenter
@Component
class BaiduJob (
    private val baiduService: BaiduService,
    private val baiduLogic: BaiduLogic
) {


    @Cron("02:41")
    suspend fun sign() {
        val list = baiduService.findAll().filter { it.config.sign == Status.ON }
        for (baiduEntity in list) {
            baiduLogic.tieBaSign(baiduEntity)
            for (i in 0 until 12) {
                delay(1000 * 15)
                baiduLogic.ybbWatchAd(baiduEntity)
            }
            for (i in 0 until 4) {
                delay(1000 * 30)
                baiduLogic.ybbWatchAd(baiduEntity, "v3")
            }
            baiduLogic.ybbSign(baiduEntity)
        }
    }


}
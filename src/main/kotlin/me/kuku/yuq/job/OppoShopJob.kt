package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import kotlinx.coroutines.runBlocking
import me.kuku.yuq.config.Transactional
import me.kuku.yuq.entity.OppoShopService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.OppoShopLogic
import me.kuku.yuq.utils.YuqUtils
import org.springframework.stereotype.Component

@Component
@JobCenter
class OppoShopJob(
    private val oppoShopService: OppoShopService
) {


    @Cron("03:12")
    fun sign() {
        val list = oppoShopService.findAll().filter { it.config.sign == Status.ON }
        for (oppoShopEntity in list) {
            kotlin.runCatching {
                OppoShopLogic.sign(oppoShopEntity)
                OppoShopLogic.shareGoods(oppoShopEntity)
                runBlocking {
                    OppoShopLogic.viewGoods(oppoShopEntity)
                }
            }
        }
    }

    @Cron("00:01")
    fun ss() = heyTapEarly()

    @Cron("19:32")
    fun sss() = heyTapEarly()

    @Transactional
    private fun heyTapEarly() {
        val list = oppoShopService.findAll().filter { it.config.earlyToBedClock == Status.ON }
        for (oppoShopEntity in list) {
            kotlin.runCatching {
                val result = OppoShopLogic.earlyBedRegistration(oppoShopEntity)
                if (result.failure()) {
                    YuqUtils.sendMessage(oppoShopEntity.qqEntity!!, "您的oppo早睡打卡打卡失败，请手动打卡")
                }
            }
        }
    }


}
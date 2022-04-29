package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.Status
import me.kuku.yuq.entity.WeiboService
import me.kuku.yuq.config.executeBlock
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.logic.WeiboPojo
import me.kuku.yuq.utils.YuqUtils
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@JobCenter
@Component
class WeiboJob (
    private val weiboService: WeiboService,
    private val transactionTemplate: TransactionTemplate
) {

    private val userMap = mutableMapOf<Long, Long>()

    @Cron("04:51")
    suspend fun sign() {
        val list = weiboService.findAll().filter { it.config.sign == Status.ON }
        for (weiboEntity in list) {
            WeiboLogic.superTalkSign(weiboEntity)
        }
    }

    @Cron("2m")
    suspend fun userMonitor() = transactionTemplate.executeBlock {
        val weiboList = weiboService.findAll().filter { it.config.push == Status.ON }
        for (weiboEntity in weiboList) {
            val qq = weiboEntity.qqEntity!!.qq
            val result = WeiboLogic.friendWeibo(weiboEntity)
            val list = result.data ?: continue
            val newList = mutableListOf<WeiboPojo>()
            if (userMap.containsKey(qq)) {
                for (weiboPojo in list) {
                    if (weiboPojo.id <= userMap[qq]!!) break
                    newList.add(weiboPojo)
                }
                for (weiboPojo in newList) {
                    YuqUtils.sendMessage(weiboEntity.qqEntity!!, "有新微博了！！\n${WeiboLogic.convert(weiboPojo)}")
                }
            }
            userMap[qq] = list[0].id
        }
        null
    }

}
package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.BiliBiliService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.logic.BiliBiliPojo
import me.kuku.yuq.utils.YuqUtils
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@JobCenter
@Component
class BiliBilliJob (
    private val biliBiliService: BiliBiliService
) {

    private val liveMap = mutableMapOf<Long, MutableMap<Long, Boolean>>()
    private val userMap = mutableMapOf<Long, Long>()


    @Cron("03:23")
    suspend fun sign() {
        val list = biliBiliService.findAll().filter { it.config.sign == Status.ON }
        for (biliBiliEntity in list) {
            val firstRank = BiliBiliLogic.ranking()[0]
            BiliBiliLogic.report(biliBiliEntity, firstRank.aid, firstRank.cid, 300)
            BiliBiliLogic.share(biliBiliEntity, firstRank.aid)
            BiliBiliLogic.liveSign(biliBiliEntity)
        }
    }

    @Cron("2m")
    @Transactional
    suspend fun liveMonitor() {
        val list = biliBiliService.findAll().filter { it.config.live == Status.ON }
        for (biliBiliEntity in list) {
            val result = BiliBiliLogic.followed(biliBiliEntity)
            if (result.failure()) continue
            val qqEntity = biliBiliEntity.qqEntity!!
            val qq = qqEntity.qq
            if (!liveMap.containsKey(qq)) liveMap[qq] = mutableMapOf()
            val map = liveMap[qq]!!
            for (up in result.data()) {
                val id = up.id.toLong()
                val name = up.name
                val live = BiliBiliLogic.live(id.toString())
                if (live.id.isEmpty()) continue
                val b = live.status
                if (map.containsKey(id)) {
                    if (map[id] != b) {
                        map[id] = b
                        val msg = if (b) "直播啦！！" else "下播了！！"
                        YuqUtils.sendMessage(qqEntity, """
                            哔哩哔哩开播提醒：
                            $name$msg
                            标题：${live.title}
                            链接：${live.url}
                        """.trimIndent())
                    }
                } else map[id] = b
            }
        }
    }


    @Cron("2m")
    @Transactional
    suspend fun userMonitor() {
        val biliBiliList = biliBiliService.findAll().filter { it.config.push == Status.ON }
        for (biliBiliEntity in biliBiliList) {
            val qq = biliBiliEntity.qqEntity!!.qq
            val result = BiliBiliLogic.friendDynamic(biliBiliEntity)
            val list = result.data ?: continue
            val newList = mutableListOf<BiliBiliPojo>()
            if (userMap.containsKey(qq)) {
                val oldId = userMap[qq]!!
                for (biliBiliPojo in list) {
                    if (biliBiliPojo.id.toLong() <= oldId) break
                    newList.add(biliBiliPojo)
                }
                for (biliBiliPojo in newList) {
                    YuqUtils.sendMessage(biliBiliEntity.qqEntity!!, "哔哩哔哩有新动态了！！\n${BiliBiliLogic.convertStr(biliBiliPojo)}")
                }
            }
            userMap[qq] = list[0].id.toLong()
        }
    }

    @Cron("05:21")
    suspend fun tossCoin() {
        val list = biliBiliService.findAll().filter { it.config.coin == Status.ON }
        for (biliBiliEntity in list) {
            val ranking = BiliBiliLogic.ranking()
            val arr = arrayOf(2, 2, 1)
            for (i in 0 until 3) {
                BiliBiliLogic.tossCoin(biliBiliEntity, ranking.random().aid, arr[i])
            }
        }

    }

}
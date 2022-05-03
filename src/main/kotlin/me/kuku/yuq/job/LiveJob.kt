@file:Suppress("DuplicatedCode")

package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.DouYuService
import me.kuku.yuq.entity.HuYaService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.DouYuLogic
import me.kuku.yuq.logic.HuYaLogic
import me.kuku.yuq.utils.YuqUtils
import org.springframework.stereotype.Component

@Component
@JobCenter
class LiveJob(
    private val douYuService: DouYuService,
    private val douYuLogic: DouYuLogic,
    private val huYaService: HuYaService,
    private val huYaLogic: HuYaLogic
) {

    private val douYuLiveMap = mutableMapOf<Long, MutableMap<Long, Boolean>>()

    private val huYaLiveMap = mutableMapOf<Long, MutableMap<Long, Boolean>>()

    @Cron("1m")
    suspend fun douYu() {
        val list = douYuService.findAll().filter { it.config.live == Status.ON }
        for (douYuEntity in list) {
            val baseResult = douYuLogic.room(douYuEntity)
            if (baseResult.failure()) continue
            val rooms = baseResult.data()
            val qqEntity = douYuEntity.qqEntity!!
            val qq = qqEntity.qq
            if (!douYuLiveMap.containsKey(qq)) douYuLiveMap[qq] = mutableMapOf()
            val map = douYuLiveMap[qq]!!
            for (room in rooms) {
                val id = room.roomId
                val b = room.showStatus
                if (map.containsKey(id)) {
                    if (map[id] != b) {
                        map[id] = b
                        val msg = if (b) "直播啦！！" else "下播啦"
                        YuqUtils.sendMessage(qqEntity, """
                            斗鱼直播开播提醒
                            ${room.nickName}$msg
                            标题：${room.name}
                            分类：${room.gameName}
                            在线：${room.online}
                            链接：${room.url}
                        """.trimIndent())
                    }
                } else map[id] = b
            }
        }
    }

    @Cron("1m")
    fun huYa() {
        val list = huYaService.findAll().filter { it.config.live == Status.ON }
        for (huYaEntity in list) {
            val baseResult = huYaLogic.live(huYaEntity)
            if (baseResult.failure()) continue
            val lives = baseResult.data()
            val qqEntity = huYaEntity.qqEntity!!
            val qq = qqEntity.qq
            if (!huYaLiveMap.containsKey(qq)) huYaLiveMap[qq] = mutableMapOf()
            val map = huYaLiveMap[qq]!!
            for (room in lives) {
                val id = room.roomId
                val b = room.isLive
                if (map.containsKey(id)) {
                    if (map[id] != b) {
                        map[id] = b
                        val msg = if (b) "直播啦！！" else "下播啦"
                        YuqUtils.sendMessage(qqEntity, """
                            虎牙直播开播提醒
                            ${room.nick}$msg
                            标题：${room.liveDesc}
                            分类：${room.gameName}
                            链接：${room.url}
                        """.trimIndent())
                    }
                } else map[id] = b
            }
        }
    }


}
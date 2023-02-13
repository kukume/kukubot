package me.kuku.mirai.scheduled

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import me.kuku.mirai.entity.BiliBiliService
import me.kuku.mirai.entity.Status
import me.kuku.mirai.logic.BiliBiliLogic
import me.kuku.mirai.logic.BiliBiliPojo
import me.kuku.utils.client
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.concurrent.TimeUnit

@Component
class BiliBilliScheduled(
    private val biliBiliService: BiliBiliService,
    private val bot: Bot
) {

    private val liveMap = mutableMapOf<Long, MutableMap<Long, Boolean>>()
    private val userMap = mutableMapOf<Long, Long>()


    @Scheduled(cron = "0 23 3 * * ?")
    suspend fun sign() {
        val list = biliBiliService.findBySign(Status.ON)
        for (biliBiliEntity in list) {
            kotlin.runCatching {
                val firstRank = BiliBiliLogic.ranking()[0]
                delay(5000)
                BiliBiliLogic.report(biliBiliEntity, firstRank.aid, firstRank.cid, 300)
                delay(5000)
                BiliBiliLogic.share(biliBiliEntity, firstRank.aid)
                delay(5000)
                BiliBiliLogic.liveSign(biliBiliEntity)
            }
            delay(3000)
        }
    }

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
    suspend fun liveMonitor() {
        val list = biliBiliService.findByLive(Status.ON)
        for (biliBiliEntity in list) {
            val result = BiliBiliLogic.followed(biliBiliEntity)
            delay(3000)
            if (result.failure()) continue
            val qq = biliBiliEntity.qq
            if (!liveMap.containsKey(qq)) liveMap[qq] = mutableMapOf()
            val map = liveMap[qq]!!
            for (up in result.data()) {
                val id = up.id.toLong()
                val name = up.name
                delay(3000)
                val live = BiliBiliLogic.live(id.toString())
                if (live.id.isEmpty()) continue
                val b = live.status
                if (map.containsKey(id)) {
                    if (map[id] != b) {
                        map[id] = b
                        val msg = if (b) "直播啦！！" else "下播了！！"
                        val text = "#哔哩哔哩开播提醒\n#$name $msg\n标题：${live.title}\n链接：${live.url}"
                        val imageUrl = live.imageUrl
                        if (imageUrl.isEmpty())
                            bot.getFriend(qq)?.sendMessage(text)
                        else {
                            client.get(imageUrl).body<InputStream>().toExternalResource().use { er ->
                                bot.getFriend(qq)?.let {
                                    val image = it.uploadImage(er)
                                    it.sendMessage(image + text)
                                }
                            }
                        }
                    }
                } else map[id] = b
            }
        }
    }

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
    suspend fun userMonitor() {
        val biliBiliList = biliBiliService.findByPush(Status.ON)
        for (biliBiliEntity in biliBiliList) {
            val qq = biliBiliEntity.qq
            delay(3000)
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
                    val text = "#哔哩哔哩动态推送\n${BiliBiliLogic.convertStr(biliBiliPojo)}"
                    val bvId = if (biliBiliPojo.bvId.isNotEmpty()) biliBiliPojo.bvId
                    else if (biliBiliPojo.forwardBvId.isNotEmpty()) biliBiliPojo.forwardBvId
                    else ""
                    try {
                        if (bvId.isNotEmpty()) {
//                            var file: File? = null
//                            try {
//                                delay(3000)
//                                file = BiliBiliLogic.videoByBvId(biliBiliEntity, biliBiliPojo.bvId)
//                            } finally {
//                                file?.delete()
//                            }
                            error("mirai好像不支持私聊文件")
                        } else if (biliBiliPojo.picList.isNotEmpty() || biliBiliPojo.forwardPicList.isNotEmpty()) {
                            val friend = bot.getFriend(qq) ?: continue
                            val picList = biliBiliPojo.picList
                            picList.addAll(biliBiliPojo.forwardPicList)
                            var messageChain = messageChainOf()
                            picList.forEach {
                                val image = client.get(it).body<InputStream>().toExternalResource().use { er ->
                                    friend.uploadImage(er)
                                }
                                messageChain = messageChain.plus(image)
                            }
                            friend.sendMessage(messageChain + text)
                        } else bot.getFriend(qq)?.sendMessage(text)
                    } catch (e: Exception) {
                        bot.getFriend(qq)?.sendMessage(text)
                    }
                }
            }
            userMap[qq] = list[0].id.toLong()
        }
    }

}

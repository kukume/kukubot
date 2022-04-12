package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import kotlinx.coroutines.delay
import me.kuku.yuq.entity.HostLocService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.HostLocLogic
import me.kuku.yuq.logic.HostLocPost
import me.kuku.yuq.utils.YuqUtils
import javax.inject.Inject

@JobCenter
class HostLocJob @Inject constructor(
    private val hostLocService: HostLocService
) {

    private var locId = 0

    @Cron("1s")
    suspend fun locPush() {
        val list = HostLocLogic.post()
        if (list.isEmpty()) return
        val newList = mutableListOf<HostLocPost>()
        if (locId != 0) {
            for (hostLocPost in list) {
                if (hostLocPost.id <= locId) break
                newList.add(hostLocPost)
            }
        }
        locId = list[0].id
        for (hostLocPost in newList) {
            delay(3000)
            val hostLocList = hostLocService.findByStatus(Status.ON).filter { it.config.push == Status.ON }
            for (hostLocEntity in hostLocList) {
                val str = """
                    Loc有新帖了！！
                    标题：${hostLocPost.title}
                    昵称：${hostLocPost.name}
                    链接：${hostLocPost.url}
                    内容：${HostLocLogic.postContent(hostLocPost.url, hostLocEntity.cookie)}
                """.trimIndent()
                YuqUtils.sendMessage(hostLocEntity.qqEntity!!, str)
            }
        }
    }

    @Cron("04:12")
    fun sign() {
        val list = hostLocService.findByStatus(Status.ON).filter { it.config.sign == Status.ON }
        for (hostLocEntity in list) {
            HostLocLogic.sign(hostLocEntity.cookie)
        }
    }



}
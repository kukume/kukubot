package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.annotation.PrivateController
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.HostLocLogic
import me.kuku.yuq.logic.HostLocPost
import me.kuku.yuq.transaction
import me.kuku.yuq.utils.YuqUtils
import javax.inject.Inject

@PrivateController
class HostLocController @Inject constructor(
    private val qqService: QqService
){

    @Action("{operate} {status}")
    fun operate(operate: String, status: Boolean, qqEntity: QqEntity): String? {
        when (operate) {
            "loc推送" -> qqEntity.config.locPush = status.toStatus()
            else -> return null
        }
        qqService.save(qqEntity)
        return "${operate}${if (status) "开启" else "关闭"}成功"
    }
}

@JobCenter
class HostLocJob @Inject constructor(
    private val qqService: QqService,
    private val groupService: GroupService
) {
    private var locId = 0

    @Cron("1m")
    fun locMonitor() {
        val list = HostLocLogic.post()
        if (list.isEmpty()) return
        val newList: MutableList<HostLocPost> = ArrayList()
        if (locId != 0) {
            for (post in list) {
                if (post.id <= locId) break
                newList.add(post)
            }
        }
        locId = list[0].id
        for (post in newList) {
            val str = """
                Loc有新帖了！！
                标题：${post.title}
                昵称：${post.name}
                链接：${post.url}
                内容：${HostLocLogic.postContent(post.url)}
            """.trimIndent()
            transaction {
                for (qqEntity in qqService.findAll()) {
                    if (qqEntity.config.locPush == Status.ON)
                        YuqUtils.sendMessage(qqEntity, str)
                }
                for (groupEntity in groupService.findAll()) {
                    if (groupEntity.config.locPush == Status.ON)
                        YuqUtils.sendMessage(groupEntity, str)
                }
            }
        }
    }
}
package me.kuku.mirai.event

import me.kuku.mirai.entity.GroupService
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.GroupMessageEvent
import org.springframework.stereotype.Component

@Component
class GroupEvent(
    private val groupService: GroupService,
    private val bot: Bot
) {

    fun GroupMessageEvent.forward() {
    }

}

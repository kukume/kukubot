package me.kuku.mirai.event

import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import org.springframework.stereotype.Component

@Component
class VerifyEvent {

    suspend fun NewFriendRequestEvent.agree() {
        accept()
    }

    suspend fun BotInvitedJoinGroupRequestEvent.agree() {
        accept()
    }


}

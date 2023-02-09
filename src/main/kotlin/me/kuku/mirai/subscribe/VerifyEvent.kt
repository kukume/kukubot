package me.kuku.mirai.subscribe

import net.mamoe.mirai.event.events.NewFriendRequestEvent
import org.springframework.stereotype.Component

@Component
class VerifyEvent {

    suspend fun NewFriendRequestEvent.agree() {
        accept()
    }

}

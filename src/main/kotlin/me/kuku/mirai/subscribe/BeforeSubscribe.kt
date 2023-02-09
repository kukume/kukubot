package me.kuku.mirai.subscribe

import me.kuku.mirai.utils.MiraiExceptionHandler
import me.kuku.mirai.utils.at
import net.mamoe.mirai.event.events.MessageEvent
import org.springframework.stereotype.Component

@Component
class BeforeSubscribe {

    fun MiraiExceptionHandler.xo() {

        handler<MessageEvent, IllegalStateException> {
            with(event) {
                subject.sendMessage(at() + throwable.toString())
            }
        }

    }


}

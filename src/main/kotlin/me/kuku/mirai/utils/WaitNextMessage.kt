package me.kuku.mirai.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val contextSessionCacheMap = ConcurrentHashMap<String, Continuation<MessageChain>>()

private fun waitNextMessageCommon(code: String, maxTime: Long): MessageChain {
    return runBlocking {
        try {
            withTimeout(maxTime){
                val msg = suspendCoroutine {
                    contextSessionCacheMap.merge(code, it) { _, _ ->
                        throw IllegalStateException("Account $code was still waiting.")
                    }
                }
                msg
            }
        }catch (e: Exception){
            contextSessionCacheMap.remove(code)
            error(e.message ?: "")
        }
    }
}

context(GroupMessageEvent)
fun waitNextMessage(maxTime: Long = 30000): MessageChain {
    return waitNextMessageCommon("group-${sender.group.id}-${sender.id}", maxTime)
}

@Service
class ContextSession {

    fun GroupMessageEvent.wait() {
        contextSessionCacheMap.remove("group-${sender.group.id}-${sender.id}")?.resume(message)
    }

}

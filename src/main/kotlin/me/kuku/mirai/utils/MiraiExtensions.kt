package me.kuku.mirai.utils

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.SingleMessage

fun MessageChain.split(): List<SingleMessage> {
    val list = mutableListOf<SingleMessage>()
    for (singleMessage in this) {
        if (singleMessage is PlainText) {
            val content = singleMessage.content
            val arr = content.split(" ")
            for (s in arr) {
                if (s.isEmpty()) continue
                list.add(PlainText(s))
            }
        } else list.add(singleMessage)
    }
    return list
}

inline fun <reified T: Any> GroupMessageEvent.firstArg(): T {
    val singleMessage = message.split().getOrNull(2) ?: error("Sorry, this feature requires 1 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> GroupMessageEvent.secondArg(): T {
    val singleMessage = message.split().getOrNull(3) ?: error("Sorry, this feature requires 1 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> GroupMessageEvent.thirdArg(): T {
    val singleMessage = message.split().getOrNull(3) ?: error("Sorry, this feature requires 1 additional input.")
    return singleMessage as T
}

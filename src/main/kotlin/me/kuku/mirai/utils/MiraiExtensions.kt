package me.kuku.mirai.utils

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
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

inline fun <reified T: Any> MessageEvent.firstArg(): T {
    val singleMessage = message.split().getOrNull(2) ?: error("Sorry, this feature requires 1 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.secondArg(): T {
    val singleMessage = message.split().getOrNull(3) ?: error("Sorry, this feature requires 2 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.thirdArg(): T {
    val singleMessage = message.split().getOrNull(4) ?: error("Sorry, this feature requires 3 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.fourthArg(): T {
    val singleMessage = message.split().getOrNull(5) ?: error("Sorry, this feature requires 4 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.fifthArg(): T {
    val singleMessage = message.split().getOrNull(6) ?: error("Sorry, this feature requires 5 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.sixthArg(): T {
    val singleMessage = message.split().getOrNull(7) ?: error("Sorry, this feature requires 6 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.seventhArg(): T {
    val singleMessage = message.split().getOrNull(8) ?: error("Sorry, this feature requires 7 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.eighthArg(): T {
    val singleMessage = message.split().getOrNull(9) ?: error("Sorry, this feature requires 8 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.ninthArg(): T {
    val singleMessage = message.split().getOrNull(10) ?: error("Sorry, this feature requires 9 additional input.")
    return singleMessage as T
}

inline fun <reified T: Any> MessageEvent.tenthArg(): T {
    val singleMessage = message.split().getOrNull(11) ?: error("Sorry, this feature requires 10 additional input.")
    return singleMessage as T
}

fun MessageEvent.at() = At(sender.id)

fun MessageEvent.atNewLine() = At(sender.id) + "\n"

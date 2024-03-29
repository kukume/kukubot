@file:Suppress("MemberVisibilityCanBePrivate")

package me.kuku.mirai.utils

import kotlinx.coroutines.*
import me.kuku.mirai.config.superclasses
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.AtAll
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import kotlin.reflect.KClass

private val globalBefore: MutableMap<KClass<out MessageEvent>, MutableList<suspend MessageEvent.() -> Unit>> = mutableMapOf()
private val superClassCache: MutableMap<KClass<*>, Set<KClass<*>>> = mutableMapOf()

class MiraiSubscribe<R: MessageEvent> {

    val threadLocal = ThreadLocal<MutableMap<String, Any>>()

    private val before: MutableList<suspend R.() -> Unit> = mutableListOf()
    private val after: MutableList<suspend R.() -> Unit> = mutableListOf()
    private val filters: MutableList<FilterAndInvoke<R>> = mutableListOf()

    class FilterAndInvoke<R>(val filter: Filter<R>, val exec: suspend R.() -> Any?)
    class Filter<R>(val filter: R.() -> Boolean) {

        operator fun plus(f: Filter<R>) = Filter<R> { filter() && f.filter.invoke(this) }

        infix fun and(f: Filter<R>) = Filter<R> { filter() && f.filter.invoke(this) }

        infix fun or(f: Filter<R>) = Filter<R> { filter() || f.filter.invoke(this) }

        infix fun xor(f: Filter<R>) = Filter<R> { filter() xor f.filter.invoke(this) }

        fun not() = Filter<R> { !filter() }

    }

    private fun Filter<R>.push(exec: suspend R.() -> Any?) = filters.add(FilterAndInvoke(this, exec))
    private fun filterBuild(filter: R.() -> Boolean) = Filter(filter)

    fun set(key: String, value: Any) {
        val cacheMap = threadLocal.get()
        cacheMap[key] = value
    }

    fun set(value: Any) {
        val name = value::class.java.simpleName
        val key = name.substring(0, 1).lowercase() + name.substring(1)
        set(key, value)
    }

    inline fun <reified T: Any> get(key: String): T? {
        val cacheMap = threadLocal.get()
        return cacheMap[key] as? T
    }

    inline fun <reified T: Any> getOrFail(key: String): T {
        return get(key) ?: error("$key not found")
    }

    inline fun <reified T: Any> firstAttr(): T {
        val cacheMap = threadLocal.get()
        return cacheMap.values.toList()[0] as T
    }

    inline fun <reified T: Any> secondAttr(): T {
        val cacheMap = threadLocal.get()
        return cacheMap.values.toList()[1] as T
    }

    inline fun <reified T: Any> thirdAttr(): T {
        val cacheMap = threadLocal.get()
        return cacheMap.values.toList()[2] as T
    }

    @Suppress("UNCHECKED_CAST")
    fun globalBefore(kClass: KClass<R>, block: suspend R.() -> Unit) {
        globalBefore[kClass] = globalBefore.getOrDefault(kClass, mutableListOf()).also { it.add(block as suspend MessageEvent.() -> Unit) }
    }

    fun before(block: suspend R.() -> Unit) = before.add(block)
    fun after(block: suspend R.() -> Unit) = after.add(block)
    fun startsWith(text: String) = filterBuild { this.message.contentToString().startsWith(text) }
    fun endsWith(text: String) = filterBuild { this.message.contentToString().endsWith(text) }

    fun regex(text: String) = filterBuild { Regex(text).matches(this.message.contentToString()) }

    fun case(text: String) = filterBuild { this.message.contentToString() == text }

    fun contains(text: String) = filterBuild { this.message.contentToString().contains(text) }

    fun filter(block: R.() -> Boolean) = filterBuild { block() }

    fun atBot() = filterBuild { this.message.firstIsInstanceOrNull<At>()?.target == bot.id }

    fun atAll() = filterBuild { message.firstIsInstanceOrNull<AtAll>() != null }

    suspend infix fun Filter<R>.reply(block: suspend R.() -> Any?) = push { executeReply(this, block) }

    suspend infix fun Filter<R>.quoteReply(block: suspend R.() -> Any?) = push { executeQuoteReply(this, block) }

    suspend infix fun Filter<R>.atReply(block: suspend R.() -> Any?) = push { executeAtReply(this, block) }

    suspend infix fun Filter<R>.atNewLineReply(block: suspend R.() -> Any?) = push { executeAtNewLineReply(this, block) }

    suspend infix fun String.reply(block: suspend R.() -> Any?) {
        filterBuild { this.message.contentToString() == this@reply }.push { executeReply(this, block) }
    }

    suspend infix fun String.quoteReply(block: suspend R.() -> Any?) {
        filterBuild { this.message.contentToString() == this@quoteReply }.push { executeQuoteReply(this, block) }
    }

    suspend infix fun String.atReply(block: suspend R.() -> Any?) {
        filterBuild { this.message.contentToString() == this@atReply }.push { executeAtReply(this, block) }
    }

    suspend infix fun String.atNewLineReply(block: suspend R.() -> Any?) {
        filterBuild { this.message.contentToString() == this@atNewLineReply }.push { executeAtNewLineReply(this, block) }
    }

    private suspend fun execute(r: R, block: suspend R.() -> Any?): Any? {
        for (function in before) {
            function.invoke(r)
        }
        val result =  block(r)
        for (function in after) {
            function.invoke(r)
        }
        return result
    }

    private suspend fun executeReply(r: R, block: suspend R.() -> Any?) {
        when(val message = block(r)) {
            is Message -> r.subject.sendMessage(message)
            null,
            is Unit -> Unit
            else -> r.subject.sendMessage(message.toString())
        }
    }

    private suspend fun executeQuoteReply(r: R, block: suspend R.() -> Any?) {
        when(val message = block(r)) {
            is Message -> r.subject.sendMessage(r.message.quote() + message)
            null,
            is Unit -> Unit
            else -> r.subject.sendMessage(r.message.quote() + message.toString())
        }
    }

    private suspend fun executeAtReply(r: R, block: suspend R.() -> Any?) {
        when(val message = block(r)) {
            is Message -> r.subject.sendMessage(At(r.sender)  + message)
            null,
            is Unit -> Unit
            else -> r.subject.sendMessage(At(r.sender) + message.toString())
        }
    }

    private suspend fun executeAtNewLineReply(r: R, block: suspend R.() -> Any?) {
        when(val message = block(r)) {
            is Message -> r.subject.sendMessage(At(r.sender) + "\n" + message)
            null,
            is Unit -> Unit
            else -> r.subject.sendMessage(At(r.sender) + "\n" + message.toString())
        }
    }

    suspend fun invoke(r: R) {
        val clazz = r::class
        withContext(Dispatchers.Default + threadLocal.asContextElement(mutableMapOf())) {
            for (filter in filters) {
                if (filter.filter.filter.invoke(r)) {
                    for (entry in globalBefore.entries) {
                        val superClassSet = superClassCache[clazz] ?: superclasses(clazz).also { superClassCache[clazz] = it }
                        if (superClassSet.contains(entry.key)) {
                            for (func in entry.value) {
                                func.invoke(r)
                            }
                        }
                    }
                    for (function in before) {
                        function.invoke(r)
                    }
                    filter.exec.invoke(r)
                    for (function in after) {
                        function.invoke(r)
                    }
                }
            }
            threadLocal.remove()
        }
    }

}

typealias GroupMessageSubscribe = MiraiSubscribe<GroupMessageEvent>
typealias MessageSubscribe = MiraiSubscribe<MessageEvent>
typealias PrivateMessageSubscribe = MiraiSubscribe<FriendMessageEvent>
typealias GroupTempMessageSubscribe = MiraiSubscribe<GroupTempMessageEvent>

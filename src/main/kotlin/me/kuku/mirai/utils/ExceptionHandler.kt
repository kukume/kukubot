package me.kuku.mirai.utils

import me.kuku.mirai.config.superclasses
import net.mamoe.mirai.event.Event
import kotlin.reflect.KClass

private val superClassCache: MutableMap<KClass<*>, Set<KClass<*>>> = mutableMapOf()

class EventExceptionContext<E: Event, R: Throwable>(
    val event: E,
    val throwable: R
)

data class MiraiExceptionHandlerKey(val eventClass: KClass<out Event>, val throwableClass: KClass<out Throwable>)

private typealias HandlerFunction = suspend EventExceptionContext<out Event, out Throwable>.() -> Unit

class MiraiExceptionHandler {

    val exceptions: MutableMap<MiraiExceptionHandlerKey, MutableList<HandlerFunction>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified E: Event, reified T: Throwable> handler(noinline block: suspend EventExceptionContext<E, T>.() -> Unit) {
        val key = MiraiExceptionHandlerKey(E::class, T::class)
        exceptions[key] = (exceptions[key] ?: mutableListOf()).also { it.add(block as HandlerFunction) }
    }

}

context(Event)
suspend fun MiraiExceptionHandler.exceptionHandler(block: suspend () -> Unit) {
    kotlin.runCatching {
        block()
    }.onFailure {
        val nowEventClass = this@Event::class
        val nowThrowableClass = it::class
        val context = EventExceptionContext(this@Event, it)
        val exceptions = this.exceptions
        val superClassSet = superClassCache[nowEventClass] ?: superclasses(nowEventClass).also { set -> superClassCache[nowEventClass] = set }
        val throwableClassSet = superClassCache[nowThrowableClass] ?: superclasses(nowThrowableClass).also { set -> superClassCache[nowThrowableClass] = set }
        for (entry in exceptions) {
            val key = entry.key
            val eventClass = key.eventClass
            val throwableClass = key.throwableClass
            if (throwableClassSet.contains(throwableClass) && superClassSet.contains(eventClass)) {
                for (func in entry.value) {
                    func.invoke(context)
                }
            }
        }
        throw it
    }
}

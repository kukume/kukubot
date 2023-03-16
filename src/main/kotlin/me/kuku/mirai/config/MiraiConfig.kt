package me.kuku.mirai.config

import kotlinx.coroutines.runBlocking
import me.kuku.mirai.utils.MiraiExceptionHandler
import me.kuku.mirai.utils.MiraiSubscribe
import me.kuku.mirai.utils.exceptionHandler
import me.kuku.utils.JobManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.BotConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmName

@Component
class MiraiBean(
    private val miraiConfig: MiraiConfig,
    private val applicationContext: ApplicationContext
): ApplicationListener<ContextRefreshedEvent> {

    @Suppress("UNCHECKED_CAST")
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val bot = applicationContext.getBean(Bot::class.java)
        val eventChannel = bot.eventChannel
        val names = applicationContext.beanDefinitionNames
        val clazzList = mutableListOf<Class<*>>()
        for (name in names) {
            applicationContext.getType(name)?.let {
                clazzList.add(it)
            }
        }
        val handler = MiraiExceptionHandler()
        for (clazz in clazzList) {
            val functions = kotlin.runCatching {
                clazz.kotlin.declaredMemberExtensionFunctions
            }.getOrNull() ?: continue
            for (function in functions) {
                val type = function.extensionReceiverParameter?.type
                val kClass = type?.classifier as? KClass<*>
                if (superclasses(kClass).contains(Event::class)) {
                    eventChannel.subscribeAlways(kClass as KClass<out Event>) {
                        handler.exceptionHandler {
                            function.callSuspend(applicationContext.getBean(clazz), this)
                        }
                    }
                }
                if (kClass?.jvmName == "me.kuku.mirai.utils.MiraiExceptionHandler") {
                    runBlocking {
                        function.callSuspend(applicationContext.getBean(clazz), handler)
                    }
                }
                if (kClass?.jvmName == "net.mamoe.mirai.event.MessageSubscribersBuilder") {
                    when (type.arguments[0].type?.classifier) {
                        GroupMessageEvent::class -> {
                            eventChannel.subscribeGroupMessages {
                                JobManager.now {
                                    function.callSuspend(applicationContext.getBean(clazz), this@subscribeGroupMessages)
                                }
                            }
                        }
                        FriendMessageEvent::class -> {
                            eventChannel.subscribeFriendMessages {
                                JobManager.now {
                                    function.callSuspend(applicationContext.getBean(clazz), this@subscribeFriendMessages)
                                }
                            }
                        }
                        MessageEvent::class -> {
                            eventChannel.subscribeMessages {
                                JobManager.now {
                                    function.callSuspend(applicationContext.getBean(clazz), this@subscribeMessages)
                                }
                            }
                        }
                        UserMessageEvent::class -> {
                            eventChannel.subscribeUserMessages {
                                JobManager.now {
                                    function.callSuspend(applicationContext.getBean(clazz), this@subscribeUserMessages)
                                }
                            }
                        }
                        GroupTempMessageEvent::class -> {
                            eventChannel.subscribeGroupTempMessages {
                                JobManager.now {
                                    function.callSuspend(applicationContext.getBean(clazz), this@subscribeGroupTempMessages)
                                }
                            }
                        }
                        StrangerMessageEvent::class -> {
                            eventChannel.subscribeStrangerMessages {
                                JobManager.now {
                                    function.callSuspend(applicationContext.getBean(clazz), this@subscribeStrangerMessages)
                                }
                            }
                        }
                        OtherClientMessageEvent::class -> {
                            eventChannel.subscribeOtherClientMessages {
                                JobManager.now {
                                    function.callSuspend(applicationContext.getBean(clazz), this@subscribeOtherClientMessages)
                                }
                            }
                        }
                    }
                }
                if (kClass?.jvmName == "me.kuku.mirai.utils.MiraiSubscribe") {
                    val eventClass = type.arguments[0].type?.classifier as? KClass<*> ?: continue
                    val subscribe = MiraiSubscribe::class.java.getDeclaredConstructor().newInstance() as MiraiSubscribe<MessageEvent>
                    runBlocking { function.callSuspend(applicationContext.getBean(clazz), subscribe) }
                    eventChannel.subscribeAlways(eventClass as KClass<out MessageEvent>) {
                        handler.exceptionHandler {
                            subscribe.invoke(this)
                        }
                    }
                }
            }
        }
        runBlocking {
            bot.login()
        }
    }

    @Bean
    fun mirai(): Bot {
        FixProtocolVersion.update()
        val bot = BotFactory.newBot(miraiConfig.qq, miraiConfig.password) {
            fileBasedDeviceInfo()
            protocol = miraiConfig.protocol
        }
        return bot
    }


}

@Component
@ConfigurationProperties(prefix = "kuku.mirai")
class MiraiConfig {
    var qq: Long = 0
    var password: String = ""
    var protocol: BotConfiguration.MiraiProtocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
    var master: Long = 0
}

fun superclasses(kClass: KClass<*>?, set: MutableSet<KClass<*>> = mutableSetOf()): Set<KClass<*>> {
    kClass?.let { set.add(it) }
    val superclasses = kClass?.superclasses ?: return set
    set.addAll(superclasses)
    for (superclass in superclasses) {
        val suSuper = superclass.superclasses
        if (suSuper.isNotEmpty()) {
            suSuper.forEach { superclasses(it, set) }
        }
    }
    return set
}

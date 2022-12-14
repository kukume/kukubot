package me.kuku.mirai.config

import kotlinx.coroutines.runBlocking
import me.kuku.utils.JobManager
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.BotConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmName

@Component
class MiraiBean(
    private val miraiConfig: MiraiConfig,
    private val applicationContext: ApplicationContext
) {

    private fun superclasses(kClass: KClass<*>?, set: MutableSet<KClass<*>> = mutableSetOf()): Set<KClass<*>> {
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

    @Bean
    fun mirai(): Bot {

        val bot = BotFactory.newBot(miraiConfig.qq, miraiConfig.password) {
            fileBasedDeviceInfo()
            protocol = miraiConfig.protocol
        }
        val eventChannel = bot.eventChannel
        val names = applicationContext.beanDefinitionNames
        val clazzList = mutableListOf<Class<*>>()
        for (name in names) {
            applicationContext.getType(name)?.let {
                clazzList.add(it)
            }
        }
        for (clazz in clazzList) {
            val functions = kotlin.runCatching {
                clazz.kotlin.declaredMemberExtensionFunctions
            }.getOrNull() ?: continue
            for (function in functions) {
                val type = function.extensionReceiverParameter?.type
                val kClass = type?.classifier as? KClass<*>
                if (superclasses(kClass).contains(Event::class)) {
                    @Suppress("UNCHECKED_CAST")
                    eventChannel.subscribeAlways(kClass as KClass<out Event>) {
                        function.callSuspend(applicationContext.getBean(clazz), this)
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
            }
        }
        runBlocking {
            bot.login()
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
}
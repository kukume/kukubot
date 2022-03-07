@file:Suppress("UNCHECKED_CAST")

package me.kuku.yuq

import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.di.ClassContext
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.event.EventBus
import com.IceCreamQAQ.Yu.event.events.AppStartEvent
import com.IceCreamQAQ.Yu.event.events.AppStopEvent
import com.IceCreamQAQ.Yu.hook.*
import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.IceCreamQAQ.Yu.module.Module
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.icecreamqaq.yuq.artqq.HookCaptchaUtils
import com.icecreamqaq.yuq.artqq.YuQArtQQStarter
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import me.kuku.utils.MyUtils
import me.kuku.yuq.utils.YuqUtils
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.event.*
import org.artqq.util.CommonResult
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.util.AbilityExtension
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.util.*
import javax.inject.Inject
import javax.persistence.EntityManagerFactory

fun main(args: Array<String>) {
    AppClassloader.registerBackList(listOf())
    YuQArtQQStarter.start(args)
}

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
open class JpaConfig{

    @Bean
    open fun dataSource(): HikariDataSource {
        return HikariDataSource()
    }

    @Bean
    open fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter  = HibernateJpaVendorAdapter()
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.setPackagesToScan("me.kuku.yuq.entity")
        factory.jpaVendorAdapter = vendorAdapter
        val properties = Properties()
        properties.load(Thread.currentThread().contextClassLoader.getResourceAsStream("hibernate.properties"))
        factory.setJpaProperties(properties)
        factory.dataSource = dataSource()
        return factory
    }

    @Bean
    open fun transactionManager(emf: EntityManagerFactory): JpaTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = emf
        return transactionManager
    }

}

private lateinit var transactionManager: JpaTransactionManager

fun <T> transaction(block: () -> T): T {
    val transactionDefinition = DefaultTransactionDefinition()
    val ts = transactionManager.getTransaction(transactionDefinition)
    return try {
        val s = block()
        transactionManager.commit(ts)
        s
    }catch (e: Exception) {
        transactionManager.rollback(ts)
        throw e
    }
}


class JpaModule: Module {

    @Inject
    private lateinit var context: YuContext

    override fun onLoad() {
        YuHook.put(
            HookItem(
                "org.artqq.util.TenCentCaptchaUtils",
                "identifyByUrl",
                "me.kuku.yuq.HookCaptchaUtils"
            )
        )
        val applicationContext = AnnotationConfigApplicationContext(JpaConfig::class.java)
        context.putBean(applicationContext)
        transactionManager = applicationContext.getBean(JpaTransactionManager::class.java)
        val transactionTemplate = applicationContext.getBean(TransactionTemplate::class.java)
        context.putBean(transactionTemplate)
        val classes = MyUtils.getClasses("me.kuku.yuq.entity")
        for ((_, v) in classes) {
            v.interfaces.takeIf { it.contains(JpaRepository::class.java) }
                ?.let {
                    val repository = applicationContext.getBean(v)
                    val name = v.name
                    val classContextMap = context::class.java.declaredFields.first { it.name == "classContextMap" }
                        .also { it.isAccessible = true }.get(context) as MutableMap<String, ClassContext>
                    val classContext =
                        ClassContext(name, v, false, null, repository, mutableMapOf("" to repository), null, null)
                    classContextMap[name] = classContext
                }
        }
    }
}

class TelegramModule @Inject constructor(
    private val context: YuContext,
    @Config("me.kuku.botToken") private val botToken: String,
    @Config("me.kuku.botUsername") private val botUsername: String,
    @Config("me.kuku.creatorId") private val creatorId: String
): Module {

    override fun onLoad() {
        if (botToken.isNotEmpty() && botUsername.isNotEmpty() && creatorId.isNotEmpty()) {
            val tgBot = TgBot(botToken, botUsername, creatorId.toLong())
            context.putBean(tgBot, "tgBot")
        }
    }
}

class TgBot(botToken: String, botUsername: String, private val creatorId: Long): AbilityBot(botToken, botUsername) {

    @Inject
    private lateinit var eventBus: EventBus

    override fun creatorId(): Long = creatorId

    public override fun addExtension(extension: AbilityExtension) {
        super.addExtension(extension)
    }

    override fun onUpdateReceived(update: Update) {
        super.onUpdateReceived(update)
        eventBus.post(TelegramMessageEvent(update))
        val chat = update.message.chat
        if (chat.isChannelChat) {
            eventBus.post(TelegramChannelMessageEvent(update))
        } else if (chat.isGroupChat) {
            eventBus.post(TelegramGroupMessageEvent(update))
        } else if (chat.isSuperGroupChat) {
            eventBus.post(TelegramSuperGroupMessageEvent(update))
        } else if (chat.isUserChat) {
            eventBus.post(TelegramUserMessageEvent(update))
        }
    }
}

@EventListener
class SystemEvent @Inject constructor(
    private val applicationContext: AnnotationConfigApplicationContext,
    private val web: OkHttpWebImpl,
    private val context: YuContext,
) {

    @Inject
    private var tgBot: TgBot? = null

    @Event
    fun close(e: AppStopEvent) {
        applicationContext.close()
    }

    @Event
    fun start(e: AppStartEvent) {
        YuqUtils.web = web
    }

    @Event
    fun telegramInit(e: AppStartEvent) {
        if (tgBot != null) {
            val field = context::class.java.getDeclaredField("classContextMap")
            field.isAccessible = true
            val map = field.get(context) as HashMap<String, ClassContext>
            map.forEach{(_,v) ->
                v.clazz.interfaces.takeIf { it.contains(AbilityExtension::class.java) }?.let {
                    context.getBean(v.clazz)
                    val ob = v.defaultInstance as? AbilityExtension
                    ob?.apply {
                        tgBot?.addExtension(this)
                    }
                }
            }
            context.injectBean(tgBot!!)
            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(tgBot)
        }
    }

}

class HookCaptchaUtils : HookRunnable {
    override fun init(info: HookInfo) {

    }

    private val log = LoggerFactory.getLogger(HookRunnable::class.java)

    override fun preRun(method: HookMethod): Boolean {
        log.info("验证码url: ${method.paras[1]}")


        val url = method.paras[1]!! as String
        val ticket = runBlocking {
            var ticket: String? = null
            for (i in 0..3) {
                log.info("正在尝试第${i + 1}次自动过验证码~~~")
                val jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/tool/captcha", mapOf("url" to url))
                if (jsonObject.getInteger("code") == 200) {
                    ticket = jsonObject.getJSONObject("data").getString("ticket")
                    break
                }
            }
            ticket
        }
        if (ticket == null) {
            log.info("自动识别验证码失败，转为手动验证验证码")
            return HookCaptchaUtils().preRun(method)
        }
        //        RainUI.webListener(url)
        method.result = CommonResult(200, "Success!", mutableMapOf("ticket" to ticket))
        return true
    }

    override fun postRun(method: HookMethod) {
        println("result: ${method.result}")
    }

    override fun onError(method: HookMethod) = false

}
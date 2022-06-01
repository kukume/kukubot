@file:Suppress("UNCHECKED_CAST")

package me.kuku.yuq.config

import com.IceCreamQAQ.Yu.DefaultApp
import com.IceCreamQAQ.Yu.di.ClassContext
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.hook.*
import com.icecreamqaq.yuq.artqq.HookCaptchaUtils
import kotlinx.coroutines.runBlocking
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.utils.SpringUtils
import org.artqq.util.CommonResult
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import java.util.function.Supplier
import javax.annotation.PreDestroy
import javax.inject.Inject

@Component
class ArtInit(
    @Suppress("unused") private val springUtils: SpringUtils,
    private val artConfig: ArtConfig
): ApplicationRunner {

    private var app: DefaultApp? = null

    private val log = LoggerFactory.getLogger(ArtInit::class.java)

    override fun run(args: ApplicationArguments?) {
        System.getProperties()["yuq.art.noUI"] = "${artConfig.qq}|${artConfig.password}|0"
        YuHook.put(
            HookItem(
                "org.artqq.util.TenCentCaptchaUtils",
                "identifyByUrl",
                "me.kuku.yuq.config.HookCaptchaUtils"
            )
        )
        val startTime = System.currentTimeMillis()
        app = DefaultApp()
        app?.start()
        val overTime = System.currentTimeMillis()
        log.info("Done! ${(overTime - startTime).toDouble() / 1000}s.")

        println(" __  __     ____ \n" +
                " \\ \\/ /_ __/ __ \\\n" +
                "  \\  / // / /_/ /\n" +
                "  /_/\\_,_/\\___\\_\\\n")
        println("感谢您使用 YuQ 进行开发，在您使用中如果遇到任何问题，可以到 Github，Gitee 提出 issue，您也可以添加 YuQ 的开发交流群（696129128）进行交流。")
    }

    @PreDestroy
    fun close() {
        app?.stop()
    }

}

class SpringModule: com.IceCreamQAQ.Yu.module.Module {

    @Inject
    private lateinit var yuContext: YuContext

    override fun onLoad() {
        val applicationContext = SpringUtils.applicationContext
        val annotationConfigApplicationContext = applicationContext as AnnotationConfigApplicationContext
        annotationConfigApplicationContext.registerBean("context", YuContext::class.java, Supplier { yuContext })
        val names = applicationContext.beanDefinitionNames
        val clazzList = mutableListOf<Class<*>>()
        for (name in names) {
            val clazzTemp = applicationContext.getType(name)
            val ss = clazzTemp?.superclass
            val list = listOf(clazzTemp, ss)
            for (clazz in list) {
//                if (clazz?.isAnnotationPresent(GroupController::class.java) == true || clazz?.isAnnotationPresent(PrivateController::class.java) == true ||
//                    clazz?.isAnnotationPresent(EventListener::class.java) == true || clazz?.isAnnotationPresent(JobCenter::class.java) == true) {
//                    clazzList.add(clazz)
//                }
                if (clazz?.isAnnotationPresent(Component::class.java) == true || clazz?.isAnnotationPresent(Controller::class.java) == true ||
                    clazz?.isAnnotationPresent(Service::class.java) == true) {
                    clazzList.add(clazz)
                }
            }
        }
        val classContextMap = yuContext::class.java.declaredFields.first { it.name == "classContextMap" }
            .also { it.isAccessible = true }.get(yuContext) as MutableMap<String, ClassContext>
        for (clazz in clazzList) {
            val bean = applicationContext.getBean(clazz)
            val name = clazz.name
            val beanClazz = bean::class.java
            yuContext.injectBean(bean)
            val classContext =
                ClassContext(name, beanClazz, false, null, bean, mutableMapOf("" to bean), null, null)
            classContextMap[name] = classContext
        }
    }
}

@ConfigurationProperties(prefix = "yuq.art")
@Component
class ArtConfig {
    var qq: Long = 0
    var password: String = ""
    var master: Long = 0
}

@ConfigurationProperties(prefix = "yuq.art.saucenao")
@Component
class SauceNaoConfig {
    var key: String = ""
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
                val jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/captcha", mapOf("url" to url))
                if (!jsonObject.containsKey("code")) {
                    ticket = jsonObject.getString("ticket")
                    log.info("自动过验证码成功")
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

class VerificationFailureException(override val message: String): RuntimeException(message)
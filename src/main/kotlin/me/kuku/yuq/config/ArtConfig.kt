@file:Suppress("UNCHECKED_CAST")

package me.kuku.yuq.config

import com.IceCreamQAQ.Yu.DefaultApp
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.IceCreamQAQ.Yu.di.ClassContext
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.hook.*
import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.artqq.HookCaptchaUtils
import com.icecreamqaq.yuq.artqq.YuQArtQQModule
import com.icecreamqaq.yuq.artqq.YuQInternalFunArtQQImpl
import com.icecreamqaq.yuq.util.YuQInternalFun
import kotlinx.coroutines.runBlocking
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.utils.SpringUtils
import org.artqq.util.CommonResult
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.stereotype.Component
import java.util.function.Supplier
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject

@Component
class ArtInit(
    @Suppress("unused") private val springUtils: SpringUtils,
    private val artConfig: ArtConfig
) {

    private var app: DefaultApp? = null

    @PostConstruct
    fun defaultApp() {
        System.getProperties()["yuq.art.noUI"] = "${artConfig.qq}|${artConfig.password}|0"
        YuHook.put(
            HookItem(
                "com.icecreamqaq.yuq.artqq.YuQArtQQModule",
                "onLoad",
                "me.kuku.yuq.config.HookYuQArtQQModule"
            )
        )
        app = DefaultApp()
        app?.start()
    }

    @PreDestroy
    fun close() {
        app?.stop()
    }

}

class SpringModule: com.IceCreamQAQ.Yu.module.Module {

    @Inject
    private lateinit var context: YuContext

    override fun onLoad() {
        val applicationContext = SpringUtils.applicationContext
        val annotationConfigApplicationContext = applicationContext as AnnotationConfigApplicationContext
        annotationConfigApplicationContext.registerBean("context", YuContext::class.java, Supplier { context })
        val names = applicationContext.beanDefinitionNames
        val clazzList = mutableListOf<Class<*>>()
        for (name in names) {
            val clazz = applicationContext.getType(name)
            if (clazz?.isAnnotationPresent(GroupController::class.java) == true || clazz?.isAnnotationPresent(PrivateController::class.java) == true ||
                    clazz?.isAnnotationPresent(EventListener::class.java) == true || clazz?.isAnnotationPresent(JobCenter::class.java) == true) {
                clazzList.add(clazz)
            }
        }
        val classContextMap = context::class.java.declaredFields.first { it.name == "classContextMap" }
            .also { it.isAccessible = true }.get(context) as MutableMap<String, ClassContext>
        for (clazz in clazzList) {
            val bean = applicationContext.getBean(clazz)
            val name = clazz.name
            val beanClazz = bean::class.java
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

class HookYuQArtQQModule: HookRunnable {

    override fun preRun(method: HookMethod): Boolean {
        val yuQArtQQModule = method.paras[0] as YuQArtQQModule
        val context = yuQArtQQModule::class.java.declaredFields[0].also { it.isAccessible = true }
            .get(yuQArtQQModule) as YuContext
        context.putBean(YuQInternalFun::class.java, "", YuQInternalFunArtQQImpl())
        AppClassloader.registerBackList(
            arrayListOf(
                "javafx.",
                "org.w3c",
                "jdk.internal.",
            )
        )
        YuHook.put(
            HookItem(
                "org.artqq.util.TenCentCaptchaUtils",
                "identifyByUrl",
                "me.kuku.yuq.config.HookCaptchaUtils"
            )
        )
        YuHook.put(
            HookItem(
                "org.artqq.Wtlogin._Login",
                "onSuccessSendVC",
                "com.icecreamqaq.yuq.artqq.HookPhoneCap"
            )
        )
        YuHook.put(
            HookItem(
                "com.baidu.bjf.remoting.protobuf.utils.JDKCompilerHelper",
                "getJdkCompiler",
                "com.icecreamqaq.yuq.artqq.HookProto"
            )
        )
        return true
    }
}

class VerificationFailureException(override val message: String): RuntimeException(message)
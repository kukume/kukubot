@file:Suppress("unused", "UNUSED_PARAMETER")

package me.kuku.yuq

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.event.events.AppStartEvent
import com.IceCreamQAQ.Yu.hook.*
import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.icecreamqaq.yuq.artqq.HookCaptchaUtils
import com.icecreamqaq.yuq.artqq.YuQArtQQModule
import com.icecreamqaq.yuq.artqq.YuQArtQQStarter
import com.icecreamqaq.yuq.artqq.YuQInternalFunArtQQImpl
import com.icecreamqaq.yuq.util.YuQInternalFun
import kotlinx.coroutines.runBlocking
import me.kuku.yuq.utils.YuqUtils
import me.kuku.utils.OkHttpUtils
import org.artqq.util.CommonResult
import org.slf4j.LoggerFactory
import javax.inject.Inject

fun main(args: Array<String>) {
    YuHook.put(
        HookItem(
            "com.icecreamqaq.yuq.artqq.YuQArtQQModule",
            "onLoad",
            "me.kuku.yuq.HookYuQArtQQModule"
        )
    )
    val newArgs = if (args.contains("-noUI")) args
    else args.plus("-noUI")
    YuQArtQQStarter.start(newArgs)
}

@EventListener
class SystemEvent @Inject constructor(
    private val web: OkHttpWebImpl
) {

    @Event
    fun start(e: AppStartEvent) {
        YuqUtils.web = web
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
                "me.kuku.yuq.HookCaptchaUtils"
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
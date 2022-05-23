@file:Suppress("DuplicatedCode")

package me.kuku.yuq.config

import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.ExceptionLogEntity
import me.kuku.yuq.entity.ExceptionLogService
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component

@Component
@Aspect
class CronAspect(
    private val exceptionLogService: ExceptionLogService
) {

    @Pointcut("@annotation(com.IceCreamQAQ.Yu.annotation.Cron)")
    fun cronPoint() {}


    @AfterThrowing(pointcut = "cronPoint()", throwing = "e")
    fun catch(joinPoint: JoinPoint, e: Throwable) {
        val st = e.stackTraceToString()
        val url = kotlin.runCatching {
            val jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/paste",
                mapOf("poster" to "kuku", "syntax" to "java", "content" to st)
            )
            jsonObject.getJSONObject("data").getString("url")
        }.getOrDefault("Ubuntu paste url 生成失败")
        val entity = ExceptionLogEntity().also {
            it.stackTrace = st
            it.url = url
        }
        exceptionLogService.save(entity)
    }

}
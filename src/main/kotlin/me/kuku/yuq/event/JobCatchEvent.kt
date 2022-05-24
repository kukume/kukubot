@file:Suppress("DuplicatedCode")

package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.event.events.JobRunExceptionEvent
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.ExceptionLogEntity
import me.kuku.yuq.entity.ExceptionLogService
import org.springframework.stereotype.Component

@Component
@EventListener
class JobCatchEvent(
    private val exceptionLogService: ExceptionLogService
) {

    @Event
    fun jobCatch(e: JobRunExceptionEvent) {
        val st = e.throwable.stackTraceToString()
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
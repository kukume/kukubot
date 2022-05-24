package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.event.events.JobRunExceptionEvent
import me.kuku.yuq.entity.ExceptionLogService
import org.springframework.stereotype.Component

@Component
@EventListener
class JobCatchEvent(
    private val exceptionLogService: ExceptionLogService
) {

    @Event
    fun jobCatch(e: JobRunExceptionEvent) {

    }


}
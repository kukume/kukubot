@file:Suppress("DuplicatedCode")

package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.event.events.EventListenerRunExceptionEvent
import com.IceCreamQAQ.Yu.event.events.JobRunExceptionEvent
import me.kuku.utils.JobManager
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.ExceptionLogEntity
import me.kuku.yuq.entity.ExceptionLogService
import me.kuku.yuq.entity.save
import org.springframework.stereotype.Component

@Component
@EventListener
class JobCatchEvent {

    @Event
    fun jobCatch(e: JobRunExceptionEvent) {
        JobManager.now {
            e.throwable.save()
        }
    }

    @Event
    fun eventCatch(e: EventListenerRunExceptionEvent) {
        JobManager.now {
            e.throwable.save()
        }
    }


}
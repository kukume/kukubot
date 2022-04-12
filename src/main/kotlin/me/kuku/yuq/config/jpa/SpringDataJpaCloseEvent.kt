@file:Suppress("UNUSED_PARAMETER")

package me.kuku.yuq.config.jpa

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.event.events.AppStopEvent
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import javax.inject.Inject

@EventListener
class SpringDataJpaCloseEvent @Inject constructor(
    private val applicationContext: AnnotationConfigApplicationContext
) {

    @Event
    fun close(e: AppStopEvent) {
        applicationContext.close()
    }

}
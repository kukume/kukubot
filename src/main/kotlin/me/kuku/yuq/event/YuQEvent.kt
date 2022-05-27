package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.di.inject
import com.IceCreamQAQ.Yu.event.events.AppStartEvent
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import me.kuku.yuq.utils.YuqUtils
import org.springframework.stereotype.Component

@Component
@EventListener
class YuQEvent {

    private val yuContext: YuContext by inject()


    @Event
    fun start(e: AppStartEvent) {
        YuqUtils.web = yuContext.getBean(OkHttpWebImpl::class.java)
    }


}
@file:Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")

package me.kuku.yuq.config.telegram

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.di.ClassContext
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.event.events.AppStartEvent
import org.telegram.abilitybots.api.util.AbilityExtension
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import java.util.HashMap
import javax.inject.Inject

@EventListener
class TelegramStartEvent @Inject constructor(
    private val context: YuContext
) {

    @Inject
    private var tgBot: TgBot? = null

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
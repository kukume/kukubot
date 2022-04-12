package me.kuku.yuq.config.telegram

import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.event.EventBus
import com.IceCreamQAQ.Yu.module.Module
import me.kuku.yuq.event.*
import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.util.AbilityExtension
import org.telegram.telegrambots.meta.api.objects.Update
import javax.inject.Inject

class TelegramModule @Inject constructor(
    private val context: YuContext,
    @Config("me.kuku.botToken") private val botToken: String,
    @Config("me.kuku.botUsername") private val botUsername: String,
    @Config("me.kuku.creatorId") private val creatorId: String
): Module {

    override fun onLoad() {
        if (botToken.isNotEmpty() && botUsername.isNotEmpty() && creatorId.isNotEmpty()) {
            val tgBot = TgBot(botToken, botUsername, creatorId.toLong())
            context.putBean(tgBot, "tgBot")
        }
    }
}

class TgBot(botToken: String, botUsername: String, private val creatorId: Long): AbilityBot(botToken, botUsername) {

    @Inject
    private lateinit var eventBus: EventBus

    override fun creatorId(): Long = creatorId

    public override fun addExtension(extension: AbilityExtension) {
        super.addExtension(extension)
    }

    override fun onUpdateReceived(update: Update) {
        super.onUpdateReceived(update)
        eventBus.post(TelegramMessageEvent(update))
        val chat = update.message.chat
        if (chat.isChannelChat) {
            eventBus.post(TelegramChannelMessageEvent(update))
        } else if (chat.isGroupChat) {
            eventBus.post(TelegramGroupMessageEvent(update))
        } else if (chat.isSuperGroupChat) {
            eventBus.post(TelegramSuperGroupMessageEvent(update))
        } else if (chat.isUserChat) {
            eventBus.post(TelegramUserMessageEvent(update))
        }
    }
}
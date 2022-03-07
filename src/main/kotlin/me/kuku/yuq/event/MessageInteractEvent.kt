package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.di.YuContext
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Text
import com.icecreamqaq.yuq.message.Voice
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.TgBot
import me.kuku.yuq.entity.MessageInteractEntity
import me.kuku.yuq.entity.MessageInteractService
import me.kuku.yuq.entity.MessageService
import me.kuku.yuq.transaction
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendVoice
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.PhotoSize
import javax.inject.Inject

@EventListener
class MessageInteractEvent {

    @Inject
    private lateinit var tgBot: TgBot
    @Inject
    private lateinit var context: YuContext
    @Inject
    private lateinit var messageInteractService: MessageInteractService
    @Inject
    private lateinit var messageService: MessageService

    @Event(weight = Event.Weight.lowest)
    fun qqMessage(e: GroupMessageEvent) {
        if (context.getBean(TgBot::class.java) == null) return
        val message = e.message
        val group = e.group
        val member = e.sender
        val silent = tgBot.silent()
        val creatorId = tgBot.creatorId()
        val messageEntity = messageService.findByMessageIdAndGroup(message.source.id, group.id)
        silent.sendMd("来自【${group.name}(${group.id})】的【${member.nameCardOrName()}(${member.id})】的消息", creatorId)
        for (messageItem in message.body) {
            val messageId = when (messageItem) {
                is Text -> {
                    val send = silent.sendMd(messageItem.text, creatorId)
                    send.orElse(null)?.messageId
                }
                is Image -> {
                    val execute = tgBot.execute(
                        SendPhoto(
                            creatorId.toString(),
                            InputFile(OkHttpUtils.getByteStream(messageItem.url), messageItem.id)
                        )
                    )
                    execute.messageId
                }
                is Voice -> {
                    val execute = tgBot.execute(
                        SendVoice(
                            creatorId.toString(),
                            InputFile(OkHttpUtils.getByteStream(messageItem.url), messageItem.id)
                        )
                    )
                    execute.messageId
                }
                else -> null
            }
            if (messageId != null && messageEntity != null) {
                val messageInteractEntity = MessageInteractEntity()
                messageInteractEntity.telegramMessageId = messageId
                messageInteractEntity.messageEntity = messageEntity
                messageInteractService.save(messageInteractEntity)
            }
        }
    }

    @Event
    fun tgMessage(e: TelegramUserMessageEvent) = transaction {
        val update = e.update
        if (update.message.isReply) {
            val replyToMessage = update.message.replyToMessage
            val messageId = replyToMessage.messageId
            val messageInteractEntity = messageInteractService.findByTelegramMessageId(messageId) ?: return@transaction
            val messageEntity = messageInteractEntity.messageEntity ?: return@transaction
            val groupEntity = messageEntity.groupEntity
            val groupNum = groupEntity.group
            val group = yuq.groups[groupNum]
            if (update.message.hasText()) {
                val text = update.message.text
                group?.sendMessage(text)
            }
            val photo = update.message.photo?.stream()
                ?.max(Comparator.comparing(PhotoSize::getFileSize))?.orElse(null)
            if (photo != null) {
                val filePath = if (photo.filePath != null) photo.filePath
                else {
                    val getFile = GetFile()
                    getFile.fileId = photo.fileId
                    val file = tgBot.execute(getFile)
                    file.filePath
                }
                val ff = tgBot.downloadFile(filePath)
                group?.sendMessage(mif.imageByFile(ff))
            }
        }
    }

}
package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.MessageEntity

@AutoBind
interface MessageService {
    fun findByMessageId(messageId: Int): MessageEntity?
    fun save(messageEntity: MessageEntity)
    fun findMaxMessageIdByGroup(group: Long): Int?
    fun findCountQQByGroupAndToday(group: Long): Map<Long, Long>
}
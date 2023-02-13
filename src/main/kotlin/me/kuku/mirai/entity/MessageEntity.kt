package me.kuku.mirai.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Component

@Document("message")
class MessageEntity: BaseEntity() {
    @Id
    var id: String? = null
    var messageId: Int = 0
    var group: Long = 0
    var qq: Long = 0
    var value: String = ""
}


@Suppress("SpringDataRepositoryMethodReturnTypeInspection")
interface MessageRepository: CoroutineCrudRepository<MessageEntity, String> {

    suspend fun findByQq(qq: Long): List<MessageEntity>

    suspend fun findByGroupAndMessageId(group: Long, messageId: Int): MessageEntity?

    suspend fun findByGroup(group: Long): List<MessageEntity>

    suspend fun findByGroupAndQq(group: Long, qq: Long): List<MessageEntity>

}

@Component
class MessageService(
    private val messageRepository: MessageRepository
) {

    suspend fun save(messageEntity: MessageEntity) = messageRepository.save(messageEntity)

    suspend fun findByQq(qq: Long) = messageRepository.findByQq(qq)

    suspend fun findByGroup(group: Long) = messageRepository.findByGroup(group)

    suspend fun findByGroupAndQq(group: Long, qq: Long) = messageRepository.findByGroupAndQq(group, qq)

    suspend fun findByGroupAndMessageId(group: Long, messageId: Int) = messageRepository.findByGroupAndMessageId(group, messageId)

}

package me.kuku.mirai.entity

import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Service

@Document("recall_message")
class RecallMessageEntity: BaseEntity() {
    var id: String? = null
    @DBRef
    var message: MessageEntity = MessageEntity()
}

@Suppress("SpringDataRepositoryMethodReturnTypeInspection", "FunctionName")
interface RecallMessageRepository: CoroutineCrudRepository<RecallMessageEntity, String> {

    suspend fun findByMessage_Qq(qq: Long): List<RecallMessageEntity>

    suspend fun findByMessage_Group(group: Long): List<RecallMessageEntity>

    suspend fun findByMessage_GroupAndMessage_Qq(group: Long, qq: Long): List<RecallMessageEntity>

}

@Service
class RecallMessageService(
    private val recallMessageRepository: RecallMessageRepository
) {

    suspend fun save(recallMessageEntity: RecallMessageEntity) = recallMessageRepository.save(recallMessageEntity)

    suspend fun findByQq(qq: Long) = recallMessageRepository.findByMessage_Qq(qq)

    suspend fun findByGroup(group: Long) = recallMessageRepository.findByMessage_Group(group)

    suspend fun findByGroupAndQq(group: Long, qq: Long) = recallMessageRepository.findByMessage_GroupAndMessage_Qq(group, qq)

}

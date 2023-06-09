package me.kuku.mirai.entity

import kotlinx.coroutines.flow.toList
import me.kuku.mirai.logic.QqGroupEssenceMessage
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Service

@Document("essence")
@CompoundIndexes(
    CompoundIndex(name = "chatId_messageThreadId_group_idx", def = "{'chatId': 1, 'messageThreadId': 1, 'group': 1}", unique = true)
)
class EssenceEntity {
    @Id
    var id: String? = null
    var chatId: Long = 0
    var messageThreadId: Int? = null
    var group: Long = 0
    var messages: MutableList<QqGroupEssenceMessage> = mutableListOf()
}


interface EssenceRepository: CoroutineCrudRepository<EssenceEntity, String> {

    suspend fun findByChatIdAndMessageThreadIdAndGroup(chatId: Long, messageThreadId: Int?, group: Long): EssenceEntity?

    suspend fun findByChatIdAndMessageThreadId(chatId: Long, messageThreadId: Int?): List<EssenceEntity>

    suspend fun deleteByChatIdAndMessageThreadIdAndGroup(chatId: Long, messageThreadId: Int?, group: Long)

}

@Service
class EssenceService(
    private val essenceRepository: EssenceRepository
) {

    suspend fun findByChatIdAndMessageThreadIdAndGroup(chatId: Long, messageThreadId: Int?, group: Long) =
        essenceRepository.findByChatIdAndMessageThreadIdAndGroup(chatId, messageThreadId, group)

    suspend fun save(entity: EssenceEntity) = essenceRepository.save(entity)

    suspend fun findByChatIdAndMessageThreadId(chatId: Long, messageThreadId: Int?) =
        essenceRepository.findByChatIdAndMessageThreadId(chatId, messageThreadId)

    suspend fun findById(id: String) = essenceRepository.findById(id)

    suspend fun deleteById(id: String) = essenceRepository.deleteById(id)

    suspend fun findAll() = essenceRepository.findAll().toList()

    suspend fun deleteByChatIdAndMessageThreadIdAndGroup(chatId: Long, messageThreadId: Int?, group: Long) =
        essenceRepository.deleteByChatIdAndMessageThreadIdAndGroup(chatId, messageThreadId, group)

}

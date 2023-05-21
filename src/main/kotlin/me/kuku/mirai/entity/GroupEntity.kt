package me.kuku.mirai.entity

import kotlinx.coroutines.flow.toList
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Service

@Document("group")
class GroupEntity {
    @Id
    var id: String? = null
    @Indexed(unique = true)
    var group: Long = 0
    var qa: MutableList<Qa> = mutableListOf()
}

@Suppress("SpringDataRepositoryMethodReturnTypeInspection")
interface GroupRepository: CoroutineCrudRepository<GroupEntity, String> {

    suspend fun findByGroup(group: Long): GroupEntity?

}

@Service
class GroupService(
    private val groupRepository: GroupRepository
) {

    suspend fun findByGroup(group: Long) = groupRepository.findByGroup(group)

    suspend fun save(groupEntity: GroupEntity) = groupRepository.save(groupEntity)

    suspend fun findAll() = groupRepository.findAll().toList()

}

class Qa {
    var q: String = ""
    var a: String = ""
    var type: Type = Type.Eq

    enum class Type {
        Eq, Like, StartsWith, EndsWith
    }
}

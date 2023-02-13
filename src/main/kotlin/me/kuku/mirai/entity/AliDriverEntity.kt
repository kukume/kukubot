package me.kuku.mirai.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Service

@Document("ali_driver")
class AliDriverEntity {
    @Id
    var id: String? = null
    var qq: Long = 0
    var refreshToken: String = ""
    var sign: Status = Status.OFF
}

@Suppress("SpringDataRepositoryMethodReturnTypeInspection")
interface AliDriverRepository: CoroutineCrudRepository<AliDriverEntity, String> {

    suspend fun findByQq(qq: Long): AliDriverEntity?

    suspend fun findBySign(sign: Status): List<AliDriverEntity>

}

@Service
class AliDriverService(
    private val aliDriverRepository: AliDriverRepository
) {

    suspend fun findByQq(qq: Long) = aliDriverRepository.findByQq(qq)

    suspend fun findBySign(sign: Status) = aliDriverRepository.findBySign(sign)

    suspend fun save(aliDriverEntity: AliDriverEntity): AliDriverEntity = aliDriverRepository.save(aliDriverEntity)

    suspend fun delete(aliDriverEntity: AliDriverEntity) = aliDriverRepository.delete(aliDriverEntity)
}

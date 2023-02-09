package me.kuku.mirai.entity

import kotlinx.coroutines.flow.toList
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Document("ku_gou")
class KuGouEntity {
    @Id
    var id: String? = null
    var qq: Long = 0
    var token: String = ""
    var userid: Long = 0
    var kuGoo: String = ""
    var mid: String = ""
    var sign: Status = Status.OFF
}

@Suppress("SpringDataRepositoryMethodReturnTypeInspection")
interface KuGouRepository: CoroutineCrudRepository<KuGouEntity, String> {

    suspend fun findByQq(qq: Long): KuGouEntity?

    suspend fun findBySign(sign: Status): List<KuGouEntity>

    suspend fun deleteByQq(qq: Long)

}

@Service
class KuGouService(
    private val kuGouRepository: KuGouRepository
) {

    suspend fun findByQq(qq: Long) = kuGouRepository.findByQq(qq)

    suspend fun findBySign(sign: Status): List<KuGouEntity> = kuGouRepository.findBySign(sign).toList()

    suspend fun save(kuGouEntity: KuGouEntity): KuGouEntity = kuGouRepository.save(kuGouEntity)

    suspend fun findAll(): List<KuGouEntity> = kuGouRepository.findAll().toList()

    @Transactional
    suspend fun deleteByQq(qq: Long) = kuGouRepository.deleteByQq(qq)
}

package me.kuku.mirai.entity

import kotlinx.coroutines.flow.toList
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Document("net_ease")
class NetEaseEntity {
    @Id
    var id: String? = null
    var qq: Long = 0
    var musicU: String = ""
    var csrf: String = ""
    var sign: Status = Status.OFF
    var musicianSign: Status = Status.OFF

    fun cookie() = "os=pc; osver=Microsoft-Windows-10-Professional-build-10586-64bit; appver=2.0.3.131777; channel=netease; __remember_me=true; MUSIC_U=$musicU; __csrf=$csrf; "
}

@Suppress("SpringDataRepositoryMethodReturnTypeInspection")
interface NetEaseRepository: CoroutineCrudRepository<NetEaseEntity, String> {

    suspend fun findByQq(qq: Long): NetEaseEntity?

    suspend fun findBySign(sign: Status): List<NetEaseEntity>

    suspend fun findByMusicianSign(musicianSign: Status): List<NetEaseEntity>

    suspend fun deleteByQq(qq: Long)

}

@Service
class NetEaseService(
    private val netEaseRepository: NetEaseRepository
) {

    suspend fun findByQq(qq: Long) = netEaseRepository.findByQq(qq)

    suspend fun findBySign(sign: Status): List<NetEaseEntity> = netEaseRepository.findBySign(sign)

    suspend fun findByMusicianSign(musicianSign: Status): List<NetEaseEntity> = netEaseRepository.findByMusicianSign(musicianSign)

    suspend fun save(netEaseEntity: NetEaseEntity): NetEaseEntity = netEaseRepository.save(netEaseEntity)

    suspend fun findAll(netEaseEntity: NetEaseEntity): List<NetEaseEntity> = netEaseRepository.findAll().toList()

    @Transactional
    suspend fun deleteByQq(qq: Long) = netEaseRepository.deleteByQq(qq)

}

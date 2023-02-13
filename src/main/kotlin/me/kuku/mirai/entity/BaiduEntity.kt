package me.kuku.mirai.entity

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import me.kuku.utils.OkUtils
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Document("baidu")
class BaiduEntity {
    @Id
    var id: String? = null
    var qq: Long = 0
    var cookie: String = ""
    var tieBaSToken: String = ""
    var sign: Status = Status.OFF

    fun otherCookie(sToken: String): String {
        return OkUtils.cookieStr(cookie, "BDUSS") + "STOKEN=$sToken; "
    }

    fun teiBaCookie(): String {
        return otherCookie(tieBaSToken)
    }
}

interface BaiduRepository: ReactiveMongoRepository<BaiduEntity, String> {
    fun findByQq(qq: Long): Mono<BaiduEntity>
    fun findBySign(sign: Status): Flux<BaiduEntity>

    fun deleteByQq(qq: Long): Mono<Void>
}

@Service
class BaiduService(
    private val baiduRepository: BaiduRepository
) {

    suspend fun findByQq(qq: Long) = baiduRepository.findByQq(qq).awaitSingleOrNull()

    suspend fun save(baiduEntity: BaiduEntity) = baiduRepository.save(baiduEntity).awaitSingle()!!

    suspend fun findBySign(sign: Status): List<BaiduEntity> = baiduRepository.findBySign(sign).collectList().awaitSingle()

    suspend fun findAll(): List<BaiduEntity> = baiduRepository.findAll().collectList().awaitSingle()

    @Transactional
    suspend fun deleteByQq(qq: Long) = baiduRepository.deleteByQq(qq).awaitSingleOrNull()
}

package me.kuku.yuq.entity

import com.alibaba.fastjson.annotation.JSONField
import me.kuku.utils.OkHttpKtUtils
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.utils.SpringUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "exception_log")
@NamedEntityGraph(name = "ff", attributeNodes = [
    NamedAttributeNode(value = "messageEntity", subgraph = "messageEntity"),
    NamedAttributeNode(value = "privateMessageEntity", subgraph = "privateMessageEntity")
], subgraphs = [
    NamedSubgraph(name = "messageEntity", attributeNodes = [NamedAttributeNode("qqEntity"), NamedAttributeNode("groupEntity")]),
    NamedSubgraph(name = "privateMessageEntity", attributeNodes = [NamedAttributeNode("qqEntity")]),
])
class ExceptionLogEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "message_id")
    var messageEntity: MessageEntity? = null
    @OneToOne
    @JoinColumn(name = "private_message_id")
    var privateMessageEntity: PrivateMessageEntity? = null
    @Column(columnDefinition = "text")
    @JSONField(serialize = false)
    var stackTrace: String = ""
    var url: String = ""
}

interface ExceptionLogRepository: JpaRepository<ExceptionLogEntity, Int> {

    @EntityGraph(value = "ff", type = EntityGraph.EntityGraphType.FETCH)
    override fun findAll(pageable: Pageable): Page<ExceptionLogEntity>

}


@Service
class ExceptionLogService (
    private val exceptionLogRepository: ExceptionLogRepository
) {

    fun save(exceptionLogEntity: ExceptionLogEntity): ExceptionLogEntity = exceptionLogRepository.save(exceptionLogEntity)

    fun findAll(pageable: Pageable) = exceptionLogRepository.findAll(pageable)

}

suspend fun Throwable.save(url: String? = null) {
    val exceptionLogService = SpringUtils.getBean<ExceptionLogService>()
    exceptionLogService.save(ExceptionLogEntity().also {
        it.stackTrace = this.stackTraceToString()
        it.url = url ?: this.toUrl()
    })
}
suspend fun Throwable.toUrl(): String {
    return kotlin.runCatching {
        val jsonObject = OkHttpKtUtils.postJson("https://api.kukuqaq.com/paste",
            mapOf("poster" to "kuku", "syntax" to "java", "content" to this.stackTraceToString())
        )
        jsonObject.getJSONObject("data").getString("url")
    }.getOrDefault("Ubuntu paste url 生成失败")
}
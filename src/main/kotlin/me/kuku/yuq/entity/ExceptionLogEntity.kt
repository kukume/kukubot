package me.kuku.yuq.entity

import com.alibaba.fastjson.annotation.JSONField
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
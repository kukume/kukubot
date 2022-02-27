package me.kuku.yuq.entity

import com.alibaba.fastjson.annotation.JSONField
import com.querydsl.core.BooleanBuilder
import me.kuku.yuq.utils.plus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import java.time.LocalDateTime
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "recall")
class RecallEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "message_id")
    var messageEntity: MessageEntity = MessageEntity()
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    var localDateTime: LocalDateTime = LocalDateTime.now()
}


interface RecallRepository: JpaRepository<RecallEntity, Int>, QuerydslPredicateExecutor<RecallEntity> {

}


class RecallService @Inject constructor(
    private val recallRepository: RecallRepository
) {

    fun save(recallEntity: RecallEntity): RecallEntity = recallRepository.save(recallEntity)

    fun findByMessageId(messageId: Int): List<RecallEntity> {
        val q = QRecallEntity.recallEntity
        return recallRepository.findAll(q.messageEntity.messageId.eq(messageId)).toList()
    }

    fun findByGroupAndQq(group: Long, qq: Long): List<RecallEntity> {
        with(QRecallEntity.recallEntity.messageEntity) {
            return recallRepository.findAll(qqEntity.qq.eq(qq) + groupEntity.group.eq(group), id.desc()).toList()
        }
    }

    fun findByAll(group: Long?, messageId: Int?, content: String?, qq: Long?, pageable: Pageable): Page<RecallEntity> {
        with(QRecallEntity.recallEntity.messageEntity) {
            val bb = BooleanBuilder()
            group?.let { bb + groupEntity.group.eq(it) }
            messageId?.let { bb + this.messageId.eq(it) }
            content?.let { bb + this.content.like("%$it%") }
            qq?.let { bb + qqEntity.qq.eq(qq) }
            return recallRepository.findAll(bb, pageable)
        }
    }
}
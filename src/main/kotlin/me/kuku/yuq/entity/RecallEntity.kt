package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import java.util.*
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
    var date = Date()
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
}
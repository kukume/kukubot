package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "private_message")
class PrivateMessageEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var messageId: Int = 0
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(columnDefinition = "text")
    var content: String = ""
    var type: PrivateMessageType = PrivateMessageType.RECEIVE
}

interface PrivateMessageRepository: JpaRepository<PrivateMessageEntity, Int>, QuerydslPredicateExecutor<PrivateMessageEntity> {

}

@Service
class PrivateMessageService (
    private val privateMessageRepository: PrivateMessageRepository
) {

    fun save(privateMessageEntity: PrivateMessageEntity): PrivateMessageEntity = privateMessageRepository.save(privateMessageEntity)

    fun findByMessageIdAndQq(messageId: Int, qq: Long): PrivateMessageEntity? {
        val q = QPrivateMessageEntity.privateMessageEntity
        val bb = q.messageId.eq(messageId).and(q.qqEntity.qq.eq(qq))
        return privateMessageRepository.findOne(bb).orElse(null)
    }

}

enum class PrivateMessageType {
    SEND, RECEIVE;
}
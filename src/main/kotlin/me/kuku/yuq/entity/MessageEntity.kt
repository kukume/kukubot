package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import java.time.LocalDateTime
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "message")
class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var messageId: Int = 0
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity = QqEntity()
    @OneToOne
    @JoinColumn(name = "group_id")
    var groupEntity: GroupEntity = GroupEntity()
    @Column(length = 5000)
    var content: String = ""
    var localDateTime: LocalDateTime = LocalDateTime.now()
}

interface MessageRepository: JpaRepository<MessageEntity, Int>, QuerydslPredicateExecutor<MessageEntity> {
    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity): MessageEntity?
}

class MessageService @Inject constructor(
    private val messageRepository: MessageRepository
) {

    fun save(messageEntity: MessageEntity): MessageEntity = messageRepository.save(messageEntity)

    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity) =
        messageRepository.findByMessageIdAndGroupEntity(messageId, groupEntity)

    fun findByMessageIdAndGroup(messageId: Int, group: Long): MessageEntity? {
        val q = QMessageEntity.messageEntity
        return messageRepository.findOne(q.messageId.eq(messageId).and(q.groupEntity.group.eq(group))).orElse(null)
    }

    fun findByGroupAndQqOrderByIdDesc(group: Long, qq: Long): List<MessageEntity> {
        with(QMessageEntity.messageEntity) {
            return messageRepository.findAll(groupEntity.group.eq(group).and(qqEntity.qq.eq(qq)), id.desc()).toList()
        }
    }

}
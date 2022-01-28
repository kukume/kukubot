package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
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
    var date: Date = Date()
}

interface MessageRepository: JpaRepository<MessageEntity, Int> {
    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity): MessageEntity?
}

class MessageService @Inject constructor(
    private val messageRepository: MessageRepository
) {

    fun save(messageEntity: MessageEntity): MessageEntity = messageRepository.save(messageEntity)

    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity) =
        messageRepository.findByMessageIdAndGroupEntity(messageId, groupEntity)

}
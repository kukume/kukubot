package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "message_interact")
class MessageInteractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var telegramMessageId: Int = 0
    @OneToOne
    @JoinColumn(name = "message_id")
    var messageEntity: MessageEntity? = null
}

interface MessageInteractRepository: JpaRepository<MessageInteractEntity, Int>, QuerydslPredicateExecutor<MessageInteractEntity> {
    fun findByTelegramMessageId(telegramMessageId: Int): MessageInteractEntity?
}

class MessageInteractService @Inject constructor(
    private val messageInteractRepository: MessageInteractRepository
) {
    fun save(messageInteractEntity: MessageInteractEntity): MessageInteractEntity =
        messageInteractRepository.save(messageInteractEntity)

    fun findByTelegramMessageId(telegramMessageId: Int) =
        messageInteractRepository.findByTelegramMessageId(telegramMessageId)
}
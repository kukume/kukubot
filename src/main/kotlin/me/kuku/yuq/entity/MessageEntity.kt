package me.kuku.yuq.entity

import com.icecreamqaq.yuq.artqq.message.ArtGroupMessageSource
import com.querydsl.core.BooleanBuilder
import me.kuku.yuq.utils.plus
import org.hibernate.annotations.Type
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "message")
class MessageEntity: BaseEntity() {
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
    @Lob
    @Column(columnDefinition = "text")
    var content: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var messageSource: MessageSource? = null
}

interface MessageRepository: JpaRepository<MessageEntity, Int>, QuerydslPredicateExecutor<MessageEntity> {
    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity): MessageEntity?
    fun findByCreateDateAfter(localDateTime: LocalDateTime): List<MessageEntity>
}

@Service
class MessageService (
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

    fun findAll(group: Long?, contentPa: String?, qq: Long?, pageRequest: PageRequest): Page<MessageEntity> {
        with(QMessageEntity.messageEntity) {
            val bb = BooleanBuilder()
            group?.let { bb + groupEntity.group.eq(it) }
            contentPa?.let{ bb + content.like("%$it%") }
            qq?.let { bb + qqEntity.qq.eq(qq) }
            return messageRepository.findAll(bb, pageRequest.withSort(Sort.by("id").descending()))
        }
    }

    fun findByCreateDateAfter(localDateTime: LocalDateTime) = messageRepository.findByCreateDateAfter(localDateTime)

    fun findByGroupAndLocalDateTimeAfter(group: Long, localDateTimeParam: LocalDateTime): List<MessageEntity> {
        with(QMessageEntity.messageEntity) {
            return messageRepository.findAll(groupEntity.group.eq(group) + createDate.after(localDateTimeParam)).toList()
        }

    }
}

data class MessageSource(
    val id: Int,
    val groupCode: Long,
    val rand: Int
) {

    fun recall() {
        ArtGroupMessageSource(id, rand, groupCode, 0, 0, 0, "").recall()
    }

}

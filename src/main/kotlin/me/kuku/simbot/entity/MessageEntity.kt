package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "message")
data class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    var messageId: String = "",
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @OneToOne
    @JoinColumn(name = "group_")
    var groupEntity: GroupEntity? = null,
    @Column(length = 20000)
    var content: String = "",
    @Temporal(TemporalType.TIMESTAMP)
    var date: Date = Date()
)

interface MessageRepository: JpaRepository<MessageEntity, Int>{
    fun findByMessageIdAndGroupEntity(messageId: String, groupEntity: GroupEntity): MessageEntity?
    fun findByGroupEntityAndDateAfter(groupEntity: GroupEntity, date: Date): List<MessageEntity>
    fun findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<MessageEntity>
}

interface MessageService{
    fun findByMessageIdAndGroupEntity(messageId: String, groupEntity: GroupEntity): MessageEntity?
    fun save(entity: MessageEntity): MessageEntity
    fun findByGroupEntityAndDateAfter(groupEntity: GroupEntity, date: Date): Map<Long, Long>
    fun findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<MessageEntity>
}

@Service
class MessageServiceImpl: MessageService{
    @Resource
    private lateinit var messageRepository: MessageRepository

    override fun findByMessageIdAndGroupEntity(messageId: String, groupEntity: GroupEntity): MessageEntity? {
        return messageRepository.findByMessageIdAndGroupEntity(messageId, groupEntity)
    }

    override fun save(entity: MessageEntity): MessageEntity {
        return messageRepository.save(entity)
    }

    override fun findByGroupEntityAndDateAfter(groupEntity: GroupEntity, date: Date): Map<Long, Long> {
        val list = messageRepository.findByGroupEntityAndDateAfter(groupEntity, date)
        val map = mutableMapOf<Long, Long>()
        for (messageEntity in list){
            val qq = messageEntity.qqEntity!!.qq
            map[qq] = map.getOrDefault(qq, 0) + 1
        }
        return map
    }

    override fun findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<MessageEntity> {
        return messageRepository.findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity)
    }
}
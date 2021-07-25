package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "recall_message")
data class RecallMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @OneToOne
    @JoinColumn(name = "group_")
    var groupEntity: GroupEntity? = null,
    @OneToOne
    @JoinColumn(name = "messageId")
    var messageEntity: MessageEntity? = null,
    @Temporal(TemporalType.TIMESTAMP)
    var date: Date = Date()
)

interface RecallMessageRepository: JpaRepository<RecallMessageEntity, Int>{
    fun findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<RecallMessageEntity>
    fun findByQqEntityOrderByDateDesc(qqEntity: QqEntity): List<RecallMessageEntity>
}

interface RecallMessageService{
    fun findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<RecallMessageEntity>
    fun findByQqEntityOrderByDateDesc(qqEntity: QqEntity): List<RecallMessageEntity>
    fun save(recallMessageEntity: RecallMessageEntity): RecallMessageEntity
}

@Service
class RecallMessageServiceImpl: RecallMessageService{

    @Resource
    private lateinit var recallMessageRepository: RecallMessageRepository

    override fun findByQqEntityAndGroupEntityOrderByDateDesc(
        qqEntity: QqEntity,
        groupEntity: GroupEntity
    ): List<RecallMessageEntity> {
        return recallMessageRepository.findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity)
    }

    override fun findByQqEntityOrderByDateDesc(qqEntity: QqEntity): List<RecallMessageEntity> {
        return recallMessageRepository.findByQqEntityOrderByDateDesc(qqEntity)
    }

    override fun save(recallMessageEntity: RecallMessageEntity): RecallMessageEntity {
        return recallMessageRepository.save(recallMessageEntity)
    }
}
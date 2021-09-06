package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Select
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import java.util.*
import javax.inject.Inject
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

interface RecallMessageDao: JPADao<RecallMessageEntity, Int>{
    @Select("from RecallMessageEntity where qq = ?0 and group_ = ?1 order by date desc")
    fun fByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<RecallMessageEntity>
    @Select("from RecallMessageEntity where qq = ?0 order by date desc")
    fun fByQqEntityOrderByDateDesc(qqEntity: QqEntity): List<RecallMessageEntity>
    @Select("from RecallMessageEntity where group = ?0 order by date desc")
    fun fByGroupEntityOrderByDateDesc(groupEntity: GroupEntity): List<RecallMessageEntity>
}

@AutoBind
interface RecallMessageService{
    fun findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<RecallMessageEntity>
    fun findByQqEntityOrderByDateDesc(qqEntity: QqEntity): List<RecallMessageEntity>
    fun save(recallMessageEntity: RecallMessageEntity)
    fun findByGroupEntityOrderByDateDesc(groupEntity: GroupEntity): List<RecallMessageEntity>
}

class RecallMessageServiceImpl @Inject constructor(private val recallMessageDao: RecallMessageDao): RecallMessageService{
    @Transactional
    override fun findByQqEntityAndGroupEntityOrderByDateDesc(
        qqEntity: QqEntity,
        groupEntity: GroupEntity
    ): List<RecallMessageEntity> {
        return recallMessageDao.fByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity)
    }

    @Transactional
    override fun findByQqEntityOrderByDateDesc(qqEntity: QqEntity): List<RecallMessageEntity> {
        return recallMessageDao.fByQqEntityOrderByDateDesc(qqEntity)
    }

    @Transactional
    override fun save(recallMessageEntity: RecallMessageEntity) {
        return recallMessageDao.saveOrUpdate(recallMessageEntity)
    }

    @Transactional
    override fun findByGroupEntityOrderByDateDesc(groupEntity: GroupEntity): List<RecallMessageEntity> {
        return recallMessageDao.fByGroupEntityOrderByDateDesc(groupEntity)
    }
}
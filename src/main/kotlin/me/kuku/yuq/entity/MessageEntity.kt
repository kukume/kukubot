package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Select
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import java.util.*
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "message")
data class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    var messageId: Int = 0,
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


interface MessageDao: JPADao<MessageEntity, Int>{
    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity): MessageEntity?
    @Select("from MessageEntity where group_ = ?0 and date > ?1")
    fun fByGroupEntityAndDateAfter(groupEntity: GroupEntity, date: Date): List<MessageEntity>
    @Select("from MessageEntity where qq = ?0 and group = ?1 order by date desc")
    fun fByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<MessageEntity>
}

@AutoBind
interface MessageService{
    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity): MessageEntity?
    fun findByGroupEntityAndDateAfter(groupEntity: GroupEntity, date: Date): Map<Long, Long>
    fun findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity: QqEntity, groupEntity: GroupEntity): List<MessageEntity>
    fun save(messageEntity: MessageEntity)
}

class MessageServiceImpl @Inject constructor(private val messageDao: MessageDao): MessageService{

    @Transactional
    override fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity): MessageEntity? {
        return messageDao.findByMessageIdAndGroupEntity(messageId, groupEntity)
    }

    @Transactional
    override fun findByGroupEntityAndDateAfter(groupEntity: GroupEntity, date: Date): Map<Long, Long> {
        val list = messageDao.fByGroupEntityAndDateAfter(groupEntity, date)
        val map = mutableMapOf<Long, Long>()
        for (messageEntity in list){
            val qq = messageEntity.qqEntity!!.qq
            map[qq] = map.getOrDefault(qq, 0) + 1
        }
        return map
    }

    @Transactional
    override fun findByQqEntityAndGroupEntityOrderByDateDesc(
        qqEntity: QqEntity,
        groupEntity: GroupEntity
    ): List<MessageEntity> {
        return messageDao.fByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity)
    }

    @Transactional
    override fun save(messageEntity: MessageEntity) {
        return messageDao.saveOrUpdate(messageEntity)
    }
}
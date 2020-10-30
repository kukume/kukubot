package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.dao.MessageDao
import me.kuku.yuq.entity.MessageEntity
import me.kuku.yuq.service.MessageService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MessageServiceImpl: MessageService {
    @Inject
    private lateinit var messageDao: MessageDao
    @Inject
    private lateinit var hibernateDao: HibernateDao<MessageEntity, Int>

    @Transactional
    override fun findByMessageId(messageId: Int) = messageDao.findByMessageId(messageId)

    @Transactional
    override fun save(messageEntity: MessageEntity) = messageDao.saveOrUpdate(messageEntity)

    @Transactional
    override fun findCountQQByGroupAndToday(group: Long): Map<Long, Long> {
        val date = Date()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date)
        val query = hibernateDao.query(
                "select count(qq),qq from MessageEntity where group_ = ? and date > parsedatetime('$today', 'yyyy-MM-dd') group by qq order by count(qq) desc",
                group
        )
        val result = query.list()
        val map = mutableMapOf<Long, Long>()
        for (obj in result){
            val arr = obj as Array<*>
            map[arr[1].toString().toLong()] = arr[0].toString().toLong()
        }
        return map
    }
}
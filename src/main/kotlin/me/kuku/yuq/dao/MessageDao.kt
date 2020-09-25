package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.MessageEntity
import java.text.SimpleDateFormat
import java.util.*

class MessageDao : HibernateDao<MessageEntity, Long>() {
    fun findByMessageId(messageId: Int) = search("from MessageEntity where messageId = ?", messageId)

    fun findMaxMessageIdByGroup(group: Long): Int? {
        val query = this.query("select max(messageId) from MessageEntity where group_ = ?", group)
        val result = query.list()
        return if (result.size == 0 || result[0] == null) null
        else result[0] as Int
    }

    fun findCountQQByGroupAndToday(group: Long): Map<Long, Long> {
        val date = Date()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date)
        val query = this.query(
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
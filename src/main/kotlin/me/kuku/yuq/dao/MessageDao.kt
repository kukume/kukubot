package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import me.kuku.yuq.entity.MessageEntity

@Dao
interface MessageDao: YuDao<MessageEntity, Long>{
    fun findByMessageId(messageId: Int): MessageEntity?
}
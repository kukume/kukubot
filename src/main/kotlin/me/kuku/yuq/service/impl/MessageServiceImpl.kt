package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.MessageDao
import me.kuku.yuq.entity.MessageEntity
import me.kuku.yuq.service.MessageService
import javax.inject.Inject

class MessageServiceImpl: MessageService {
    @Inject
    private lateinit var messageDao: MessageDao

    @Transactional
    override fun findByMessageId(messageId: Int) = messageDao.findByMessageId(messageId)

    @Transactional
    override fun save(messageEntity: MessageEntity) = messageDao.saveOrUpdate(messageEntity)

    @Transactional
    override fun findMaxMessageIdByGroup(group: Long) = messageDao.findMaxMessageIdByGroup(group)

    @Transactional
    override fun findCountQQByGroupAndToday(group: Long) = messageDao.findCountQQByGroupAndToday(group)
}
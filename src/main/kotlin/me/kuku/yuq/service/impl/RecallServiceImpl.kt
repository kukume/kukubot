package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.RecallDao
import me.kuku.yuq.entity.RecallEntity
import me.kuku.yuq.service.RecallService
import javax.inject.Inject

class RecallServiceImpl: RecallService {
    @Inject
    private lateinit var recallDao: RecallDao

    @Transactional
    override fun findByGroupAndQQ(group: Long, qq: Long) = recallDao.findByGroupAndQQ(group, qq)

    @Transactional
    override fun save(recallEntity: RecallEntity) = recallDao.saveOrUpdate(recallEntity)
}
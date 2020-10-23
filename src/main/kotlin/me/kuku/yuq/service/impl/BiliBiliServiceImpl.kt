package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.BiliBiliDao
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.yuq.service.BiliBiliService
import javax.inject.Inject

class BiliBiliServiceImpl: BiliBiliService {
    @Inject
    private lateinit var biliBiliDao: BiliBiliDao

    @Transactional
    override fun findByQQ(qq: Long) = biliBiliDao.findByQQ(qq)

    @Transactional
    override fun save(biliEntity: BiliBiliEntity) = biliBiliDao.saveOrUpdate(biliEntity)

    @Transactional
    override fun delByQQ(qq: Long) = biliBiliDao.delByQQ(qq)

    @Transactional
    override fun findByMonitor(monitor: Boolean) = biliBiliDao.findByMonitor(monitor)

    @Transactional
    override fun findAll() = biliBiliDao.findAll()

    @Transactional
    override fun findByTask(task: Boolean) = biliBiliDao.findByTask(task)
}
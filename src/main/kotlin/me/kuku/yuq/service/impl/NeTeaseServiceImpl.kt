package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.NeTeaseDao
import me.kuku.yuq.entity.NeTeaseEntity
import me.kuku.yuq.service.NeTeaseService
import javax.inject.Inject

class NeTeaseServiceImpl: NeTeaseService {
    @Inject
    private lateinit var neTeaseDao: NeTeaseDao

    @Transactional
    override fun findByQQ(qq: Long) = neTeaseDao.findByQQ(qq)

    @Transactional
    override fun save(neTeaseEntity: NeTeaseEntity) = neTeaseDao.saveOrUpdate(neTeaseEntity)

    @Transactional
    override fun findAll() = neTeaseDao.findAll()
}
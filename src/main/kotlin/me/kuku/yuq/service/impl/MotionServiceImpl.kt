package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.MotionDao
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.service.MotionService
import javax.inject.Inject

class MotionServiceImpl: MotionService {
    @Inject
    private lateinit var motionDao: MotionDao

    @Transactional
    override fun findByQQ(qq: Long) = motionDao.findByQQ(qq)

    @Transactional
    override fun findAll() = motionDao.findAll()

    @Transactional
    override fun save(motionEntity: MotionEntity) = motionDao.saveOrUpdate(motionEntity)

    @Transactional
    override fun delByQQ(qq: Long) = motionDao.delByQQ(qq)
}
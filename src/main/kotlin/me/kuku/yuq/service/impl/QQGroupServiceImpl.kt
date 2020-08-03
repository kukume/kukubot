package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.QQGroupDao
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.service.QQGroupService
import javax.inject.Inject

class QQGroupServiceImpl: QQGroupService {
    @Inject
    private lateinit var qqGroupDao: QQGroupDao

    @Transactional
    override fun save(qqGroupEntity: QQGroupEntity) = qqGroupDao.saveOrUpdate(qqGroupEntity)

    @Transactional
    override fun findByGroup(group: Long) = qqGroupDao.findByGroup(group)

    @Transactional
    override fun findByOnTimeAlarm(onTimeAlarm: Boolean) = qqGroupDao.findByOnTimeAlarm(onTimeAlarm)

    @Transactional
    override fun findAll() = qqGroupDao.findAll()
}
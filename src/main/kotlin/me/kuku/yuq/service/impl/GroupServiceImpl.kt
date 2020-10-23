package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.GroupDao
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.service.GroupService
import javax.inject.Inject

class GroupServiceImpl: GroupService {
    @Inject
    private lateinit var groupDao: GroupDao

    @Transactional
    override fun save(groupEntity: GroupEntity) = groupDao.saveOrUpdate(groupEntity)

    @Transactional
    override fun findByGroup(group: Long) = groupDao.findByGroup(group)

    @Transactional
    override fun findByOnTimeAlarm(onTimeAlarm: Boolean) = groupDao.findByOnTimeAlarm(onTimeAlarm)

    @Transactional
    override fun findAll() = groupDao.findAll()

    @Transactional
    override fun findByLocMonitor(locMonitor: Boolean) = groupDao.findByLocMonitor(locMonitor)
}
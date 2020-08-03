package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.GroupQQDao
import me.kuku.yuq.entity.GroupQQEntity
import me.kuku.yuq.service.GroupQQService
import javax.inject.Inject

class GroupQQServiceImpl: GroupQQService {
    @Inject
    private lateinit var groupQQDao: GroupQQDao

    @Transactional
    override fun findByQQAndGroup(qq: Long, group: Long) = groupQQDao.findByQQAndGroup(qq, group)

    @Transactional
    override fun save(groupQQEntity: GroupQQEntity) = groupQQDao.saveOrUpdate(groupQQEntity)

    @Transactional
    override fun delByQQAndGroup(qq: Long, group: Long) = groupQQDao.delByQQAndGroup(qq, group)

    @Transactional
    override fun findAll() = groupQQDao.findAll()
}
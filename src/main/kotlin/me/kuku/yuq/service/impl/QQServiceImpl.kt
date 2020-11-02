package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.GroupDao
import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.service.QQService
import javax.inject.Inject

class QQServiceImpl: QQService {
    @Inject
    private lateinit var qqDao: QQDao
    @Inject
    private lateinit var groupDao: GroupDao

    @Transactional
    override fun findByQQAndGroup(qq: Long, group: Long): QQEntity? {
        val groupEntity = groupDao.findByGroup(group) ?: return null
        return qqDao.findByQQAndGroup(qq, groupEntity.id!!)
    }

    @Transactional
    override fun save(QQEntity: QQEntity) = qqDao.saveOrUpdate(QQEntity)

    @Transactional
    override fun delByQQAndGroup(qq: Long, group: Long): Int {
        val groupEntity = groupDao.findByGroup(group) ?: return 0
        return qqDao.delByQQAndGroup(qq, groupEntity.id!!)
    }

    @Transactional
    override fun findAll() = qqDao.findAll()
}
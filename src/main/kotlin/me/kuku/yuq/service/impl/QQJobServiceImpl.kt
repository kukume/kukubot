package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.QQJobDao
import me.kuku.yuq.entity.QQJobEntity
import me.kuku.yuq.service.QQJobService
import javax.inject.Inject

class QQJobServiceImpl: QQJobService {

    @Inject
    private lateinit var qqJobDao: QQJobDao

    @Transactional
    override fun findByQQAndType(qq: Long, type: String) = qqJobDao.findByQQAndType(qq, type)

    @Transactional
    override fun findByQQ(qq: Long) = qqJobDao.findByQQ(qq)

    @Transactional
    override fun findByType(type: String) = qqJobDao.findByType(type)

    @Transactional
    override fun delByQQ(qq: Long) = qqJobDao.delByQQ(qq)

    @Transactional
    override fun save(qqJobEntity: QQJobEntity) = qqJobDao.saveOrUpdate(qqJobEntity)
}
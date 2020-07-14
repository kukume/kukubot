package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.service.QQService
import javax.inject.Inject

class QQServiceImpl: QQService {
    @Inject
    private lateinit var qqDao: QQDao

    @Transactional
    override fun findByQQ(qq: Long) = qqDao.findByQQ(qq)

    @Transactional
    override fun save(qqEntity: QQEntity) = qqDao.saveOrUpdate(qqEntity)

    @Transactional
    override fun delByQQ(qq: Long) = qqDao.delByQQ(qq)

    @Transactional
    override fun findByActivity() = qqDao.findByActivity()


}
package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.QQLoginDao
import me.kuku.yuq.entity.QQLoginEntity
import me.kuku.yuq.service.QQLoginService
import javax.inject.Inject

class QQLoginServiceImpl: QQLoginService {
    @Inject
    private lateinit var qqLoginDao: QQLoginDao

    @Transactional
    override fun findByQQ(qq: Long) = qqLoginDao.findByQQ(qq)

    @Transactional
    override fun save(qqLoginEntity: QQLoginEntity) = qqLoginDao.saveOrUpdate(qqLoginEntity)

    @Transactional
    override fun delByQQ(qq: Long) = qqLoginDao.delByQQ(qq)

    @Transactional
    override fun findByActivity() = qqLoginDao.findByActivity()


}
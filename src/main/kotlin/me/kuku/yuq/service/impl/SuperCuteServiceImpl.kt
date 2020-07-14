package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.SuperCuteDao
import me.kuku.yuq.entity.SuperCuteEntity
import me.kuku.yuq.service.SuperCuteService
import javax.inject.Inject

class SuperCuteServiceImpl: SuperCuteService {

    @Inject
    private lateinit var superCuteDao: SuperCuteDao

    @Transactional
    override fun findByQQ(qq: Long) = superCuteDao.findByQQ(qq)

    @Transactional
    override fun save(superCuteEntity: SuperCuteEntity) = superCuteDao.saveOrUpdate(superCuteEntity)

    @Transactional
    override fun delByQQ(qq: Long) = superCuteDao.delByQQ(qq)
}
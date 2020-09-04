package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.ConfigDao
import me.kuku.yuq.entity.ConfigEntity
import me.kuku.yuq.service.ConfigService
import javax.inject.Inject

class ConfigServiceImpl: ConfigService {

    @Inject
    private lateinit var configDao: ConfigDao

    @Transactional
    override fun findAll() = configDao.findAll()

    @Transactional
    override fun save(configEntity: ConfigEntity) = configDao.saveOrUpdate(configEntity)

    @Transactional
    override fun findByType(type: String) = configDao.findByType(type)
}
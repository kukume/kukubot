package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.SteamDao
import me.kuku.yuq.entity.SteamEntity
import me.kuku.yuq.service.SteamService
import javax.inject.Inject

class SteamServiceImpl: SteamService {
    @Inject
    private lateinit var steamDao: SteamDao

    @Transactional
    override fun findByQQ(qq: Long) = steamDao.findByQQ(qq)

    @Transactional
    override fun save(steamEntity: SteamEntity) = steamDao.saveOrUpdate(steamEntity)

    @Transactional
    override fun delByQQ(qq: Long) = steamDao.delByQQ(qq)
}
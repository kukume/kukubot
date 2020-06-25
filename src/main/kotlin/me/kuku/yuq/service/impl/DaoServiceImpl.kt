package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transaction
import me.kuku.yuq.dao.MotionDao
import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.dao.SteamDao
import me.kuku.yuq.dao.SuperCuteDao
import me.kuku.yuq.entity.MotionEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.SteamEntity
import me.kuku.yuq.entity.SuperCuteEntity
import me.kuku.yuq.service.DaoService
import javax.inject.Inject

class DaoServiceImpl: DaoService {
    @Inject
    private lateinit var qqDao: QQDao
    @Inject
    private lateinit var motionDao: MotionDao
    @Inject
    private lateinit var superCuteDao: SuperCuteDao
    @Inject
    private lateinit var steamDao: SteamDao

    @Transaction
    fun saveOrUpdateQQ(entity: QQEntity) = qqDao.saveOrUpdate(entity)

    @Transaction
    fun saveOrUpdateMotion(entity: MotionEntity) = motionDao.saveOrUpdate(entity)

    @Transaction
    fun saveOrUpdateSuperCute(entity: SuperCuteEntity) = superCuteDao.saveOrUpdate(entity)

    @Transaction
    fun saveOrUpdateSteam(entity: SteamEntity) = steamDao.saveOrUpdate(entity)

    fun findQQByQQ(qq: Long) = qqDao.findByQQ(qq)

    fun findMotionByQQ(qq: Long) = motionDao.findByQQ(qq)

    fun findSuperCuteByQQ(qq: Long) = superCuteDao.findByQQ(qq)

    fun findSteamByQQ(qq: Long) = steamDao.findByQQ(qq)

    fun findQQByAll() = qqDao.findAll()

    fun findMotionByAll() = motionDao.findAll()



}
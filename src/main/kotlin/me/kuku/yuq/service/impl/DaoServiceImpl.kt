package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.*
import me.kuku.yuq.entity.*
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
    @Inject
    private lateinit var qqJobDao: QQJobDao

    @Transactional
    override fun saveOrUpdateQQ(entity: QQEntity) = qqDao.saveOrUpdate(entity)

    @Transactional
    override fun saveOrUpdateMotion(entity: MotionEntity) = motionDao.saveOrUpdate(entity)

    @Transactional
    override fun saveOrUpdateSuperCute(entity: SuperCuteEntity) = superCuteDao.saveOrUpdate(entity)

    @Transactional
    override fun saveOrUpdateSteam(entity: SteamEntity) = steamDao.saveOrUpdate(entity)

    @Transactional
    override fun saveOrUpdateQQJob(entity: QQJobEntity) = qqJobDao.saveOrUpdate(entity)

    @Transactional
    override fun delQQ(qqEntity: QQEntity) {
        qqDao.delete(qqEntity.id!!)
        val list = this.findQQJobByQQ(qqEntity.qq)
        list?.forEach {
            val qqJobEntity = it as QQJobEntity
            qqJobDao.delete(qqJobEntity.id!!)
        }
    }

    override fun findQQByQQ(qq: Long) = qqDao.findByQQ(qq)

    override fun findQQJobByQQAndType(qq: Long, type: String) = qqJobDao.findByQQAndType(qq, type)

    override fun findMotionByQQ(qq: Long) = motionDao.findByQQ(qq)

    override fun findSuperCuteByQQ(qq: Long) = superCuteDao.findByQQ(qq)

    override fun findSteamByQQ(qq: Long) = steamDao.findByQQ(qq)

    override fun findQQJobByQQ(qq: Long) = qqJobDao.findByQQ(qq)

    override fun findQQByActivity(): MutableList<Any?>? = qqDao.findActivity()

    override fun findQQByAll() = qqDao.findAll()

    override fun findMotionByAll() = motionDao.findAll()

    override fun findQQJobByType(type: String) = qqJobDao.findByType(type)
}
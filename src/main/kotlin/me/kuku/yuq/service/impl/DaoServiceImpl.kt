package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.*
import me.kuku.yuq.service.DaoService
import javax.inject.Inject

class DaoServiceImpl: DaoService {
    @Inject
    private lateinit var qqJobDao: QQJobDao
    @Inject
    private lateinit var qqDao: QQDao
    @Inject
    private lateinit var motionDao: MotionDao
    @Inject
    private lateinit var superCuteDao: SuperCuteDao
    @Inject
    private lateinit var steamDao: SteamDao
    @Inject
    private lateinit var biliBiliDao: BiliBiliDao
    @Inject
    private lateinit var neTeaseDao: NeTeaseDao
    @Inject
    private lateinit var weiboDao: WeiboDao

    @Transactional
    override fun delQQ(qq: Long) {
        qqJobDao.delByQQ(qq)
        qqDao.delByQQ(qq)
        motionDao.delByQQ(qq)
        superCuteDao.delByQQ(qq)
        steamDao.delByQQ(qq)
        biliBiliDao.delByQQ(qq)
        neTeaseDao.delByQQ(qq)
        weiboDao.delByQQ(qq)
    }
}
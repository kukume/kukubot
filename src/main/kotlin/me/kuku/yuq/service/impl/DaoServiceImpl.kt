package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.*
import me.kuku.yuq.service.DaoService
import javax.inject.Inject

class DaoServiceImpl: DaoService {
    @Inject
    private lateinit var qqJobDao: QQJobDao
    @Inject
    private lateinit var qqLoginDao: QQLoginDao
    @Inject
    private lateinit var motionDao: MotionDao
    @Inject
    private lateinit var biliBiliDao: BiliBiliDao
    @Inject
    private lateinit var neTeaseDao: NeTeaseDao
    @Inject
    private lateinit var weiboDao: WeiboDao

    @Transactional
    override fun delQQ(qq: Long) {
        qqJobDao.delByQQ(qq)
        qqLoginDao.delByQQ(qq)
        motionDao.delByQQ(qq)
        biliBiliDao.delByQQ(qq)
        neTeaseDao.delByQQ(qq)
        weiboDao.delByQQ(qq)
    }
}
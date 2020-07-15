package me.kuku.yuq.service.impl

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.*
import me.kuku.yuq.service.DaoService
import javax.inject.Inject

@AutoBind
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

    @Transactional
    override fun delQQ(qq: Long) {
        qqJobDao.delByQQ(qq)
        qqDao.delByQQ(qq)
        motionDao.delByQQ(qq)
        superCuteDao.delByQQ(qq)
        steamDao.delByQQ(qq)
    }
}
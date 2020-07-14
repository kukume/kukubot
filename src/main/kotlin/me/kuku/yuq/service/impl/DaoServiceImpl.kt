package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.service.*
import javax.inject.Inject

class DaoServiceImpl: DaoService {
    @Inject
    private lateinit var qqJobService: QQJobService
    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var motionService: MotionService
    @Inject
    private lateinit var superCuteService: SuperCuteService
    @Inject
    private lateinit var steamService: SteamService

    @Transactional
    override fun delQQ(qq: Long) {
        qqJobService.delByQQ(qq)
        qqService.delByQQ(qq)
        motionService.delByQQ(qq)
        superCuteService.delByQQ(qq)
        steamService.delByQQ(qq)
    }
}
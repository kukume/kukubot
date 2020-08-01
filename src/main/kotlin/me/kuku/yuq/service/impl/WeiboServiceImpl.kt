package me.kuku.yuq.service.impl

import com.icecreamqaq.yudb.jpa.annotation.Transactional
import me.kuku.yuq.dao.WeiboDao
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.service.WeiboService
import javax.inject.Inject

class WeiboServiceImpl: WeiboService {
    @Inject
    private lateinit var weiboDao: WeiboDao

    @Transactional
    override fun findByQQ(qq: Long) = weiboDao.findByQQ(qq)

    @Transactional
    override fun save(weiboEntity: WeiboEntity) = weiboDao.saveOrUpdate(weiboEntity)

    @Transactional
    override fun delByQQ(qq: Long) = weiboDao.delByQQ(qq)
}
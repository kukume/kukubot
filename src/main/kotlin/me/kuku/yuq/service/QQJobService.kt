package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQJobEntity

@AutoBind
interface QQJobService {
    fun findByQQAndType(qq: Long, type: String): QQJobEntity?
    fun findByQQ(qq: Long): List<QQJobEntity>
    fun findByType(type: String): List<QQJobEntity>
    fun delByQQ(qq: Long)
    fun save(qqJobEntity: QQJobEntity)
}
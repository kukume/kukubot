package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.BiliBiliEntity

@AutoBind
interface BiliBiliService {
    fun findByQQ(qq: Long): BiliBiliEntity?
    fun save(biliEntity: BiliBiliEntity)
    fun delByQQ(qq: Long): Int
    fun findByMonitor(monitor: Boolean): List<BiliBiliEntity>
    fun findAll(): List<BiliBiliEntity>
}
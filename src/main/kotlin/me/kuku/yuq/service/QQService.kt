package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQEntity

@AutoBind
interface QQService {
    fun findByQQAndGroup(qq: Long, group: Long): QQEntity?
    fun save(QQEntity: QQEntity)
    fun delByQQAndGroup(qq: Long, group: Long): Int
    fun findAll(): List<QQEntity>
}
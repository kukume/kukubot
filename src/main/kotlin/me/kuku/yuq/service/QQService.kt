package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQEntity

@AutoBind
interface QQService {
    fun findByQQ(qq: Long): QQEntity?
    fun save(qqEntity: QQEntity)
    fun delByQQ(qq: Long)
    fun findByActivity(): List<QQEntity>
}
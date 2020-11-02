package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQLoginEntity

@AutoBind
interface QQLoginService {
    fun findByQQ(qq: Long): QQLoginEntity?
    fun save(qqLoginEntity: QQLoginEntity)
    fun delByQQ(qq: Long)
    fun findByActivity(): List<QQLoginEntity>
}
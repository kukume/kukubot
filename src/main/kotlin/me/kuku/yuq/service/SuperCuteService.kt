package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.SuperCuteEntity

@AutoBind
interface SuperCuteService {
    fun findByQQ(qq: Long): SuperCuteEntity?
    fun save(superCuteEntity: SuperCuteEntity)
    fun delByQQ(qq: Long)
}
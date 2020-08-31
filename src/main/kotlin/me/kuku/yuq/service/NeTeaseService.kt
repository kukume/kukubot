package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.NeTeaseEntity

@AutoBind
interface NeTeaseService {

    fun findByQQ(qq: Long): NeTeaseEntity?
    fun save(neTeaseEntity: NeTeaseEntity)
    fun findAll(): List<NeTeaseEntity>
    fun delByQQ(qq: Long): Int

}
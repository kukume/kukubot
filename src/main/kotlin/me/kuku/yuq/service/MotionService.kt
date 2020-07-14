package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.MotionEntity

@AutoBind
interface MotionService {
    fun findByQQ(qq: Long): MotionEntity?
    fun findAll(): List<MotionEntity>
    fun save(motionEntity: MotionEntity)
    fun delByQQ(qq: Long)
}
package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.RecallEntity

@AutoBind
interface RecallService {
    fun findByGroupAndQQ(group: Long, qq: Long): List<RecallEntity>
    fun save(recallEntity: RecallEntity)
}
package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.GroupQQEntity

@AutoBind
interface GroupQQService {
    fun findByQQAndGroup(qq: Long, group: Long): GroupQQEntity?
    fun save(groupQQEntity: GroupQQEntity)
    fun delByQQAndGroup(qq: Long, group: Long): Int
    fun findAll(): List<GroupQQEntity>
}
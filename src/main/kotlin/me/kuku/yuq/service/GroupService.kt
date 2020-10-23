package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.GroupEntity

@AutoBind
interface GroupService {

    fun save(groupEntity: GroupEntity)
    fun findByGroup(group: Long): GroupEntity?
    fun findByOnTimeAlarm(onTimeAlarm: Boolean): List<GroupEntity>
    fun findAll(): List<GroupEntity>
    fun findByLocMonitor(locMonitor: Boolean): List<GroupEntity>

}
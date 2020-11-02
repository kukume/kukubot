package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Select
import me.kuku.yuq.entity.GroupEntity

@Dao
interface GroupDao: YuDao<GroupEntity, Int>{
    fun findByGroup(group: Long): GroupEntity?
    fun findByOnTimeAlarm(onTimeAlarm: Boolean): List<GroupEntity>
    @Select("from GroupEntity")
    fun findAll(): List<GroupEntity>
    fun findByLocMonitor(locMonitor: Boolean): List<GroupEntity>
}
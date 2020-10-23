package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.GroupEntity

class GroupDao: HibernateDao<GroupEntity, Int>(){

    fun findByGroup(group: Long) = this.search("from GroupEntity where group_ = ?", group)

    fun findByOnTimeAlarm(onTimeAlarm: Boolean) = this.searchList("from GroupEntity where onTimeAlarm = ?", null, onTimeAlarm)

    fun findAll() = this.searchList("from GroupEntity")

    fun findByLocMonitor(locMonitor: Boolean) = this.searchList("from GroupEntity where locMonitor = ?", null, locMonitor)

}
package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.QQGroupEntity

class QQGroupDao: HibernateDao<QQGroupEntity, Int>(){

    fun findByGroup(group: Long) = this.search("from QQGroupEntity where group_ = ?", group)

    fun findByOnTimeAlarm(onTimeAlarm: Boolean) = this.searchList("from QQGroupEntity where onTimeAlarm = ?", null, onTimeAlarm)

}
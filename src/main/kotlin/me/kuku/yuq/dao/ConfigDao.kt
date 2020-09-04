package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.ConfigEntity

class ConfigDao: HibernateDao<ConfigEntity, Int>() {
    fun findAll() = this.searchList("from ConfigEntity")

    fun findByType(type: String) = this.search("from ConfigEntity where type = ?", type)
}
package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Select
import me.kuku.yuq.entity.ConfigEntity

@Dao
interface ConfigDao: YuDao<ConfigEntity, Int>{
    @Select("from ConfigEntity")
    fun findAll(): List<ConfigEntity>
    fun findByType(type: String): ConfigEntity?
}
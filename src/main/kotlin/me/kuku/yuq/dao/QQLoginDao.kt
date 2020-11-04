package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Execute
import com.icecreamqaq.yudb.jpa.annotation.Select
import me.kuku.yuq.entity.QQLoginEntity

@Dao
interface QQLoginDao: YuDao<QQLoginEntity, Int>{
    fun findByQQ(qq: Long): QQLoginEntity?
    @Select("from QQLoginEntity")
    fun findAll(): List<QQLoginEntity>
    @Select("from QQLoginEntity where status = true")
    fun findByActivity(): List<QQLoginEntity>
    @Execute("delete from QQLoginEntity where qq = ?0")
    fun delByQQ(qq: Long)
}
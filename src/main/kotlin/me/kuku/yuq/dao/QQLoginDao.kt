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
    fun findByActivity(): List<QQLoginEntity>
    @Execute("delete from QQLoginEntity where qq = ?")
    fun delByQQ(qq: Long)
}
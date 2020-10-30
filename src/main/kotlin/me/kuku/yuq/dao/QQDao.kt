package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Execute
import com.icecreamqaq.yudb.jpa.annotation.Select
import me.kuku.yuq.entity.QQEntity

@Dao
interface QQDao: YuDao<QQEntity, Int>{
    fun findByQQAndGroup(qq: Long, group: Int): QQEntity?
    @Execute("delete from QQEntity where qq = ? and group_id = ?")
    fun delByQQAndGroup(qq: Long, group: Int): Int
    @Select("from QQEntity")
    fun findAll(): List<QQEntity>
}
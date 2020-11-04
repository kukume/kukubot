package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Execute
import com.icecreamqaq.yudb.jpa.annotation.Select
import me.kuku.yuq.entity.WeiboEntity

@Dao
interface WeiboDao: YuDao<WeiboEntity, Int>{
    fun findByQQ(qq: Long): WeiboEntity?
    @Execute("delete from WeiboEntity where qq = ?0")
    fun delByQQ(qq: Long): Int
    fun findByMonitor(monitor: Boolean): List<WeiboEntity>
    @Select("from WeiboEntity")
    fun findAll(): List<WeiboEntity>
}
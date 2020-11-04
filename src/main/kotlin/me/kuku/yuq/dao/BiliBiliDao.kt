package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Execute
import com.icecreamqaq.yudb.jpa.annotation.Select
import me.kuku.yuq.entity.BiliBiliEntity

@Dao
interface BiliBiliDao: YuDao<BiliBiliEntity, Int>{
    fun findByQQ(qq: Long): BiliBiliEntity?
    @Execute("delete from BiliBiliEntity where qq = ?0")
    fun delByQQ(qq: Long): Int
    fun findByMonitor(monitor: Boolean): List<BiliBiliEntity>
    @Select("from BiliBiliEntity")
    fun findAll(): List<BiliBiliEntity>
    fun findByTask(task: Boolean): List<BiliBiliEntity>
}
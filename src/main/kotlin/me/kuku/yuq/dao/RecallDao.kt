package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import me.kuku.yuq.entity.RecallEntity

@Dao
interface RecallDao: YuDao<RecallEntity, Int>{
    fun findByGroupAndQQ(group: Long, qq: Long): List<RecallEntity>
}
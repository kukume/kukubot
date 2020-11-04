package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Execute
import com.icecreamqaq.yudb.jpa.annotation.Select
import me.kuku.yuq.entity.MotionEntity

@Dao
interface MotionDao: YuDao<MotionEntity, Int>{
    fun findByQQ(qq: Long): MotionEntity?
    @Select("from MotionEntity")
    fun findAll(): List<MotionEntity>
    @Execute("delete from MotionEntity where qq = ?1")
    fun delByQQ(qq: Long)
}
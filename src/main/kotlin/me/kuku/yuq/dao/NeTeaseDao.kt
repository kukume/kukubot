package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Execute
import com.icecreamqaq.yudb.jpa.annotation.Select
import me.kuku.yuq.entity.NeTeaseEntity

@Dao
interface NeTeaseDao: YuDao<NeTeaseEntity, Int>{
    @Select("from NeTeaseEntity where qq = ?")
    fun findByQQ(qq: Long): NeTeaseEntity?
    @Select("from NeTeaseEntity")
    fun findAll(): List<NeTeaseEntity>
    @Execute("delete from GroupQQEntity where qq = ?")
    fun delByQQ(qq: Long): Int
}
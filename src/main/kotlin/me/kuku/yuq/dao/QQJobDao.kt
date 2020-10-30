package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import com.icecreamqaq.yudb.jpa.annotation.Execute
import me.kuku.yuq.entity.QQJobEntity

@Dao
interface QQJobDao: YuDao<QQJobEntity, Int>{
    fun findByQQAndType(qq: Long, type: String): QQJobEntity?
    fun findByQQ(qq: Long): List<QQJobEntity>
    fun findByType(type: String): List<QQJobEntity>
    @Execute("delete from QQJobEntity where qq = ?")
    fun delByQQ(qq: Long)
}
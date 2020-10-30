package me.kuku.yuq.dao

import com.icecreamqaq.yudb.YuDao
import com.icecreamqaq.yudb.jpa.annotation.Dao
import me.kuku.yuq.entity.UserRecord

@Dao
interface UserRecordDao: YuDao<UserRecord, Int>{
    fun findByQqAndPool(qq: Long, pool: String): UserRecord?
}
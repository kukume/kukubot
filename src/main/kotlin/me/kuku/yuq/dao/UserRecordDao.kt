package me.kuku.yuq.dao

import me.kuku.yuq.entity.UserRecord
import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao

class UserRecordDao : HibernateDao<UserRecord, Int>() {


    fun findByQqAndPool(qq: Long, pool: String) = search("from UserRecord where qq = ? and pool = ?", qq, pool)


}
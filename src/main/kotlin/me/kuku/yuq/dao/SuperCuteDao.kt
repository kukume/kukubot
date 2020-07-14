package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.SuperCuteEntity

class SuperCuteDao: HibernateDao<SuperCuteEntity, Int>() {

    fun findByQQ(qq: Long) = this.search("from SuperCuteEntity where qq = ?", qq)

    fun delByQQ(qq: Long) {
        val query = this.query("delete from SuperCuteEntity where qq = ?", qq)
        query.executeUpdate()
    }

}
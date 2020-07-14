package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.MotionEntity

class MotionDao: HibernateDao<MotionEntity, Int>() {

    fun findByQQ(qq: Long) = this.search("from MotionEntity where qq = ?", qq)

    fun findAll() = this.searchList("from MotionEntity")

    fun delByQQ(qq: Long) {
        val query = this.query("delete from MotionEntity where qq = ?", qq)
        query.executeUpdate()
    }

}
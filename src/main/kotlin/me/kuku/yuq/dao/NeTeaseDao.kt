package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.NeTeaseEntity

class NeTeaseDao: HibernateDao<NeTeaseEntity, Int>() {

    fun findByQQ(qq: Long) = this.search("from NeTeaseEntity where qq = ?", qq)

    fun findAll() = this.searchList("from NeTeaseEntity")

    fun delByQQ(qq: Long): Int{
        val query = this.query("delete from GroupQQEntity where qq = ?", qq)
        return query.executeUpdate()
    }
}
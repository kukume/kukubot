package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.BiliBiliEntity

class BiliBiliDao: HibernateDao<BiliBiliEntity, Int>() {
    fun findByQQ(qq: Long) = this.search("from BiliBiliEntity where qq = ?", qq)

    fun delByQQ(qq: Long): Int {
        val query = this.query("delete from BiliBiliEntity where qq = ?", qq)
        return query.executeUpdate()
    }

    fun findByMonitor(monitor: Boolean) = this.searchList("from BiliBiliEntity where monitor = ?", null, monitor)

    fun findAll() = this.searchList("from BiliBiliEntity")
}
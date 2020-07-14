package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.QQEntity

class QQDao: HibernateDao<QQEntity, Int>() {
    fun findByQQ(qq: Long) = this.search("from QQEntity where qq = ?", qq)

    fun findAll() = this.searchList("from QQEntity")

    fun findByActivity() = this.searchList("from QQEntity where status = true")

    fun delByQQ(qq: Long) {
        val query = this.query("delete from QQEntity where qq = ?", qq)
        query.executeUpdate()
    }
}
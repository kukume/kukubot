package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.QQLoginEntity

class QQLoginDao: HibernateDao<QQLoginEntity, Int>() {
    fun findByQQ(qq: Long) = this.search("from QQLoginEntity where qq = ?", qq)

    fun findAll() = this.searchList("from QQLoginEntity")

    fun findByActivity() = this.searchList("from QQLoginEntity where status = true")

    fun delByQQ(qq: Long) {
        val query = this.query("delete from QQLoginEntity where qq = ?", qq)
        query.executeUpdate()
    }
}
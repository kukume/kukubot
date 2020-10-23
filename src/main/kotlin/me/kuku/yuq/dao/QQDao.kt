package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.QQEntity

class QQDao: HibernateDao<QQEntity, Int>() {

    fun findByQQAndGroup(qq: Long, group: Int) =
            this.search("from QQEntity where qq = ? and group_id = ?", qq, group)

    fun delByQQAndGroup(qq: Long, group: Int): Int {
        val query = this.query("delete from QQEntity where qq = ? and group_id = ?", qq, group)
        return query.executeUpdate()
    }

    fun findAll() = this.searchList("from QQEntity")
}
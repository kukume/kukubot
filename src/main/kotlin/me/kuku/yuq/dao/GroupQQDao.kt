package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.GroupQQEntity

class GroupQQDao: HibernateDao<GroupQQEntity, Int>() {

    fun findByQQAndGroup(qq: Long, group: Long) =
            this.search("from GroupQQEntity where qq = ? and group_ = ?", qq, group)

    fun delByQQAndGroup(qq: Long, group: Long): Int {
        val query = this.query("delete from GroupQQEntity where qq = ? and group_ = ?", qq, group)
        return query.executeUpdate()
    }

    fun findAll() = this.searchList("from GroupQQEntity")


}
package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.QQJobEntity

class QQJobDao: HibernateDao<QQJobEntity, Int>() {

    fun findByQQAndType(qq: Long, type: String) = this.search("from QQJobEntity where qq = ? and type = ?", qq, type)

    fun findByQQ(qq: Long) = this.searchList("from QQJobEntity where qq = ?", null,  qq)

    fun findByType(type: String) = this.searchList("from QQJobEntity where type = ?", null, type)

    fun delByQQ(qq: Long) {
        val query = this.query("delete from QQJobEntity where qq = ?", qq)
        query.executeUpdate()
    }


}
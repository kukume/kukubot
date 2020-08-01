package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.WeiboEntity

class WeiboDao: HibernateDao<WeiboEntity, Int>() {

    fun findByQQ(qq: Long) = this.search("from WeiboEntity where qq = ?", qq)

    fun delByQQ(qq: Long): Int {
        val query = this.query("delete from WeiboEntity where qq = ?", qq)
        return query.executeUpdate()
    }

}
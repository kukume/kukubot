package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.SteamEntity

class SteamDao: HibernateDao<SteamEntity, Int>() {

    fun findByQQ(qq: Long) = this.search("from SteamEntity where qq = ?", qq)

    fun delByQQ(qq: Long) {
        val query = this.query("delete from SteamEntity where qq = ?", qq)
        query.executeUpdate()
    }


}
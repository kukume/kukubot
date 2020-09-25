package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.RecallEntity

class RecallDao: HibernateDao<RecallEntity, Int>() {

    fun findByGroupAndQQ(group: Long, qq: Long) = this.searchList("from RecallEntity where group_ = ? and qq = ? order by id desc", null, group, qq)

}
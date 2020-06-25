package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.MotionEntity

class MotionDao: HibernateDao<MotionEntity, Int>() {

    fun findByQQ(qq: Long): MotionEntity? {
        val session = this.getSession()
        val query = session.createQuery("from MotionEntity where qq = :qq")
        query.setLong("qq", qq)
        val result = query.uniqueResult()
        session.close()
        return if (result == null) null else result as MotionEntity
    }

    fun findAll(): MutableList<Any?>? {
        val session = this.getSession()
        val query = session.createQuery("from MotionEntity")
        val resultList = query.list()
        session.close()
        return resultList
    }

}
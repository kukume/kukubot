package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.QQEntity

class QQDao: HibernateDao<QQEntity, Int>() {
    fun findByQQ(qq: Long): QQEntity? {
        val session = this.getSession()
        val query = session.createQuery("from QQEntity where qq = :qq")
        query.setLong("qq", qq)
        val result = query.uniqueResult()
        session.close()
        return if (result == null) null else result as QQEntity
    }

    fun findAll(): MutableList<Any?>? {
        val session = this.getSession()
        val query = session.createQuery("from QQEntity")
        val resultList = query.list()
        session.close()
        return resultList
    }

    fun findActivity(): MutableList<Any?>?{
        val session = this.getSession()
        val query = session.createQuery("from QQEntity where status = true")
        val resultList =  query.list()
        session.close()
        return resultList
    }

}
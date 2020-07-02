package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.QQJobEntity

class QQJobDao: HibernateDao<QQJobEntity, Int>() {

    fun findByQQAndType(qq: Long, type: String): QQJobEntity?{
        val session = this.getSession()
        val query = session.createQuery("from QQJobEntity where qq = :qq and type = :type")
        query.setLong("qq", qq)
        query.setString("type", type)
        val result = query.uniqueResult()
        session.close()
        return if (result == null) null else result as QQJobEntity
    }

    fun findByQQ(qq: Long): MutableList<Any?>? {
        val session = this.getSession()
        val query = session.createQuery("from QQJobEntity where qq = :qq")
        query.setLong("qq", qq)
        val list = query.list()
        return list
    }

    fun findByType(type: String): MutableList<Any?>? {
        val session = this.getSession()
        val query = session.createQuery("from QQJobEntity where type = :type")
        query.setString("type", type)
        return query.list()
    }

}
package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.QQEntity

class QQDao: HibernateDao<QQEntity, Int>() {
    fun findByQQ(qq: Long): QQEntity? {
        val session = this.getSession()
        val query = session.createQuery("from QQEntity where qq = :qq")
        query.setLong("qq", qq)
        val result = query.uniqueResult()
        return if (result == null) null else result as QQEntity
    }

    fun singleSave(entity: QQEntity) {
        val session = this.getSession()
        val transaction = session.beginTransaction()
        session.save(entity)
        transaction.commit()
    }

    fun findAll(): MutableList<Any?>? {
        val session = this.getSession()
        val query = session.createQuery("from QQEntity")
        return query.list()
    }

}
package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.MotionEntity

class MotionDao: HibernateDao<MotionEntity, Int>() {

    fun findByQQ(qq: Long): MotionEntity? {
        val session = this.getSession()
        val query = session.createQuery("from MotionEntity where qq = :qq")
        query.setLong("qq", qq)
        val result = query.uniqueResult()
        return if (result == null) null else result as MotionEntity
    }

    fun singleSaveOrUpdate(entity: MotionEntity) {
        val session = this.getSession()
        val transaction = session.beginTransaction()
        super.saveOrUpdate(entity)
        transaction.commit()
        session.close()
    }

    fun findAll(): MutableList<Any?>? {
        val session = this.getSession()
        val query = session.createQuery("from MotionEntity")
        return query.list()
    }

}
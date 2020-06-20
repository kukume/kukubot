package me.kuku.yuq.dao

import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao
import me.kuku.yuq.entity.SuperCuteEntity

class SuperCuteDao: HibernateDao<SuperCuteEntity, Int>() {

    fun findByQQ(qq: Long): SuperCuteEntity? {
        val session = this.getSession()
        val query = session.createQuery("from SuperCuteEntity where qq = :qq")
        query.setLong("qq", qq)
        val result = query.uniqueResult()
        return if (result == null) null else result as SuperCuteEntity
    }

    fun singleSave(entity: SuperCuteEntity) {
        val session = this.getSession()
        val transaction = session.beginTransaction()
        super.saveOrUpdate(entity)
        transaction.commit()
    }

}
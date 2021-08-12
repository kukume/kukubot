package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "ku_gou")
data class KuGouEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    var token: String = "",
    var userid: Long = 0,
    @Column(length = 2000)
    var kuGoo: String? = "",
    var mid: String? = ""
)


interface KuGouDao: JPADao<KuGouEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): KuGouEntity?
}

@AutoBind
interface KuGouService{
    fun findByQqEntity(qqEntity: QqEntity): KuGouEntity?
    fun save(kuGouEntity: KuGouEntity)
    fun delete(kuGouEntity: KuGouEntity)
    fun findAll(): List<KuGouEntity>
}

class KuGouServiceImpl @Inject constructor(private val kuGouDao: KuGouDao): KuGouService{
    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): KuGouEntity? {
        return kuGouDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun save(kuGouEntity: KuGouEntity) {
        return kuGouDao.saveOrUpdate(kuGouEntity)
    }

    @Transactional
    override fun delete(kuGouEntity: KuGouEntity) {
        return kuGouDao.delete(kuGouEntity.id!!)
    }

    @Transactional
    override fun findAll(): List<KuGouEntity> {
        return kuGouDao.findAll()
    }
}
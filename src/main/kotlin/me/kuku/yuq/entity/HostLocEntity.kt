package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "host_loc")
data class HostLocEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 2000)
    var cookie: String = "",
    var sign: Boolean = false
){
    companion object{
        fun getInstance(qqEntity: QqEntity): HostLocEntity{
            return HostLocEntity(qqEntity = qqEntity)
        }

        fun getInstance(cookie: String): HostLocEntity{
            return HostLocEntity(cookie = cookie)
        }
    }
}

interface HostLocDao: JPADao<HostLocEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): HostLocEntity?
    fun findBySign(sign: Boolean): List<HostLocEntity>
}

@AutoBind
interface HostLocService{
    fun findByQqEntity(qqEntity: QqEntity): HostLocEntity?
    fun save(hostLocEntity: HostLocEntity)
    fun findAll(): List<HostLocEntity>
    fun findBySign(sign: Boolean): List<HostLocEntity>
    fun delete(entity: HostLocEntity)
}

class HostLocServiceImpl: HostLocService{

    @Inject
    private lateinit var hostLocDao: HostLocDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): HostLocEntity? {
        return hostLocDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun save(hostLocEntity: HostLocEntity) {
        return hostLocDao.saveOrUpdate(hostLocEntity)
    }

    @Transactional
    override fun findAll(): List<HostLocEntity> {
        return hostLocDao.findAll()
    }

    @Transactional
    override fun findBySign(sign: Boolean): List<HostLocEntity> {
        return hostLocDao.findBySign(sign)
    }

    @Transactional
    override fun delete(entity: HostLocEntity) {
        hostLocDao.delete(entity.id!!)
    }
}
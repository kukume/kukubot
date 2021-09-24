package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "ql")
data class QlEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    var url: String = "",
    var clientId: String = "",
    var clientSecret: String = ""
){
    companion object{
        fun getInstance(url: String, clientId: String, clientSecret: String): QlEntity{
            return QlEntity(null, url, clientId, clientSecret)
        }
    }
}

interface QlDao: JPADao<QlEntity, Int>

@AutoBind
interface QlService{
    fun findAll(): List<QlEntity>
    fun get(id: Int): QlEntity?
    fun save(qlEntity: QlEntity)
    fun delete(id: Int)
}

class QlServiceImpl: QlService{

    @Inject
    private lateinit var qlDao: QlDao

    @Transactional
    override fun findAll() = qlDao.findAll()

    @Transactional
    override fun get(id: Int) = qlDao.get(id)

    @Transactional
    override fun save(qlEntity: QlEntity) = qlDao.saveOrUpdate(qlEntity)

    @Transactional
    override fun delete(id: Int) = qlDao.delete(id)
}



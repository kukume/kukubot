package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "qq_video")
data class QqVideoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 1000)
    var cookie: String = "",
    @Column(length = 1000)
    var vuSession: String = "",
    @Column(length = 1000)
    var accessToken: String = ""
){
    companion object{
        fun getInstance(qqEntity: QqEntity): QqVideoEntity{
            return QqVideoEntity(qqEntity = qqEntity)
        }
        fun getInstance(cookie: String): QqVideoEntity{
            return QqVideoEntity(cookie = cookie)
        }
    }
}

interface QqVideoDao: JPADao<QqVideoEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): QqVideoEntity?
}

@AutoBind
interface QqVideoService{
    fun findByQqEntity(qqEntity: QqEntity): QqVideoEntity?
    fun findAll(): List<QqVideoEntity>
    fun save(qqVideoEntity: QqVideoEntity)
}

class QqVideoServiceImpl: QqVideoService{

    @Inject
    private lateinit var qqVideoDao: QqVideoDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): QqVideoEntity? {
        return qqVideoDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun findAll(): List<QqVideoEntity> {
        return qqVideoDao.findAll()
    }

    @Transactional
    override fun save(qqVideoEntity: QqVideoEntity) {
        return qqVideoDao.saveOrUpdate(qqVideoEntity)
    }
}
package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.alibaba.fastjson.annotation.JSONField
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "net_ease")
data class NetEaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 1000)
    var musicU: String = "",
    @Column(length = 1000)
    var csrf: String = ""
){
    companion object{
        fun getInstance(qqEntity: QqEntity): NetEaseEntity{
            return NetEaseEntity(qqEntity = qqEntity)
        }
        fun getInstance(musicU: String, csrf: String): NetEaseEntity{
            return NetEaseEntity(musicU = musicU, csrf = csrf)
        }
    }

    @JSONField(serialize = false)
    fun getCookie(): String {
        return "os=pc; osver=Microsoft-Windows-10-Professional-build-10586-64bit; appver=2.0.3.131777; channel=netease; __remember_me=true; MUSIC_U=$musicU; __csrf=$csrf; "
    }
}

interface NetEaseDao: JPADao<NetEaseEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): NetEaseEntity?
}

@AutoBind
interface NetEaseService{
    fun findByQqEntity(qqEntity: QqEntity): NetEaseEntity?
    fun findAll(): List<NetEaseEntity>
    fun save(entity: NetEaseEntity)
}

class NetEaseServiceImpl: NetEaseService{

    @Inject
    private lateinit var netEaseDao: NetEaseDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): NetEaseEntity? {
        return netEaseDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun findAll(): List<NetEaseEntity> {
        return netEaseDao.findAll()
    }

    @Transactional
    override fun save(entity: NetEaseEntity) {
        return netEaseDao.saveOrUpdate(entity)
    }
}
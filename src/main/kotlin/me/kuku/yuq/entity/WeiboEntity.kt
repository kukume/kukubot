package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "weibo")
data class WeiboEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 2000)
    var pcCookie: String = "",
    @Column(length = 2000)
    var mobileCookie: String = "",
    var monitor: Boolean = false
){
    companion object{
        fun getInstance(qqEntity: QqEntity): WeiboEntity{
            return WeiboEntity(qqEntity = qqEntity)
        }
        fun getInstance(pcCookie: String, mobileCookie: String): WeiboEntity{
            return WeiboEntity(pcCookie = pcCookie, mobileCookie = mobileCookie)
        }
    }
}

interface WeiboDao: JPADao<WeiboEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): WeiboEntity?
    fun findByMonitor(monitor: Boolean): List<WeiboEntity>
}

@AutoBind
interface WeiboService{
    fun findByQqEntity(qqEntity: QqEntity): WeiboEntity?
    fun findByMonitor(monitor: Boolean): List<WeiboEntity>
    fun save(weiboEntity: WeiboEntity)
}

class WeiboServiceImpl: WeiboService {

    @Inject
    private lateinit var weiboDao: WeiboDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): WeiboEntity? {
        return weiboDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun findByMonitor(monitor: Boolean): List<WeiboEntity> {
        return weiboDao.findByMonitor(monitor)
    }

    @Transactional
    override fun save(weiboEntity: WeiboEntity) {
        return weiboDao.saveOrUpdate(weiboEntity)
    }
}

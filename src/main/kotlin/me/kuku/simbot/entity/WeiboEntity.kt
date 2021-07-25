package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
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

interface WeiboRepository: JpaRepository<WeiboEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): WeiboEntity?
    fun findByMonitor(monitor: Boolean): List<WeiboEntity>
}


interface WeiboService{
    fun findByQqEntity(qqEntity: QqEntity): WeiboEntity?
    fun findByMonitor(monitor: Boolean): List<WeiboEntity>
    fun save(weiboEntity: WeiboEntity): WeiboEntity
}

@Service
class WeiboServiceImpl: WeiboService{
    
    @Resource
    private lateinit var weiboRepository: WeiboRepository

    override fun findByQqEntity(qqEntity: QqEntity): WeiboEntity? {
        return weiboRepository.findByQqEntity(qqEntity)
    }

    override fun findByMonitor(monitor: Boolean): List<WeiboEntity> {
        return weiboRepository.findByMonitor(monitor)
    }

    override fun save(weiboEntity: WeiboEntity): WeiboEntity {
        return weiboRepository.save(weiboEntity)
    }
}
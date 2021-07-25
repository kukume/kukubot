package me.kuku.simbot.entity

import com.alibaba.fastjson.annotation.JSONField
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
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

interface NetEaseRepository: JpaRepository<NetEaseEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): NetEaseEntity?
}

interface NetEaseService{
    fun findByQqEntity(qqEntity: QqEntity): NetEaseEntity?
    fun findAll(): List<NetEaseEntity>
    fun save(entity: NetEaseEntity): NetEaseEntity
}

@Service
class NetEaseServiceImpl: NetEaseService{

    @Resource
    private lateinit var netEaseRepository: NetEaseRepository

    override fun findByQqEntity(qqEntity: QqEntity): NetEaseEntity? {
        return netEaseRepository.findByQqEntity(qqEntity)
    }

    override fun findAll(): List<NetEaseEntity> {
        return netEaseRepository.findAll()
    }

    override fun save(entity: NetEaseEntity): NetEaseEntity {
        return netEaseRepository.save(entity)
    }
}
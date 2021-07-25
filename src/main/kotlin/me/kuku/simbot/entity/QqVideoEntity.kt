package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
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

interface QqVideoRepository: JpaRepository<QqVideoEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): QqVideoEntity?
}

interface QqVideoService{
    fun findByQqEntity(qqEntity: QqEntity): QqVideoEntity?
    fun findAll(): List<QqVideoEntity>
    fun save(qqVideoEntity: QqVideoEntity): QqVideoEntity
}

@Service
class QqVideoServiceImpl: QqVideoService{

    @Resource
    private lateinit var qqVideoRepository: QqVideoRepository

    override fun findByQqEntity(qqEntity: QqEntity): QqVideoEntity? {
        return qqVideoRepository.findByQqEntity(qqEntity)
    }

    override fun findAll(): MutableList<QqVideoEntity> {
        return qqVideoRepository.findAll()
    }

    override fun save(qqVideoEntity: QqVideoEntity): QqVideoEntity {
        return qqVideoRepository.save(qqVideoEntity)
    }
}
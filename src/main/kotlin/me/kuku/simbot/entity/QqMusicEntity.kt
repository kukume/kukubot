package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "qq_music")
data class QqMusicEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 1000)
    var cookie: String = ""
)

interface QqMusicRepository: JpaRepository<QqMusicEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity?
}

interface QqMusicService {
    fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity?
    fun save(qqMusicEntity: QqMusicEntity): QqMusicEntity
    fun delete(qqMusicEntity: QqMusicEntity)
    fun findAll(): List<QqMusicEntity>
}


@Service
class QqMusicServiceImpl: QqMusicService{

    @Resource
    private lateinit var qqMusicRepository: QqMusicRepository

    override fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity? {
        return qqMusicRepository.findByQqEntity(qqEntity)
    }

    override fun save(qqMusicEntity: QqMusicEntity): QqMusicEntity {
        return qqMusicRepository.save(qqMusicEntity)
    }

    override fun delete(qqMusicEntity: QqMusicEntity) {
        qqMusicRepository.delete(qqMusicEntity)
    }

    override fun findAll(): List<QqMusicEntity> {
        return qqMusicRepository.findAll()
    }
}
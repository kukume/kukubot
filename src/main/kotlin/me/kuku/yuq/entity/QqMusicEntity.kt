package me.kuku.yuq.entity

import me.kuku.utils.OkUtils
import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "qq_music")
@NamedEntityGraph(name = "qq_music_find_all", attributeNodes = [NamedAttributeNode("qqEntity")])
class QqMusicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 1000)
    var cookie: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "text")
    var config: QqMusicConfig = QqMusicConfig()

    @Transient
    var qqMusicKey: String = OkUtils.cookie(cookie, "qqmusic_key") ?: ""
}

interface QqMusicRepository: JpaRepository<QqMusicEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity?

    @EntityGraph(value = "qq_music_find_all", type = EntityGraph.EntityGraphType.FETCH)
    override fun findAll(): MutableList<QqMusicEntity>
}

@Service
class QqMusicService(
    private val qqMusicRepository: QqMusicRepository
) {

    fun findByQqEntity(qqEntity: QqEntity) = qqMusicRepository.findByQqEntity(qqEntity)

    fun save(qqMusicEntity: QqMusicEntity): QqMusicEntity = qqMusicRepository.save(qqMusicEntity)

    fun findAll(): List<QqMusicEntity> = qqMusicRepository.findAll()
}


data class QqMusicConfig(var comment: Status = Status.OFF, var view: Status = Status.ON, var password: String = "")
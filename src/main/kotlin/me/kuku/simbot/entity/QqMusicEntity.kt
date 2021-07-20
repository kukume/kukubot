package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
@Table(name = "qq_muisic")
data class QqMusicEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 1000)
    var cookie: String? = null
)

interface QqMusicRepository: JpaRepository<QqMusicEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity?
}


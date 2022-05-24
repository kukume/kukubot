package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "ku_gou")
class KuGouEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    var token: String = ""
    var userid: Long = 0
    @Column(length = 2000)
    var kuGoo: String = ""
    var mid: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "text")
    var config: KuGouConfig = KuGouConfig()
}

interface KuGouRepository: JpaRepository<KuGouEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): KuGouEntity?
}

@Service
class KuGouService (
    private val kuGouRepository: KuGouRepository
) {

    fun save(kuGouEntity: KuGouEntity): KuGouEntity = kuGouRepository.save(kuGouEntity)

    fun findByQqEntity(qqEntity: QqEntity) = kuGouRepository.findByQqEntity(qqEntity)

    fun findAll(): List<KuGouEntity> = kuGouRepository.findAll()

}

data class KuGouConfig(var sign: Status = Status.OFF)
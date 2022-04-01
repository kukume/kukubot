package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
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
}

interface KuGouRepository: JpaRepository<KuGouEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): KuGouEntity?
}


class KuGouService @Inject constructor(
    private val kuGouRepository: KuGouRepository
) {

    fun save(kuGouEntity: KuGouEntity): KuGouEntity = kuGouRepository.save(kuGouEntity)

    fun findByQqEntity(qqEntity: QqEntity) = kuGouRepository.findByQqEntity(qqEntity)

}
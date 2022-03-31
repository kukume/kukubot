package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "net_ease")
class NetEaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 2000)
    var musicU: String = ""
    @Column(length = 2000)
    var csrf: String = ""
}

interface NetEaseRepository: JpaRepository<NetEaseEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): NetEaseEntity?
}

class NetEaseService @Inject constructor(
    private val netEaseRepository: NetEaseRepository
) {
    fun save(netEaseEntity: NetEaseEntity): NetEaseEntity = netEaseRepository.save(netEaseEntity)

    fun findByQqEntity(qqEntity: QqEntity) = netEaseRepository.findByQqEntity(qqEntity)
}
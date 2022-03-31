package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "host_loc")
class HostLocEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 1000)
    var cookie: String = ""
}

interface HostLocRepository: JpaRepository<HostLocEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): HostLocEntity?
}


class HostLocService @Inject constructor(
    private val hostLocRepository: HostLocRepository
) {

    fun save(hostLocEntity: HostLocEntity): HostLocEntity = hostLocRepository.save(hostLocEntity)

    fun findByQqEntity(qqEntity: QqEntity) = hostLocRepository.findByQqEntity(qqEntity)
}
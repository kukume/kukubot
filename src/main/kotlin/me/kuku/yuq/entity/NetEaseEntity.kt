package me.kuku.yuq.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "net_ease")
@TypeDef(name = "json", typeClass = JsonType::class)
class NetEaseEntity: BaseEntity() {
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
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: NetEaseConfig = NetEaseConfig()

    fun cookie() = "os=pc; osver=Microsoft-Windows-10-Professional-build-10586-64bit; appver=2.0.3.131777; channel=netease; __remember_me=true; MUSIC_U=$musicU; __csrf=$csrf; "
}

interface NetEaseRepository: JpaRepository<NetEaseEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): NetEaseEntity?
}

class NetEaseService @Inject constructor(
    private val netEaseRepository: NetEaseRepository
) {
    fun save(netEaseEntity: NetEaseEntity): NetEaseEntity = netEaseRepository.save(netEaseEntity)

    fun findByQqEntity(qqEntity: QqEntity) = netEaseRepository.findByQqEntity(qqEntity)

    fun findAll(): List<NetEaseEntity> = netEaseRepository.findAll()
}

data class NetEaseConfig(var sign: Status = Status.OFF)
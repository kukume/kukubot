package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "mi_ho_yo")
class MiHoYoEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 2000)
    var cookie: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: MiHoYoConfig = MiHoYoConfig()
}

interface MiHoYoRepository: JpaRepository<MiHoYoEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): MiHoYoEntity?
}

@Service
class MiHoYoService (
    private val miHoYoRepository: MiHoYoRepository
) {
    fun save(miHoYoEntity: MiHoYoEntity): MiHoYoEntity = miHoYoRepository.save(miHoYoEntity)

    fun findByQqEntity(qqEntity: QqEntity) = miHoYoRepository.findByQqEntity(qqEntity)

    fun findAll(): List<MiHoYoEntity> = miHoYoRepository.findAll()
}

data class MiHoYoConfig(var sign: Status = Status.OFF)
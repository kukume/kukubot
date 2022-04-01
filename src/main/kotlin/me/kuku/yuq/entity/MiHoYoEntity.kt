package me.kuku.yuq.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "mi_ho_yo")
@TypeDef(name = "json", typeClass = JsonType::class)
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

class MiHoYoService @Inject constructor(
    private val miHoYoRepository: MiHoYoRepository
) {
    fun save(miHoYoEntity: MiHoYoEntity): MiHoYoEntity = miHoYoRepository.save(miHoYoEntity)

    fun findByQqEntity(qqEntity: QqEntity) = miHoYoRepository.findByQqEntity(qqEntity)

    fun findAll(): List<MiHoYoEntity> = miHoYoRepository.findAll()
}

data class MiHoYoConfig(var sign: Status = Status.OFF)
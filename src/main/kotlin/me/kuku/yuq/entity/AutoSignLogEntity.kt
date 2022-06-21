package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "auto_sign_log")
class AutoSignLogEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @ManyToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    var autoSignType: AutoSignType = AutoSignType.Baidu
    @Type(type = "json")
    @Column(columnDefinition = "varchar(1000)")
    var result: Any = Any()
}


enum class AutoSignType {
    Baidu
}

interface AutoSignLogRepository: JpaRepository<AutoSignLogEntity, Int> {
    fun findByAutoSignTypeAndQqEntity(autoSignType: AutoSignType, qqEntity: QqEntity): List<AutoSignLogEntity>
}

@Service
class AutoSignLogService(
    private val autoSignLogRepository: AutoSignLogRepository
) {

    fun save(autoSignLogEntity: AutoSignLogEntity) = autoSignLogRepository.save(autoSignLogEntity)

    fun findByAutoSignTypeAndQqEntity(autoSignType: AutoSignType, qqEntity: QqEntity) =
        autoSignLogRepository.findByAutoSignTypeAndQqEntity(autoSignType, qqEntity)

}
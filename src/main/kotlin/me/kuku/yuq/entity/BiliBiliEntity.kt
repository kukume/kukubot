package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "bili_bili")
class BiliBiliEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity = QqEntity()
    @Column(length = 2000)
    var cookie: String = ""
    var userid: String = ""
    var token: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: BiliBiliConfig = BiliBiliConfig()
}

interface BiliBiliRepository: JpaRepository<BiliBiliEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): BiliBiliEntity?
}

class BiliBiliService @Inject constructor(
    private val biliBiliRepository: BiliBiliRepository
) {
    fun findByQqEntity(qqEntity: QqEntity) = biliBiliRepository.findByQqEntity(qqEntity)

    fun save(biliEntity: BiliBiliEntity): BiliBiliEntity = biliBiliRepository.save(biliEntity)

    fun findAll(): List<BiliBiliEntity> = biliBiliRepository.findAll()

}

data class BiliBiliConfig(var push: Status = Status.OFF, var sign: Status = Status.OFF, var live: Status = Status.OFF)
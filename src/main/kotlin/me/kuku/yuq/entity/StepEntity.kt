package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "step")
class StepEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 5000)
    var leXinCookie: String = ""
    var leXinUserid: String = ""
    @Column(length = 5000)
    var leXinAccessToken: String = ""
    @Column(length = 1000)
    var miLoginToken: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: StepConfig = StepConfig()
}

interface StepRepository: JpaRepository<StepEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): StepEntity?
}

class StepService @Inject constructor(
    private val stepRepository: StepRepository
) {

    fun save(stepEntity: StepEntity): StepEntity = stepRepository.save(stepEntity)

    fun findAll(): List<StepEntity> = stepRepository.findAll()

    fun findByQqEntity(qqEntity: QqEntity) = stepRepository.findByQqEntity(qqEntity)
}

class StepConfig(var step: Int = -1)
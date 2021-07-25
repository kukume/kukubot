package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "step")
data class StepEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,

    var leXinPhone: String = "",
    var leXinPassword: String = "",
    @Column(length = 5000)
    var leXinCookie: String = "",
    var leXinUserid: String = "",
    @Column(length = 5000)
    var leXinAccessToken: String = "",

    var miPhone: String = "",
    var miPassword: String = "",
    var miLoginToken: String = "",

    var step: Int = -1
){
    companion object{
        fun getInstance(qqEntity: QqEntity): StepEntity{
            return StepEntity(qqEntity = qqEntity)
        }
        fun getInstance(leXinPhone: String, leXinPassword: String, leXinCookie: String, leXinUserid: String, leXinAccessToken: String): StepEntity{
            return StepEntity(leXinPhone = leXinPhone, leXinPassword = leXinPassword, leXinCookie = leXinCookie,
                leXinUserid = leXinUserid, leXinAccessToken = leXinAccessToken)
        }
        fun getInstance(miPhone: String, miPassword: String, miLoginToken: String): StepEntity{
            return StepEntity(miPhone = miPhone, miPassword = miPassword, miLoginToken = miLoginToken)
        }
    }
}


interface StepRepository: JpaRepository<StepEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): StepEntity?
}

interface StepService{
    fun findByQqEntity(qqEntity: QqEntity): StepEntity?
    fun save(stepEntity: StepEntity): StepEntity
}

@Service
class StepServiceImpl: StepService{

    @Resource
    private lateinit var stepRepository: StepRepository

    override fun findByQqEntity(qqEntity: QqEntity): StepEntity? {
        return stepRepository.findByQqEntity(qqEntity)
    }

    override fun save(stepEntity: StepEntity): StepEntity {
        return stepRepository.save(stepEntity)
    }
}
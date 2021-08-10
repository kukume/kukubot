package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
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
    @Column(length = 1000)
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


interface StepDao: JPADao<StepEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): StepEntity?
}

@AutoBind
interface StepService{
    fun findByQqEntity(qqEntity: QqEntity): StepEntity?
    fun save(stepEntity: StepEntity)
}

class StepServiceImpl: StepService{

    @Inject
    private lateinit var stepDao: StepDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): StepEntity? {
        return stepDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun save(stepEntity: StepEntity) {
        return stepDao.saveOrUpdate(stepEntity)
    }
}
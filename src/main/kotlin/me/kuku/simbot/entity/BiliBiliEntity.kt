package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "bili_bili")
data class BiliBiliEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 2000)
    var cookie: String = "",
    var userid: String = "",
    var token: String = "",
    var monitor: Boolean = false,
    @Column(name = "task_")
    var task: Boolean = false,
    var live: Boolean = false
){

    companion object{
        fun getInstance(qqEntity: QqEntity): BiliBiliEntity{
            return BiliBiliEntity(qqEntity = qqEntity)
        }
        fun getInstance(cookie: String, userid: String, token: String): BiliBiliEntity{
            return BiliBiliEntity(cookie = cookie, userid = userid, token = token)
        }
    }
}

interface BiliBiliRepository: JpaRepository<BiliBiliEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): BiliBiliEntity?
    fun findByMonitor(monitor: Boolean): List<BiliBiliEntity>
    fun findByTask(task: Boolean): List<BiliBiliEntity>
    fun findByLive(live: Boolean): List<BiliBiliEntity>
}

interface BiliBiliService {
    fun findByQqEntity(qqEntity: QqEntity): BiliBiliEntity?
    fun save(entity: BiliBiliEntity): BiliBiliEntity
    fun findAll(): List<BiliBiliEntity>
    fun findByMonitor(monitor: Boolean): List<BiliBiliEntity>
    fun findByTask(task: Boolean): List<BiliBiliEntity>
    fun findByLive(live: Boolean): List<BiliBiliEntity>
}

@Service
class BiliBiliServiceImpl: BiliBiliService{

    @Resource
    private lateinit var biliBiliRepository: BiliBiliRepository

    override fun findByQqEntity(qqEntity: QqEntity): BiliBiliEntity? {
        return biliBiliRepository.findByQqEntity(qqEntity)
    }

    override fun save(entity: BiliBiliEntity): BiliBiliEntity {
        return biliBiliRepository.save(entity)
    }

    override fun findAll(): List<BiliBiliEntity> {
        return biliBiliRepository.findAll()
    }

    override fun findByMonitor(monitor: Boolean): List<BiliBiliEntity> {
        return biliBiliRepository.findByMonitor(monitor)
    }

    override fun findByTask(task: Boolean): List<BiliBiliEntity> {
        return biliBiliRepository.findByTask(task)
    }

    override fun findByLive(live: Boolean): List<BiliBiliEntity> {
        return biliBiliRepository.findByLive(live)
    }
}
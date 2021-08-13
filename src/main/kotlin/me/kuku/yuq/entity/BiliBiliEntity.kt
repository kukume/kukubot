package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
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

interface BiliBiliDao: JPADao<BiliBiliEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): BiliBiliEntity?
    fun findByMonitor(monitor: Boolean): List<BiliBiliEntity>
    fun findByTask(task: Boolean): List<BiliBiliEntity>
    fun findByLive(live: Boolean): List<BiliBiliEntity>
}

@AutoBind
interface BiliBiliService {
    fun findByQqEntity(qqEntity: QqEntity): BiliBiliEntity?
    fun save(entity: BiliBiliEntity)
    fun findAll(): List<BiliBiliEntity>
    fun findByMonitor(monitor: Boolean): List<BiliBiliEntity>
    fun findByTask(task: Boolean): List<BiliBiliEntity>
    fun findByLive(live: Boolean): List<BiliBiliEntity>
    fun delete(biliEntity: BiliBiliEntity)
}

class BiliBiliServiceImpl: BiliBiliService{

    @Inject
    private lateinit var biliBiliDao: BiliBiliDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): BiliBiliEntity? {
        return biliBiliDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun save(entity: BiliBiliEntity) {
        return biliBiliDao.saveOrUpdate(entity)
    }

    @Transactional
    override fun findAll(): List<BiliBiliEntity> {
        return biliBiliDao.findAll()
    }

    @Transactional
    override fun findByMonitor(monitor: Boolean): List<BiliBiliEntity> {
        return biliBiliDao.findByMonitor(monitor)
    }

    @Transactional
    override fun findByTask(task: Boolean): List<BiliBiliEntity> {
        return biliBiliDao.findByTask(task)
    }

    @Transactional
    override fun findByLive(live: Boolean): List<BiliBiliEntity> {
        return biliBiliDao.findByLive(live)
    }

    override fun delete(biliEntity: BiliBiliEntity) {
        biliBiliDao.delete(biliEntity.id!!)
    }
}
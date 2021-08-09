package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "hey_tap")
data class HeyTapEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 2000)
    var cookie: String = "",
    var earlyToBedClock: Boolean? = false
){
    companion object{
        fun getInstance(qqEntity: QqEntity): HeyTapEntity{
            return HeyTapEntity(qqEntity = qqEntity);
        }
    }
}

interface HeyTapDao: JPADao<HeyTapEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): HeyTapEntity?
    fun findByEarlyToBedClock(earlyToBedClock: Boolean): List<HeyTapEntity>
}

@AutoBind
interface HeyTapService{
    fun findByQqEntity(qqEntity: QqEntity): HeyTapEntity?
    fun save(heyTapEntity: HeyTapEntity)
    fun findAll(): List<HeyTapEntity>
    fun delete(heyTapEntity: HeyTapEntity)
    fun findByEarlyToBedClock(earlyToBedClock: Boolean): List<HeyTapEntity>
}

class HeyTapServiceImpl: HeyTapService{

    @Inject
    private lateinit var heyTapDao: HeyTapDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): HeyTapEntity? {
        return heyTapDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun save(heyTapEntity: HeyTapEntity) {
        return heyTapDao.saveOrUpdate(heyTapEntity)
    }

    @Transactional
    override fun findAll(): List<HeyTapEntity> {
        return heyTapDao.findAll()
    }

    @Transactional
    override fun delete(heyTapEntity: HeyTapEntity) {
        return heyTapDao.delete(heyTapEntity.id!!)
    }

    @Transactional
    override fun findByEarlyToBedClock(earlyToBedClock: Boolean) = heyTapDao.findByEarlyToBedClock(earlyToBedClock)
}
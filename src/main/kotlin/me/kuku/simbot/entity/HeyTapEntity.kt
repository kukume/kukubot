package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
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

interface HeyTapRepository: JpaRepository<HeyTapEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): HeyTapEntity?
    fun findByEarlyToBedClock(earlyToBedClock: Boolean): List<HeyTapEntity>
}

interface HeyTapService{
    fun findByQqEntity(qqEntity: QqEntity): HeyTapEntity?
    fun save(heyTapEntity: HeyTapEntity): HeyTapEntity
    fun findAll(): List<HeyTapEntity>
    fun delete(heyTapEntity: HeyTapEntity)
    fun findByEarlyToBedClock(earlyToBedClock: Boolean): List<HeyTapEntity>
}

@Service
class HeyTapServiceImpl: HeyTapService{

    @Resource
    private lateinit var heyTapRepository: HeyTapRepository

    override fun findByQqEntity(qqEntity: QqEntity): HeyTapEntity? {
        return heyTapRepository.findByQqEntity(qqEntity)
    }

    override fun save(heyTapEntity: HeyTapEntity): HeyTapEntity {
        return heyTapRepository.save(heyTapEntity)
    }

    override fun findAll(): List<HeyTapEntity> {
        return heyTapRepository.findAll()
    }

    override fun delete(heyTapEntity: HeyTapEntity) {
        return heyTapRepository.delete(heyTapEntity)
    }

    override fun findByEarlyToBedClock(earlyToBedClock: Boolean) = heyTapRepository.findByEarlyToBedClock(earlyToBedClock)
}
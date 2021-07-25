package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "host_loc")
data class HostLocEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 2000)
    var cookie: String = "",
    var sign: Boolean = false,
    var monitor: Boolean = false
){
    companion object{
        fun getInstance(qqEntity: QqEntity): HostLocEntity{
            return HostLocEntity(qqEntity = qqEntity)
        }

        fun getInstance(cookie: String): HostLocEntity{
            return HostLocEntity(cookie = cookie)
        }
    }
}

interface HostLocRepository: JpaRepository<HostLocEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): HostLocEntity?
    fun findByMonitor(monitor: Boolean): List<HostLocEntity>
    fun findBySign(sign: Boolean): List<HostLocEntity>
}

interface HostLocService{
    fun findByQqEntity(qqEntity: QqEntity): HostLocEntity?
    fun save(hostLocEntity: HostLocEntity): HostLocEntity
    fun findAll(): List<HostLocEntity>
    fun findByMonitor(monitor: Boolean): List<HostLocEntity>
    fun findBySign(sign: Boolean): List<HostLocEntity>
    fun delete(entity: HostLocEntity)
}

@Service
class HostLocServiceImpl: HostLocService{

    @Resource
    private lateinit var hostLocRepository: HostLocRepository

    override fun findByQqEntity(qqEntity: QqEntity): HostLocEntity? {
        return hostLocRepository.findByQqEntity(qqEntity)
    }

    override fun save(hostLocEntity: HostLocEntity): HostLocEntity {
        return hostLocRepository.save(hostLocEntity)
    }

    override fun findAll(): List<HostLocEntity> {
        return hostLocRepository.findAll()
    }

    override fun findByMonitor(monitor: Boolean): List<HostLocEntity> {
        return hostLocRepository.findByMonitor(monitor)
    }

    override fun findBySign(sign: Boolean): List<HostLocEntity> {
        return hostLocRepository.findBySign(sign)
    }

    override fun delete(entity: HostLocEntity) {
        hostLocRepository.delete(entity)
    }
}
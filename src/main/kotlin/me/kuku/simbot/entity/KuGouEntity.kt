package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "ku_gou")
data class KuGouEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    var token: String = "",
    var userid: Long = 0
)


interface KuGouRepository: JpaRepository<KuGouEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): KuGouEntity?
}

interface KuGouService{
    fun findByQqEntity(qqEntity: QqEntity): KuGouEntity?
    fun save(kuGouEntity: KuGouEntity): KuGouEntity
    fun delete(kuGouEntity: KuGouEntity)
    fun findAll(): List<KuGouEntity>
}

@Service
class KuGouServiceImpl(private val kuGouRepository: KuGouRepository): KuGouService{
    override fun findByQqEntity(qqEntity: QqEntity): KuGouEntity? {
        return kuGouRepository.findByQqEntity(qqEntity)
    }

    override fun save(kuGouEntity: KuGouEntity): KuGouEntity {
        return kuGouRepository.save(kuGouEntity)
    }

    override fun delete(kuGouEntity: KuGouEntity) {
        return kuGouRepository.delete(kuGouEntity)
    }

    override fun findAll(): List<KuGouEntity> {
        return kuGouRepository.findAll()
    }
}
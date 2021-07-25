package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "qq")
data class QqEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @Column(unique = true)
    var qq: Long = 0L,
    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(name = "qq_group", joinColumns = [JoinColumn(name = "qq_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")])
    var groups: Set<GroupEntity> = hashSetOf()
){
    companion object{
        fun getInstance(qq: Long): QqEntity{
            return QqEntity(qq = qq)
        }
    }

    fun getGroup(group: Long): GroupEntity?{
        for (groupEntity in groups){
            if (group == groupEntity.group) return groupEntity
        }
        return null
    }

    override fun toString(): String {
        return "";
    }

    override fun hashCode(): Int {
        return 0
    }
}

interface QqRepository: JpaRepository<QqEntity, Int>{
    fun findByQq(qq: Long): QqEntity?
}

interface QqService{
    fun findByQq(qq: Long): QqEntity?
    fun save(entity: QqEntity): QqEntity
    fun delete(entity: QqEntity)
}

@Service
class QqServiceImpl: QqService{
    @Resource
    private lateinit var qqRepository: QqRepository

    override fun findByQq(qq: Long) = qqRepository.findByQq(qq)

    override fun save(entity: QqEntity): QqEntity = qqRepository.save(entity)

    override fun delete(entity: QqEntity) = qqRepository.delete(entity)
}

package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
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

interface QqDao: JPADao<QqEntity, Int>{
    fun findByQq(qq: Long): QqEntity?
}

@AutoBind
interface QqService{
    fun findByQq(qq: Long): QqEntity?
    fun save(qqEntity: QqEntity)
    fun delete(qqEntity: QqEntity)
}

class QqServiceImpl: QqService{

    @Inject
    private lateinit var qqDao: QqDao

    @Transactional
    override fun findByQq(qq: Long) = qqDao.findByQq(qq)

    @Transactional
    override fun save(qqEntity: QqEntity) = qqDao.saveOrUpdate(qqEntity)

    @Transactional
    override fun delete(qqEntity: QqEntity) = qqDao.delete(qqEntity.id!!)
}
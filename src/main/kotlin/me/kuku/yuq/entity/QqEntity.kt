package me.kuku.yuq.entity

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Type
import org.springframework.cache.annotation.CachePut
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "qq")
@JsonIgnoreProperties("groups")
@NamedEntityGraph(name = "groups", attributeNodes = [NamedAttributeNode("groups")])
@NamedEntityGraph(name = "queryAll", attributeNodes = [
    NamedAttributeNode("baiduEntity"),
    NamedAttributeNode("biliBiliEntity"),
    NamedAttributeNode("hostLocEntity"),
    NamedAttributeNode("kuGouEntity"),
    NamedAttributeNode("miHoYoEntity"),
    NamedAttributeNode("netEaseEntity"),
    NamedAttributeNode("oppoShopEntity"),
    NamedAttributeNode("stepEntity"),
    NamedAttributeNode("weiboEntity"),
    NamedAttributeNode("douYuEntity"),
    NamedAttributeNode("huYaEntity")
])
open class QqEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Int? = null
    @Column(unique = true)
    open var qq: Long = 0
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(name = "qq_group", joinColumns = [JoinColumn(name = "qq_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")])
    @JSONField(serialize = false)
    open var groups: MutableSet<GroupEntity> = linkedSetOf()
    @Type(type = "json")
    @Column(columnDefinition = "json")
    open var config: QqConfig = QqConfig()

    @OneToOne(mappedBy = "qqEntity")
    open var baiduEntity: BaiduEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var biliBiliEntity: BiliBiliEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var hostLocEntity: HostLocEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var kuGouEntity: KuGouEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var miHoYoEntity: MiHoYoEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var netEaseEntity: NetEaseEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var oppoShopEntity: OppoShopEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var stepEntity: StepEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var weiboEntity: WeiboEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var douYuEntity: DouYuEntity? = null
    @OneToOne(mappedBy = "qqEntity")
    open var huYaEntity: HuYaEntity? = null

    fun get(group: Long): GroupEntity? {
        for (groupEntity in groups) {
            if (groupEntity.group == group) return groupEntity
        }
        return null
    }
}

interface QqRepository: JpaRepository<QqEntity, Int> {
    @EntityGraph(value = "groups", type = EntityGraph.EntityGraphType.FETCH)
    fun findByQq(qq: Long): QqEntity?

    @EntityGraph(value = "queryAll", type = EntityGraph.EntityGraphType.FETCH)
    fun findByQqOrderById(qq: Long): QqEntity?
}

@Service
class QqService (
    private val qqRepository: QqRepository
){
    @CachePut(value = ["database"], key = "'qq' + #qqEntity.qq")
    fun save(qqEntity: QqEntity): QqEntity = qqRepository.save(qqEntity)

    @org.springframework.cache.annotation.Cacheable(value = ["database"], key = "'qq' + #qq")
    fun findByQq(qq: Long): QqEntity? = qqRepository.findByQq(qq)

    fun findAll(): MutableList<QqEntity> = qqRepository.findAll()

    fun findByQqOrderById(qq: Long) = qqRepository.findByQqOrderById(qq)
}

class QqConfig {
}
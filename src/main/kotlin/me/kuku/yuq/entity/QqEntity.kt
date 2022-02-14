package me.kuku.yuq.entity

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "qq")
@TypeDef(name = "json", typeClass = JsonType::class)
@JsonIgnoreProperties("groups")
open class QqEntity {
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

    fun get(group: Long): GroupEntity? {
        for (groupEntity in groups) {
            if (groupEntity.group == group) return groupEntity
        }
        return null
    }
}

interface QqRepository: JpaRepository<QqEntity, Int> {
    fun findByQq(qq: Long): QqEntity?
}

class QqService @Inject constructor(
    private val qqRepository: QqRepository
){
    fun save(qqEntity: QqEntity): QqEntity = qqRepository.save(qqEntity)

    fun findByQq(qq: Long): QqEntity? = qqRepository.findByQq(qq)

    fun findAll(): MutableList<QqEntity> = qqRepository.findAll()
}

class QqConfig {
}
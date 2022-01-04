package me.kuku.yuq.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name=  "group_")
@TypeDef(name = "json", typeClass = JsonType::class)
class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(unique = true, name = "group_")
    var group: Long = 0
    @ManyToMany(cascade = [CascadeType.ALL], mappedBy = "groups")
    var qqs: MutableSet<QqEntity> = linkedSetOf()
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: GroupConfig = GroupConfig()
}

interface GroupRepository: JpaRepository<GroupEntity, Int> {
    fun findByGroup(group: Long): GroupEntity?
    fun deleteByGroup(group: Long)
}

class GroupService @Inject constructor(
    private val groupRepository: GroupRepository
){
    fun save(groupEntity: GroupEntity): GroupEntity = groupRepository.save(groupEntity)

    fun findByGroup(group: Long) = groupRepository.findByGroup(group)

    fun findAll(): MutableList<GroupEntity> = groupRepository.findAll()

    fun deleteByGroup(group: Long) = groupRepository.deleteByGroup(group)
}

class GroupConfig{
    var repeat: Status = Status.OFF
    var locPush: Status = Status.OFF
}

enum class Status {
    ON,OFF;
}
package me.kuku.yuq.entity

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.querydsl.core.BooleanBuilder
import com.vladmihalcea.hibernate.type.json.JsonType
import me.kuku.yuq.utils.plus
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name=  "group_")
@TypeDef(name = "json", typeClass = JsonType::class)
@JsonIgnoreProperties("qqs")
class GroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(unique = true, name = "group_")
    var group: Long = 0
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "groups")
    @JSONField(serialize = false)
    var qqs: MutableSet<QqEntity> = linkedSetOf()
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: GroupConfig = GroupConfig()

    fun get(qq: Long): QqEntity? {
        for (qqEntity in qqs) {
            if (qqEntity.qq == qq)
                return qqEntity
        }
        return null
    }
}

interface GroupRepository: JpaRepository<GroupEntity, Int>, QuerydslPredicateExecutor<GroupEntity> {
    fun findByGroup(group: Long): GroupEntity?
    fun deleteByGroup(group: Long)
}

class GroupService @Inject constructor(
    private val groupRepository: GroupRepository
){
    fun save(groupEntity: GroupEntity): GroupEntity = groupRepository.save(groupEntity)

    fun findByGroup(group: Long) = groupRepository.findByGroup(group)

    fun findById(id: Int): GroupEntity? = groupRepository.findById(id).orElse(null)

    fun findAll(): MutableList<GroupEntity> = groupRepository.findAll()

    fun findAll(groupParam: Long?, pageable: Pageable): Page<GroupEntity> {
        with(QGroupEntity.groupEntity) {
            val bb = BooleanBuilder()
            if (groupParam != null) bb + group.eq(groupParam)
            return groupRepository.findAll(bb, pageable)
        }
    }

    fun deleteByGroup(group: Long) = groupRepository.deleteByGroup(group)
}

class GroupConfig{
    var repeat: Status = Status.OFF
    var locPush: Status = Status.OFF
    var recallNotify: Status = Status.OFF
    var flashImageNotify: Status = Status.OFF
    var leaveToBlack: Status = Status.ON
    var loLiConR18: Status = Status.OFF
    var prohibitedWords: MutableSet<String> = mutableSetOf()
    var blackList: MutableSet<Long> = mutableSetOf()
    var qaList: MutableList<Qa> = mutableListOf()
}

data class Qa(
    var q: String = "",
    var a: String = "",
    var type: QaType = QaType.FUZZY
)

enum class QaType {
    FUZZY,
    EXACT
}

enum class Status {
    ON,OFF;
}
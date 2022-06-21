package me.kuku.yuq.entity

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.querydsl.core.BooleanBuilder
import me.kuku.yuq.utils.plus
import org.hibernate.annotations.Type
import org.springframework.cache.annotation.CachePut
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name=  "group_")
@JsonIgnoreProperties("qqs")
@NamedEntityGraph(name = "qqs", attributeNodes = [NamedAttributeNode("qqs")])
class GroupEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(unique = true, name = "group_")
    var group: Long = 0
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "groups")
    @JSONField(serialize = false)
    var qqs: MutableSet<QqEntity> = linkedSetOf()
    @Type(type = "json")
    @Column(columnDefinition = "text")
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
    @EntityGraph(value = "qqs", type = EntityGraph.EntityGraphType.FETCH)
    fun findByGroup(group: Long): GroupEntity?
    fun deleteByGroup(group: Long)
}

@Service
class GroupService (
    private val groupRepository: GroupRepository
){
    @CachePut(value = ["database"], key = "'group' + #groupEntity.group")
    fun save(groupEntity: GroupEntity): GroupEntity = groupRepository.save(groupEntity)

    @org.springframework.cache.annotation.Cacheable(value = ["database"], key = "'group' + #group")
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
    var switch: Status = Status.OFF
    var repeat: Status = Status.OFF
    var recallNotify: Status = Status.OFF
    var flashImageNotify: Status = Status.OFF
    var leaveToBlack: Status = Status.OFF
    var loLiConR18: Status = Status.OFF
    var entryVerification: Status = Status.OFF
    var adminCanExecute: Status = Status.OFF
    var timekeeping: Status = Status.OFF
    var interceptList: MutableSet<String> = mutableSetOf()
    var adminList: MutableSet<Long> = mutableSetOf()
    var prohibitedWords: MutableSet<String> = mutableSetOf()
    var blackList: MutableSet<Long> = mutableSetOf()
    var qaList: MutableList<Qa> = mutableListOf()
    var commandLimitList: MutableSet<CommandLimit> = mutableSetOf()
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
    OFF,ON;
}

enum class CommandLimitType {
    QQ, GROUP
}

data class CommandLimit(
    var command: String = "",
    var limit: Int = 0,
    var type: CommandLimitType = CommandLimitType.GROUP
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandLimit

        if (command != other.command) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
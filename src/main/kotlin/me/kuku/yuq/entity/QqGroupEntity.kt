package me.kuku.yuq.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import java.io.Serializable
import javax.inject.Inject
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "qq_group")
@TypeDef(name = "json", typeClass = JsonType::class)
class QqGroupEntity {
    @EmbeddedId
    var qqGroupId: QqGroupId = QqGroupId()
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: QqGroupConfig = QqGroupConfig()
}


data class QqGroupId(
    @Column(name = "qq_id")
    var qqId: Int = 0,
    @Column(name = "group_id")
var groupId: Int = 0
) : Serializable {
    companion object {
        private const val serialVersionUID = -3549488445375382600L
    }
}

class QqGroupConfig{
    var prohibitedCount: Int = 0
}

interface QqGroupRepository: JpaRepository<QqGroupEntity, QqGroupId> {
    fun findByQqGroupId(qqGroupId: QqGroupId): QqGroupEntity?
}


class QqGroupService @Inject constructor(
    private val qqGroupRepository: QqGroupRepository
) {
    fun findByQqGroupId(qqGroupId: QqGroupId) = qqGroupRepository.findByQqGroupId(qqGroupId)

    fun save(qqGroupEntity: QqGroupEntity): QqGroupEntity = qqGroupRepository.save(qqGroupEntity)
}


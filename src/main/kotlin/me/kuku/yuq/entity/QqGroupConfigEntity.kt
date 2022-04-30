package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "qq_group_config")
class QqGroupConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @ManyToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @ManyToOne
    @JoinColumn(name = "group_id")
    var groupEntity: GroupEntity? = null
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: QqGroupConfig = QqGroupConfig()
}

interface QqGroupConfigRepository: JpaRepository<QqGroupConfigEntity, Int>, QuerydslPredicateExecutor<QqGroupConfigEntity> {

}

@Service
class QqGroupConfigService(
    private val qqGroupConfigRepository: QqGroupConfigRepository
) {

    fun findByGroupAndQq(group: Long, qq: Long): QqGroupConfigEntity? {
        val q = QQqGroupConfigEntity.qqGroupConfigEntity
        return qqGroupConfigRepository.findOne(q.groupEntity.group.eq(group).and(q.qqEntity.qq.eq(qq))).orElse(null)
    }

    fun save(qqGroupConfigEntity: QqGroupConfigEntity): QqGroupConfigEntity {
        return qqGroupConfigRepository.save(qqGroupConfigEntity)
    }

}



data class QqGroupConfig(var violationsNum: Int = 0)
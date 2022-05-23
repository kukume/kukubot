package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "bili_bili")
@NamedEntityGraph(name = "bili_bili_qq", attributeNodes = [NamedAttributeNode(value = "qqEntity", subgraph = "qqEntity")],
    subgraphs = [NamedSubgraph(name = "qqEntity", attributeNodes = [NamedAttributeNode("groups")])])
class BiliBiliEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 2000)
    var cookie: String = ""
    var userid: String = ""
    var token: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: BiliBiliConfig = BiliBiliConfig()
}

interface BiliBiliRepository: JpaRepository<BiliBiliEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): BiliBiliEntity?

    @EntityGraph(value = "bili_bili_qq", type = EntityGraph.EntityGraphType.FETCH)
    override fun findAll(): List<BiliBiliEntity>
}

@Service
class BiliBiliService (
    private val biliBiliRepository: BiliBiliRepository
) {
    fun findByQqEntity(qqEntity: QqEntity) = biliBiliRepository.findByQqEntity(qqEntity)

    fun save(biliEntity: BiliBiliEntity): BiliBiliEntity = biliBiliRepository.save(biliEntity)

    fun findAll(): List<BiliBiliEntity> = biliBiliRepository.findAll()

}

data class BiliBiliConfig(var push: Status = Status.OFF, var sign: Status = Status.OFF, var live: Status = Status.OFF, var coin: Status = Status.OFF)
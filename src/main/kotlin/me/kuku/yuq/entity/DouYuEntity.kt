package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "dou_yu")
@NamedEntityGraph(name = "douYu_qq_graph", attributeNodes = [NamedAttributeNode(value = "qqEntity", subgraph = "qqEntity")],
    subgraphs = [NamedSubgraph(name = "qqEntity", attributeNodes = [NamedAttributeNode("groups")])])
class DouYuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id", unique = true)
    var qqEntity: QqEntity? = null
    @Column(length = 3000)
    var cookie: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: DouYuConfig = DouYuConfig()
}

interface DouYuRepository: JpaRepository<DouYuEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): DouYuEntity?

    @EntityGraph(value = "douYu_qq_graph", type = EntityGraph.EntityGraphType.FETCH)
    fun findBy(): List<DouYuEntity>
}


@Service
class DouYuService(
    private val douYuRepository: DouYuRepository
) {

    fun save(douYuEntity: DouYuEntity): DouYuEntity = douYuRepository.save(douYuEntity)

    fun findByQqEntity(qqEntity: QqEntity) = douYuRepository.findByQqEntity(qqEntity)

    fun findAll(): List<DouYuEntity> = douYuRepository.findBy()

}

data class DouYuConfig(var live: Status = Status.OFF)
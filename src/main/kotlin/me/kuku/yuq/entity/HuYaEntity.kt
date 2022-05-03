package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "hu_ya")
@NamedEntityGraph(name = "huYa_qq_graph", attributeNodes = [NamedAttributeNode(value = "qqEntity", subgraph = "qqEntity")],
    subgraphs = [NamedSubgraph(name = "qqEntity", attributeNodes = [NamedAttributeNode("groups")])])
class HuYaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 3000)
    var cookie: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: HuYaConfig = HuYaConfig()
}

interface HuYaRepository: JpaRepository<HuYaEntity, Int>, QuerydslPredicateExecutor<HuYaEntity> {
    fun findByQqEntity(qqEntity: QqEntity): HuYaEntity?

    @EntityGraph(value = "huYa_qq_graph", type = EntityGraph.EntityGraphType.FETCH)
    fun findBy(): List<HuYaEntity>
}

@Service
class HuYaService(
    private val huYaRepository: HuYaRepository
){
    fun findByQqEntity(qqEntity: QqEntity) = huYaRepository.findByQqEntity(qqEntity)

    fun findAll(): List<HuYaEntity> = huYaRepository.findBy()

    fun save(entity: HuYaEntity): HuYaEntity = huYaRepository.save(entity)

}

data class HuYaConfig(var live: Status = Status.OFF)
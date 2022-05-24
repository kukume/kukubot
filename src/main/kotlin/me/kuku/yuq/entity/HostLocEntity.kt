package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "host_loc")
@NamedEntityGraph(name = "hostLoc_qq_graph", attributeNodes = [NamedAttributeNode(value = "qqEntity", subgraph = "qqEntity")],
    subgraphs = [NamedSubgraph(name = "qqEntity", attributeNodes = [NamedAttributeNode("groups")])])
class HostLocEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 1000)
    var cookie: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "text")
    var config: HostLocConfig = HostLocConfig()
}

interface HostLocRepository: JpaRepository<HostLocEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): HostLocEntity?

    @EntityGraph(value = "hostLoc_qq_graph", type = EntityGraph.EntityGraphType.FETCH)
    fun findByStatus(status: Status): List<HostLocEntity>

}

@Service
class HostLocService (
    private val hostLocRepository: HostLocRepository
) {

    fun save(hostLocEntity: HostLocEntity): HostLocEntity = hostLocRepository.save(hostLocEntity)

    fun findByQqEntity(qqEntity: QqEntity) = hostLocRepository.findByQqEntity(qqEntity)

    fun findByStatus(status: Status) = hostLocRepository.findByStatus(status)
}

data class HostLocConfig(var push: Status = Status.OFF, var sign: Status = Status.OFF)
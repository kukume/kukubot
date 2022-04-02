package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "host_loc")
@NamedEntityGraph(name = "qq_graph", attributeNodes = [NamedAttributeNode(value = "qqEntity", subgraph = "qqEntity")],
    subgraphs = [NamedSubgraph(name = "qqEntity", attributeNodes = [NamedAttributeNode("groups")])])
class HostLocEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity = QqEntity()
    @Column(length = 1000)
    var cookie: String = ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: HostLocConfig = HostLocConfig()
}

interface HostLocRepository: JpaRepository<HostLocEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): HostLocEntity?

    @EntityGraph(value = "qq_graph", type = EntityGraph.EntityGraphType.FETCH)
    fun findByStatus(status: Status): List<HostLocEntity>

}


class HostLocService @Inject constructor(
    private val hostLocRepository: HostLocRepository
) {

    fun save(hostLocEntity: HostLocEntity): HostLocEntity = hostLocRepository.save(hostLocEntity)

    fun findByQqEntity(qqEntity: QqEntity) = hostLocRepository.findByQqEntity(qqEntity)

    fun findByStatus(status: Status) = hostLocRepository.findByStatus(status)
}

data class HostLocConfig(var push: Status = Status.OFF, var sign: Status = Status.OFF)
package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*

@Entity
@Table(name = "job")
@NamedEntityGraph(name = "query_all_graph", attributeNodes = [
    NamedAttributeNode(value = "groupEntity"),
    NamedAttributeNode(value = "qqEntity", subgraph = "sub_group")
], subgraphs = [
    NamedSubgraph(name = "sub_group", attributeNodes = [NamedAttributeNode("groups")])
])
class JobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var cron: String = ""
    @Column(length = 3000)
    var msg: String = ""
    @ManyToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @ManyToOne
    @JoinColumn(name = "group_id")
    var groupEntity: GroupEntity? = null
    var begin: Boolean = true
}

interface JobRepository: JpaRepository<JobEntity, Int> {
    @EntityGraph(value = "query_all_graph", type = EntityGraph.EntityGraphType.FETCH)
    fun findBy(): List<JobEntity>

    fun findByQqEntity(qqEntity: QqEntity): List<JobEntity>

    fun findByGroupEntity(groupEntity: GroupEntity): List<JobEntity>

    @EntityGraph(value = "query_all_graph", type = EntityGraph.EntityGraphType.FETCH)
    fun findByIdOrderById(id: Int): JobEntity?
}


@Service
class JobService(
    private val jobRepository: JobRepository
) {

    fun findAll() = jobRepository.findBy()

    fun save(jobEntity: JobEntity): JobEntity = jobRepository.save(jobEntity)

    @Transactional
    fun delete(jobEntity: JobEntity) = jobRepository.delete(jobEntity)

    @Transactional
    fun deleteById(id: Int) = jobRepository.deleteById(id)

    fun findByQqEntity(qqEntity: QqEntity) = jobRepository.findByQqEntity(qqEntity)

    fun findByGroupEntity(groupEntity: GroupEntity) = jobRepository.findByGroupEntity(groupEntity)

    fun findByIdOrderById(id: Int) = jobRepository.findByIdOrderById(id)

}
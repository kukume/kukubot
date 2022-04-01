package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "forward")
class ForwardEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(name= "group_")
    var group: Long = 0
    var qq: Long = 0
    @Column(unique = true)
    var instruction: String = ""
}


interface ForwardRepository: JpaRepository<ForwardEntity, Int> {
    fun findByInstruction(instruction: String): ForwardEntity?
    fun deleteByInstruction(instruction: String)
    fun findByInstructionStartingWith(instruction: String): MutableList<ForwardEntity>
}

class ForwardService @Inject constructor(
    private val forwardRepository: ForwardRepository
) {

    fun save(forwardEntity: ForwardEntity): ForwardEntity = forwardRepository.save(forwardEntity)

    fun findByInstruction(instruction: String): ForwardEntity? = forwardRepository.findByInstruction(instruction)

    fun deleteByInstruction(instruction: String) = forwardRepository.deleteByInstruction(instruction)

    fun findAll(): MutableList<ForwardEntity> = forwardRepository.findAll()

    fun findByInstructionStartingWith(instruction: String): MutableList<ForwardEntity> =
        forwardRepository.findByInstructionStartingWith(instruction)

}
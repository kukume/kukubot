package me.kuku.simbot.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "i_qi_yi")
data class IqiYiEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 1000)
    var cookie: String = "",
    var pOne: String = "",
    var pThree: String = ""
){
    companion object{
        fun getInstance(qqEntity: QqEntity): IqiYiEntity{
            return IqiYiEntity(qqEntity = qqEntity)
        }
        fun getInstance(cookie: String, pOne: String, pThree: String): IqiYiEntity{
            return IqiYiEntity(cookie = cookie, pOne = pOne, pThree = pThree)
        }
    }
}

interface IqiYiRepository: JpaRepository<IqiYiEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): IqiYiEntity?
}

interface IqiYiService{
    fun findByQqEntity(qqEntity: QqEntity): IqiYiEntity?
    fun save(entity: IqiYiEntity): IqiYiEntity
    fun findAll(): List<IqiYiEntity>
    fun delete(entity: IqiYiEntity)
}

@Service
class IqiYiServiceImpl: IqiYiService{

    @Resource
    private lateinit var iqiYiRepository: IqiYiRepository

    override fun findByQqEntity(qqEntity: QqEntity): IqiYiEntity? {
        return iqiYiRepository.findByQqEntity(qqEntity)
    }

    override fun save(entity: IqiYiEntity): IqiYiEntity {
        return iqiYiRepository.save(entity)
    }

    override fun findAll(): List<IqiYiEntity> {
        return iqiYiRepository.findAll()
    }

    override fun delete(entity: IqiYiEntity) {
        iqiYiRepository.delete(entity)
    }
}
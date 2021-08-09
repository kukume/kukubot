package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
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

interface IqiYiDao: JPADao<IqiYiEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): IqiYiEntity?
}

@AutoBind
interface IqiYiService{
    fun findByQqEntity(qqEntity: QqEntity): IqiYiEntity?
    fun save(entity: IqiYiEntity)
    fun findAll(): List<IqiYiEntity>
    fun delete(entity: IqiYiEntity)
}

class IqiYiServiceImpl: IqiYiService{

    @Inject
    private lateinit var iqiYiDao: IqiYiDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): IqiYiEntity? {
        return iqiYiDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun save(entity: IqiYiEntity) {
        return iqiYiDao.saveOrUpdate(entity)
    }

    @Transactional
    override fun findAll(): List<IqiYiEntity> {
        return iqiYiDao.findAll()
    }

    @Transactional
    override fun delete(entity: IqiYiEntity) {
        iqiYiDao.delete(entity.id!!)
    }
}
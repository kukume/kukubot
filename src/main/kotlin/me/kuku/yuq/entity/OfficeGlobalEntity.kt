package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.alibaba.fastjson.JSON
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "office_global")
data class OfficeGlobalEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @Column(unique = true)
    var name: String = "",
    var clientId: String = "",
    var clientSecret: String = "",
    var tenantId: String = "",
    var sku: String = "[]",
    var domain: String = ""
){
    var sKuJson: List<Sku>
        get() = JSON.parseArray(sku, Sku::class.java)
        set(json) {
            sku = JSON.toJSONString(json)
        }
}

data class Sku(
    var name: String = "",
    var id: String = ""
)


interface OfficeGlobalDao: JPADao<OfficeGlobalEntity, Int>{
    fun findByName(name: String): OfficeGlobalEntity?
}

@AutoBind
interface OfficeGlobalService{
    fun findAll(): List<OfficeGlobalEntity>
    fun delete(officeGlobalEntity: OfficeGlobalEntity)
    fun save(officeGlobalEntity: OfficeGlobalEntity)
    fun findByName(name: String): OfficeGlobalEntity?
}

class OfficeGlobalServiceImpl @Inject constructor(private val officeGlobalDao: OfficeGlobalDao): OfficeGlobalService{
    @Transactional
    override fun findAll(): List<OfficeGlobalEntity> {
        return officeGlobalDao.findAll()
    }

    @Transactional
    override fun delete(officeGlobalEntity: OfficeGlobalEntity) {
        officeGlobalDao.delete(officeGlobalEntity.id!!)
    }

    @Transactional
    override fun save(officeGlobalEntity: OfficeGlobalEntity) {
        return officeGlobalDao.saveOrUpdate(officeGlobalEntity)
    }

    @Transactional
    override fun findByName(name: String): OfficeGlobalEntity? {
        return officeGlobalDao.findByName(name)
    }
}

enum class OfficeRole(val value: String){
    USER_ADMIN("fe930be7-5e62-47db-91af-98c3a49a38b1"),
    GLOBAL_ADMIN("62e90394-69f5-4237-9190-012177145e10"),
    PRIVILEGED_ROLE_ADMIN("e8611ab8-c189-46e8-94e1-60213ab1f814")
}
package me.kuku.simbot.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
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


interface OfficeGlobalRepository: JpaRepository<OfficeGlobalEntity, Int>{
    fun findByName(name: String): OfficeGlobalEntity?
}

interface OfficeGlobalService{
    fun findAll(): List<OfficeGlobalEntity>
    fun delete(officeGlobalEntity: OfficeGlobalEntity)
    fun save(officeGlobalEntity: OfficeGlobalEntity): OfficeGlobalEntity
    fun findByName(name: String): OfficeGlobalEntity?
}

@Service
class OfficeGlobalServiceImpl(private val officeGlobalRepository: OfficeGlobalRepository): OfficeGlobalService{
    override fun findAll(): List<OfficeGlobalEntity> {
        return officeGlobalRepository.findAll()
    }

    override fun delete(officeGlobalEntity: OfficeGlobalEntity) {
        officeGlobalRepository.delete(officeGlobalEntity)
    }

    override fun save(officeGlobalEntity: OfficeGlobalEntity): OfficeGlobalEntity {
        return officeGlobalRepository.save(officeGlobalEntity)
    }

    override fun findByName(name: String): OfficeGlobalEntity? {
        return officeGlobalRepository.findByName(name)
    }
}

enum class OfficeRole(val value: String){
    USER_ADMIN("fe930be7-5e62-47db-91af-98c3a49a38b1"),
    GLOBAL_ADMIN("62e90394-69f5-4237-9190-012177145e10"),
    PRIVILEGED_ROLE_ADMIN("e8611ab8-c189-46e8-94e1-60213ab1f814")
}
package me.kuku.simbot.entity

import com.alibaba.fastjson.annotation.JSONField
import me.kuku.utils.QqUtils
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "qq_login")
open class QqLoginEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    open var qqEntity: QqEntity? = null,
    open var sKey: String = "",
    open var psKey: String = "",
    open var superKey: String = "",
    open var superToken: String = "",
    open var pt4Token: String = "",
    @Transient
    open var groupPsKey: String = ""
){
    companion object{
        fun getInstance(qqEntity: QqEntity): QqLoginEntity{
            return QqLoginEntity(qqEntity = qqEntity)
        }

        fun getInstance(qqEntity: QqEntity, sKey: String, psKey: String, superKey: String, superToken: Long, groupPsKey: String): QqLoginEntity{
            return QqLoginEntity(null, qqEntity, sKey, psKey, superKey, superToken.toString(), "", groupPsKey)
        }
    }

    @JSONField(serialize = false)
    @Transactional
    open fun getCookie(): String {
        return String.format("pt2gguin=o0%s; uin=o0%s; skey=%s; ", qqEntity!!.qq, qqEntity!!.qq, sKey)
    }

    @JSONField(serialize = false)
    @Transactional
    open fun getCookie(psKey: String?): String {
        return String.format("%sp_skey=%s; p_uin=o0%s;", getCookie(), psKey, qqEntity!!.qq)
    }

    @JSONField(serialize = false)
    @Transactional
    open fun getCookieWithPs(): String {
        return String.format("%sp_skey=%s; p_uin=o0%s; ", getCookie(), psKey, qqEntity!!.qq)
    }

    @JSONField(serialize = false)
    @Transactional
    open fun getCookieWithSuper(): String {
        return String.format("superuin=o0%s; superkey=%s; supertoken=%s; ", qqEntity!!.qq, superKey, superToken)
    }

    @JSONField(serialize = false)
    fun getGtk(): String {
        return QqUtils.getGTK(sKey).toString()
    }

    @JSONField(serialize = false)
    fun getGtk(psKey: String?): String {
        return QqUtils.getGTK(psKey).toString()
    }

    @JSONField(serialize = false)
    fun getGtk2(): String {
        return QqUtils.getGTK2(sKey)
    }

    @JSONField(serialize = false)
    fun getGtkP(): String {
        return QqUtils.getGTK(psKey).toString()
    }

    @JSONField(serialize = false)
    fun getToken(): String {
        return QqUtils.getToken(superToken).toString()
    }

    @JSONField(serialize = false)
    fun getToken2(): String {
        return QqUtils.getToken2(superToken).toString()
    }
}

interface QqLoginRepository: JpaRepository<QqLoginEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): QqLoginEntity?
}

interface QqLoginService{
    fun findByQqEntity(qqEntity: QqEntity): QqLoginEntity?
    fun findAll(): List<QqLoginEntity>
    fun save(qqLoginEntity: QqLoginEntity): QqLoginEntity
    fun delete(entity: QqLoginEntity)
}

@Service
class QqLoginServiceImpl: QqLoginService{
    @Resource
    private lateinit var qqLoginRepository: QqLoginRepository
    override fun findByQqEntity(qqEntity: QqEntity): QqLoginEntity? {
        return qqLoginRepository.findByQqEntity(qqEntity)
    }

    override fun findAll(): List<QqLoginEntity> {
        return qqLoginRepository.findAll()
    }

    override fun save(qqLoginEntity: QqLoginEntity): QqLoginEntity {
        return qqLoginRepository.save(qqLoginEntity)
    }

    override fun delete(entity: QqLoginEntity) {
        return qqLoginRepository.delete(entity)
    }
}
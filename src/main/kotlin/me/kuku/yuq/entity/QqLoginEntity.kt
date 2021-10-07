package me.kuku.yuq.entity

import com.alibaba.fastjson.annotation.JSONField
import me.kuku.utils.QqUtils
import javax.persistence.*
import kotlin.jvm.Transient

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
    open fun getCookie(): String {
        return String.format("pt2gguin=o0%s; uin=o0%s; skey=%s; ", qqEntity!!.qq, qqEntity!!.qq, sKey)
    }

    @JSONField(serialize = false)
    open fun getCookie(psKey: String?): String {
        return String.format("%sp_skey=%s; p_uin=o0%s;", getCookie(), psKey, qqEntity!!.qq)
    }

    @JSONField(serialize = false)
    open fun getCookieWithPs(): String {
        return String.format("%sp_skey=%s; p_uin=o0%s; ", getCookie(), psKey, qqEntity!!.qq)
    }

    @JSONField(serialize = false)
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

package me.kuku.yuq.entity

import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.QQUtils
import javax.persistence.*

@Entity
@Table(name = "qqLogin")
data class QQLoginEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true)
        var qq: Long = 0L,
        var qqGroup: Long = 0L,
        var password: String = "",
        var sKey: String = "",
        var psKey: String = "",
        var groupPsKey: String? = "",
        var superKey: String = "",
        var superToken: String = "",
        var pt4Token: String = "",
        var status: Boolean = false
)
{
    @Transient
    fun cookie() = OkHttpClientUtils.addCookie(this.getCookie())
    @Transient
    fun cookieWithQQZone() = OkHttpClientUtils.addCookie(this.getCookieWithQQZone())
    @Transient
    fun cookieWithGroup() = OkHttpClientUtils.addCookie(this.getCookieWithGroup())
    @Transient
    fun getCookie() = "pt2gguin=o0$qq; uin=o0$qq; skey=$sKey; "
    @Transient
    fun getCookie(psKey: String) = "${this.getCookie()}p_skey=$psKey; p_uin=o0$qq;"
    @Transient
    fun getCookieWithQQZone() = "${this.getCookie()}p_skey=$psKey; p_uin=o0$qq; "
    @Transient
    fun getCookieWithGroup() = "${this.getCookie()}p_skey=$groupPsKey; p_uin=o0$qq; "
    @Transient
    fun getCookieWithSuper() = "superuin=o0$qq; superkey=$superKey; supertoken=$superToken; "
    @Transient
    fun getGtk() = QQUtils.getGtk(sKey).toString()
    @Transient
    fun getGtk(psKey: String) = QQUtils.getGtk(psKey).toString()
    @Transient
    fun getGtk2() = QQUtils.getGtk2(sKey)
    @Transient
    fun getGtkP() = QQUtils.getGtk(psKey).toString()
    @Transient
    fun getToken() = QQUtils.getToken(superToken).toString()

}
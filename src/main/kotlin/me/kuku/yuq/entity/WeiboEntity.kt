package me.kuku.yuq.entity

import javax.persistence.*

@Entity
@Table(name = "weibo")
data class WeiboEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true)
        var qq: Long = 0L,
        var group_: Long? = 0L,
        var username: String = "",
        var password: String = "",
        @Lob
        @Column(columnDefinition="text")
        var pcCookie: String = "",
        @Lob
        @Column(columnDefinition="text")
        var mobileCookie: String = "",
        var monitor: Boolean? = false
)
package me.kuku.yuq.entity

import javax.persistence.*

@Entity
@Table(name = "steam")
data class SteamEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        val qq:Long = 0L,
        var username: String = "",
        var password: String = "",
        @Column(length = 400)
        var cookie: String = "",
        @Column(length = 500)
        var buffCookie: String = "",
        var steamId: String = ""
)
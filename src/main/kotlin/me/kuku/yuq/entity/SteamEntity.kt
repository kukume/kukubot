package me.kuku.yuq.entity

import javax.persistence.*

@Entity
@Table(name = "steam")
data class SteamEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true)
        val qq:Long = 0L,
        var username: String = "",
        var password: String = "",
        @Lob
        @Column(columnDefinition="text")
        var cookie: String = "",
        @Lob
        @Column(columnDefinition="text")
        var buffCookie: String = "",
        var steamId: String = ""
)
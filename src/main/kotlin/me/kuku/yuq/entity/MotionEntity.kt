package me.kuku.yuq.entity

import javax.persistence.*

@Table(name = "motion")
@Entity
data class MotionEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true)
        var qq: Long = 0L,
        var leXinPhone: String = "",
        var leXinPassword: String = "",
        @Lob
        @Column(columnDefinition="text")
        var leXinCookie: String = "",
        var leXinUserId: String = "",
        @Lob
        @Column(columnDefinition="text")
        var leXinAccessToken: String = "",
        var step: Int = 0,
        var miPhone: String = "",
        var miPassword: String = "",
        @Lob
        @Column(columnDefinition="text")
        var miLoginToken: String = ""
)
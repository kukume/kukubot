package me.kuku.yuq.entity

import javax.persistence.*

@Table(name = "motion")
@Entity
data class MotionEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var qq: Long = 0L,
        var phone: String = "",
        @Column(length = 1400)
        var cookie: String = "",
        var userId: String = "",
        var accessToken: String = "",
        var step: Int = 0
)
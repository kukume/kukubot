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
        @Lob
        @Column(columnDefinition="text")
        var cookie: String = "",
        var userId: String = "",
        @Column(length = 500)
        var accessToken: String = "",
        var step: Int = 0
)
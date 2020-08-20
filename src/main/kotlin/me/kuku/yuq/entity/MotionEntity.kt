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
        var phone: String = "",
        @Lob
        @Column(columnDefinition="text")
        var cookie: String = "",
        var userId: String = "",
        @Lob
        @Column(columnDefinition="text")
        var accessToken: String = "",
        var step: Int = 0
)
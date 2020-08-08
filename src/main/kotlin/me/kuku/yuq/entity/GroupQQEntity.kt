package me.kuku.yuq.entity

import javax.persistence.*

@Entity
@Table(name = "groupQQ")
data class GroupQQEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var qq: Long = 0L,
        var group_: Long = 0L,
        var violationCount: Int = 0
)
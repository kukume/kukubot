package me.kuku.yuq.entity

import javax.persistence.*

@Entity
@Table(name = "QQ")
data class QQEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var qq: Long = 0L,
        @OneToOne
        @JoinColumn(name = "group_id")
        var groupEntity: GroupEntity = GroupEntity(),
        var violationCount: Int = 0
)
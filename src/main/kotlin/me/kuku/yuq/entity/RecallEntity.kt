package me.kuku.yuq.entity

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "recall", indexes = [Index(name = "ids_group_qq", columnList = "group_,qq")])
data class RecallEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var qq: Long = 0,
        @Column(name = "group_")
        var group: Long = 0,
        @OneToOne
        @JoinColumn(name = "message_id")
        var messageEntity: MessageEntity = MessageEntity(),
        @Temporal(TemporalType.TIMESTAMP)
        var date: Date = Date()
)
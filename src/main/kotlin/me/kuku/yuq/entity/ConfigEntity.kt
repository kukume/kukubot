package me.kuku.yuq.entity

import javax.persistence.*

@Entity
@Table(name = "config")
data class ConfigEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true)
        var type: String = "",
        @Lob
        @Column(columnDefinition = "text")
        var content: String = ""
)
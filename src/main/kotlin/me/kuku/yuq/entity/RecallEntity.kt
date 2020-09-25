package me.kuku.yuq.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
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
        var message: String = "",
        @Temporal(TemporalType.TIMESTAMP)
        var date: Date = Date()
){
    val messageJsonArray: JSONArray
        @Transient
        get() = JSON.parseArray(message)
}
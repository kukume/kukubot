package me.kuku.yuq.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "message", indexes = [Index(name = "idx_group_date", columnList = "group_,date"), Index(name = "idx_messageId", columnList = "messageId")])
data class MessageEntity (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        var messageId: Int = 0,
        @Column(name = "group_")
        var group: Long = 0,
        var qq: Long = 0,
        @Lob
        @Column(columnDefinition = "text")
        val content: String = "",
        @Temporal(TemporalType.TIMESTAMP)
        var date: Date = Date()
){
        val contentJsonArray: JSONArray
                @Transient
                get() = JSON.parseArray(content)
}
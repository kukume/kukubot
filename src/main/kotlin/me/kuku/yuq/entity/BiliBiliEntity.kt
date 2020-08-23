package me.kuku.yuq.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import javax.persistence.*

@Entity
@Table(name = "biliBili")
data class BiliBiliEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true)
        var qq: Long = 0L,
        var group_: Long = 0L,
        @Lob
        @Column(columnDefinition = "text")
        var cookie: String = "",
        var monitor: Boolean = false,
        @Lob
        @Column(columnDefinition = "text")
        var liveList: String = "[]"
){
        @Transient
        fun getLiveJsonArray(): JSONArray = JSON.parseArray(liveList) ?: JSON.parseArray("[]")
}
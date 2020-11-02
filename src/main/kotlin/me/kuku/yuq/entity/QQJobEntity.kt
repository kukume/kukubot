package me.kuku.yuq.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import javax.persistence.*

@Entity
@Table(name = "QQJob")
data class QQJobEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var qq: Long = 0L,
        var type: String = "",
        @Lob
        @Column(columnDefinition="text")
        var data: String = "{}"
){
        val dataJsonObject: JSONObject
                get() = JSON.parseObject(data)

}
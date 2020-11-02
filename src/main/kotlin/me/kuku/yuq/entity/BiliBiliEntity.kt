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
        @Column(name = "task_")
        var task: Boolean = false,
        @Lob
        @Column(columnDefinition = "text")
        var liveList: String = "[]",
        @Lob
        @Column(columnDefinition = "text")
        var likeList: String = "[]",
        @Lob
        @Column(columnDefinition = "text")
        var commentList: String = "[]",
        @Lob
        @Column(columnDefinition = "text")
        var forwardList: String = "[]",
        @Lob
        @Column(columnDefinition = "text")
        var tossCoinList: String = "[]",
        @Lob
        @Column(columnDefinition = "text")
        var favoritesList: String = "[]",
        var token: String = "",
        var userId: String = ""
){
        val liveJsonArray: JSONArray
                get() = JSON.parseArray(liveList)
        val likeJsonArray: JSONArray
                get() = JSON.parseArray(likeList)
        val commentJsonArray: JSONArray
                get() = JSON.parseArray(commentList)
        val forwardJsonArray: JSONArray
                get() = JSON.parseArray(forwardList)
        val tossCoinJsonArray: JSONArray
                get() = JSON.parseArray(tossCoinList)
        val favoritesJsonArray: JSONArray
                get() = JSON.parseArray(favoritesList)
}
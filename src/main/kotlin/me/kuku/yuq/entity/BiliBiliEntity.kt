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
        @Transient
        fun getLiveJsonArray(): JSONArray = JSON.parseArray(liveList) ?: JSON.parseArray("[]")
        @Transient
        fun getLikeJsonArray(): JSONArray = JSON.parseArray(likeList) ?: JSON.parseArray("[]")
        @Transient
        fun getCommentJsonArray(): JSONArray = JSON.parseArray(commentList) ?: JSON.parseArray("[]")
        @Transient
        fun getForwardJsonArray(): JSONArray = JSON.parseArray(forwardList) ?: JSON.parseArray("[]")
        @Transient
        fun getTossCoinJsonArray(): JSONArray = JSON.parseArray(tossCoinList) ?: JSON.parseArray("[]")
        @Transient
        fun getFavoritesJsonArray(): JSONArray = JSON.parseArray(favoritesList) ?: JSON.parseArray("[]")
}
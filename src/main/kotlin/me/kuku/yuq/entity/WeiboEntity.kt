package me.kuku.yuq.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import javax.persistence.*

@Entity
@Table(name = "weibo")
data class WeiboEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true)
        var qq: Long = 0L,
        @Column(name = "group_")
        var group: Long = 0L,
        var username: String = "",
        var password: String = "",
        @Lob
        @Column(columnDefinition="text")
        var pcCookie: String = "",
        @Lob
        @Column(columnDefinition="text")
        var mobileCookie: String = "",
        var monitor: Boolean = false,
        @Lob
        @Column(columnDefinition="text")
        var likeList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var commentList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var forwardList: String = "[]"
){
        val likeJsonArray: JSONArray
                get() = JSON.parseArray(likeList)
        val commentJsonArray: JSONArray
                get() = JSON.parseArray(commentList)
        val forwardJsonArray: JSONArray
                get() = JSON.parseArray(forwardList)
}
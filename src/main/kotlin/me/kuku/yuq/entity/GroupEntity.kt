package me.kuku.yuq.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import javax.persistence.*

@Entity
@Table(name = "group_")
data class GroupEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        @Column(unique = true, name = "group_")
        var group: Long = 0L,
        @Lob
        @Column(columnDefinition="text")
        var blackList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var whiteList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var violationList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var qaList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var adminList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var weiboList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var biliBiliList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var interceptList: String = "[]",
        var colorPic: Boolean = false,
        var status: Boolean = false,
        var recall: Boolean = false,
        var pic: Boolean = false,
        var leaveGroupBlack: Boolean = false,
        var welcomeMsg: Boolean = false,
        var autoReview: Boolean = false,
        var onTimeAlarm: Boolean = false,
        var colorPicType: String = "danbooru",
        var maxViolationCount: Int = 5,
        var locMonitor: Boolean = false,
        var flashNotify: Boolean = false
){
        val biliBiliJsonArray: JSONArray
                get() = JSON.parseArray(biliBiliList)
        val weiboJsonArray: JSONArray
                get() = JSON.parseArray(weiboList)
        val adminJsonArray: JSONArray
                get() = JSON.parseArray(adminList)
        val violationJsonArray: JSONArray
                get() = JSON.parseArray(violationList)
        val qaJsonArray: JSONArray
                get() = JSON.parseArray(qaList)
        val blackJsonArray: JSONArray
                get() = JSON.parseArray(blackList)
        val whiteJsonArray: JSONArray
                get() = JSON.parseArray(whiteList)
        val interceptJsonArray: JSONArray
                get() = JSON.parseArray(interceptList)
}
package me.kuku.yuq.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import javax.persistence.*

@Entity
@Table(name = "qqGroup")
data class QQGroupEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var group_: Long = 0L,
        @Lob
        @Column(columnDefinition="text")
        var blackList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var whiteList: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var keyword: String = "[]",
        @Lob
        @Column(columnDefinition="text")
        var qa: String = "[]",
        var musicType: String = "qq",
        var colorPic: Boolean? = false,
        var status: Boolean? = false,
        var recall: Boolean? = false,
        var pic: Boolean? = false,
        var leaveGroupBlack: Boolean? = false,
        var qqStatus: Boolean? = false,
        var superCute: Boolean? = false,
        var welcomeMsg: Boolean? = false,
        var mouthOdor: Boolean? = false,
        var autoReview: Boolean? = false,
        var onTimeAlarm: Boolean? = false,
        var colorPicType: String? = "remote"
){
        @Transient
        fun getQaJsonArray(): JSONArray = JSON.parseArray(qa) ?: JSON.parseArray("[]")
        @Transient
        fun getBlackJsonArray(): JSONArray = JSON.parseArray(blackList) ?: JSON.parseArray("[]")
        @Transient
        fun getWhiteJsonArray(): JSONArray = JSON.parseArray(whiteList) ?: JSON.parseArray("[]")
        @Transient
        fun getKeywordJsonArray(): JSONArray = JSON.parseArray(keyword) ?: JSON.parseArray("[]")
}
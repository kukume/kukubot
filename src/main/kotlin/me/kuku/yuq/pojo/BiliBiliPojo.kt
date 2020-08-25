package me.kuku.yuq.pojo

data class BiliBiliPojo(
        var userId: String = "",
        var name: String = "",
        var id: String = "",
        var rid: String = "",
        var time: Long = 0,
        var text: String = "",
        var bvId: String? = null,
        var picList: MutableList<String> = mutableListOf(),
        var isForward: Boolean = false,
        var forwardUserId: String? = null,
        var forwardName: String? = null,
        var forwardId: String? = null,
        var forwardTime: Long? = null,
        var forwardText: String? = null,
        var forwardBvId: String? = null,
        var forwardPicList: MutableList<String> = mutableListOf()
)
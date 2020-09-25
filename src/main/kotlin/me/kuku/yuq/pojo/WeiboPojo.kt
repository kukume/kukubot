package me.kuku.yuq.pojo

data class WeiboPojo(
        var id: String = "",
        var name: String = "",
        var userId: String = "",
        var created: String = "",
        var text: String = "",
        var bid: String = "",
        var imageUrl: List<String> = listOf(),
        var isForward: Boolean = false,
        var forwardId: String = "",
        var forwardTime: String = "",
        var forwardName: String = "",
        var forwardText: String = "",
        var forwardBid: String = ""
)
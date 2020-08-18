package me.kuku.yuq.pojo

data class WeiboPojo(
        var id: String = "",
        var name: String = "",
        var userId: String = "",
        var created: String = "",
        var text: String = "",
        var bid: String = "",
        var imageUrl: List<String> = listOf()
)
package me.kuku.yuq.pojo

data class GroupMember(
        val qq: Long,
        val level: Int = 0,
        val integral: Int = 0,
        val joinTime: Long = 0,
        val lastTime: Long = 0,
        val age: Int = 0,
        val groupCard: String? = null,
        val nickName: String? = null,
        val country: String? = null,
        val province: String? = null,
        val city: String? = null,
        val userAge: Int = 0
)
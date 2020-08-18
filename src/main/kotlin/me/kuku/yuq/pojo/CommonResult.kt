package me.kuku.yuq.pojo

data class CommonResult<T>(
        val code: Int,
        val msg: String,
        val t: T? = null
)
package me.kuku.yuq.pojo

import com.alibaba.fastjson.annotation.JSONField

data class Token (
    var accessToken: String = "",
    var refreshToken: String = "",
    var expire: Long = 0,
    var createTime: Long = System.currentTimeMillis()
){
    companion object{
        fun getInstance(accessToken: String, refreshToken: String, expire: Long): Token{
            return Token(accessToken, refreshToken, expire)
        }
    }

    @JSONField(serialize = false)
    fun isExpire(): Boolean{
        return System.currentTimeMillis() - createTime > expire
    }
}
package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.SteamEntity
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface SteamLogic {
    fun login(username: String, password: String, twoCode: String): CommonResult<Map<String, String>>
    fun changeName(steamEntity: SteamEntity, name: String): String
    fun loginToBuff(steamEntity: SteamEntity): CommonResult<String>
}
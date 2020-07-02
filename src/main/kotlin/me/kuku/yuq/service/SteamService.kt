package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.SteamEntity
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface SteamService {
    fun login(username: String, password: String, twoCode: String): CommonResult<Map<String, String>>
    fun changeName(steamEntity: SteamEntity, name: String): String
    fun loginToBuff(steamEntity: SteamEntity): CommonResult<String>
}
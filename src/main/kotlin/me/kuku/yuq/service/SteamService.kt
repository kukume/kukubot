package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.SteamEntity

@AutoBind
interface SteamService {
    fun findByQQ(qq: Long): SteamEntity?
    fun save(steamEntity: SteamEntity)
    fun delByQQ(qq: Long)
}
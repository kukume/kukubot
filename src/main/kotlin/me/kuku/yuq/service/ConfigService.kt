package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.ConfigEntity

@AutoBind
interface ConfigService {
    fun findAll(): List<ConfigEntity>
    fun save(configEntity: ConfigEntity)
    fun findByType(type: String): ConfigEntity?
}
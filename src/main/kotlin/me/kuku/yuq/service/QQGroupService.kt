package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQGroupEntity

@AutoBind
interface QQGroupService {

    fun save(qqGroupEntity: QQGroupEntity)
    fun findByGroup(group: Long): QQGroupEntity?
    fun findByOnTimeAlarm(onTimeAlarm: Boolean): List<QQGroupEntity>

}
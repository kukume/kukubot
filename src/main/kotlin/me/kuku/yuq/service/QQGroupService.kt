package me.kuku.yuq.service

import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult

interface QQGroupService {
    fun addGroupMember(qqEntity: QQEntity, qq: Long, group: Long): String
    fun setGroupAdmin(qqEntity: QQEntity, qq: Long, group: Long, isAdmin: Boolean): String
    fun setGroupCard(qqEntity: QQEntity, qq: Long, group: Long, name: String): String
    fun deleteGroupMember(qqEntity: QQEntity, qq: Long, group: Long, isFlag: Boolean): String
    fun groupDragonKing(qqEntity: QQEntity, group: Long): CommonResult<Map<String, Long>>
}
package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface QQGroupLogic {
    fun addGroupMember(qq: Long, group: Long): String
    fun setGroupAdmin(qq: Long, group: Long, isAdmin: Boolean): String
    fun setGroupCard(qq: Long, group: Long, name: String): String
    fun deleteGroupMember(qq: Long, group: Long, isFlag: Boolean): String
    fun groupDragonKing(group: Long): CommonResult<Map<String, Long>>
    fun addHomeWork(group: Long, courseName: String, title: String, content: String, needFeedback: Boolean): String
    fun groupCharin(group: Long, content: String, time: Long): String
}
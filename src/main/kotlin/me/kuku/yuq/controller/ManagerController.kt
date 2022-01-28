package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Config
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.Status
import javax.inject.Inject

@GroupController
class ManagerController @Inject constructor(
    private val groupService: GroupService,
    @Config("YuQ.ArtQQ.master") private val master: String
): QQController(){

    private fun before(qq: Long) {
        if (qq != master.toLong()) throw mif.at(qq).plus("权限不足，无法执行").toThrowable()
    }

    @Action("{operate} {status}")
    @QMsg(reply = true)
    fun operateStatus(operate: String, status: Boolean, groupEntity: GroupEntity, qq: Long): String? {
        val config = groupEntity.config
        when (operate) {
            "loc群推送" -> config.locPush = status.toStatus()
            "复读" -> config.repeat = status.toStatus()
            "撤回通知" -> config.recallNotify = status.toStatus()
            "闪照通知" -> config.flashImageNotify = status.toStatus()
            "退群拉黑" -> config.leaveToBlack = status.toStatus()
            else -> return null
        }
        before(qq)
        groupService.save(groupEntity)
        return "${operate}${if (status) "开启" else "关闭"}成功"
    }

    @Action("加{operate}")
    @QMsg(reply = true)
    fun add(operate: String, session: ContextSession, groupEntity: GroupEntity, qq: Long): String? {
        val config = groupEntity.config
        when (operate) {
            "违禁词" -> {
                reply("请输入违禁词列表，如有多个，使用空格分割")
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ")
                config.prohibitedWords.addAll(arr)
            }
            else -> return null
        }
        before(qq)
        groupService.save(groupEntity)
        return "添加${operate}成功"
    }

    @Action("删{operate}")
    @QMsg(reply = true)
    fun delete(operate: String, session: ContextSession, groupEntity: GroupEntity, qq: Long): String? {
        val config = groupEntity.config
        when (operate) {
            "违禁词" -> {
                reply("请输入需要删除的违禁词列表，如果有多个，使用空格分割")
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ")
                val prohibitedWords = config.prohibitedWords
                arr.forEach(prohibitedWords::remove)
            }
            else -> return null
        }
        before(qq)
        groupService.save(groupEntity)
        return "删除${operate}成功"
    }

    @Action("查{operate}")
    @QMsg(reply = true)
    fun query(operate: String, groupEntity: GroupEntity): String? {
        val config = groupEntity.config
        val sb = StringBuilder()
        return when (operate) {
            "违禁词" -> {
                config.prohibitedWords.forEach { sb.append(it).append(" ") }
                sb.toString()
            }
            else -> return null
        }
    }
}

fun Boolean.toStatus() = if (this) Status.ON else Status.OFF

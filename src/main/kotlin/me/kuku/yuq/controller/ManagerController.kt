package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Config
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import me.kuku.yuq.entity.*
import javax.inject.Inject

@GroupController
class ManagerController @Inject constructor(
    private val groupService: GroupService,
    @Config("YuQ.ArtQQ.master") private val master: String,
    private val messageService: MessageService
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
                reply("请发送违禁词列表，多个使用空格分割")
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ")
                config.prohibitedWords.addAll(arr)
            }
            "黑名单" -> {
                reply("请发送黑名单列表，多个使用空格分割")
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ").map { it.toLong() }
                config.blackList.addAll(arr)
            }
            "问答" -> {
                reply("请发送问题")
                val q = session.waitNextMessage().toCodeString()
                reply("请发送机器人回复的答案")
                val a = session.waitNextMessage().toCodeString()
                reply("请发送数字，1表示精准匹配，2表示模糊匹配")
                val ss = session.waitNextMessage().firstString()
                val type = if (ss == "1") QaType.EXACT else QaType.FUZZY
                val qa = Qa(q, a, type)
                config.qaList.add(qa)
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
                reply("请发送需要删除的违禁词列表，多个使用空格分割")
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ")
                val prohibitedWords = config.prohibitedWords
                arr.forEach(prohibitedWords::remove)
            }
            "黑名单" -> {
                reply("请发送需要删除的黑名单列表，多个使用空格分割")
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ").map { it.toLong() }
                val blackList = config.blackList
                arr.forEach(blackList::remove)
            }
            "问答" -> {
                reply("请发送需要删除的问答的问题")
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ")
                val qaList = config.qaList
                for (s in arr) {
                    qaList.removeIf { it.q == s }
                }
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
            "黑名单" -> {
                config.blackList.forEach { sb.append(it).append(" ") }
                sb.toString()
            }
            "问答" -> {
                config.qaList.forEach { sb.append(it.q).append(" ") }
                sb.toString()
            }
            else -> return null
        }
    }
}

fun Boolean.toStatus() = if (this) Status.ON else Status.OFF

@file:Suppress("DuplicatedCode")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.At
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Text
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@GroupController
@Component
class ManagerController (
    private val groupService: GroupService,
    private val messageService: MessageService,
    @Value("\${yuq.art.master}") private val master: String
) {

    @Before(except = ["operateStatus", "add", "delete", "query"])
    fun before(qq: Member, groupEntity: GroupEntity) {
        val b = groupEntity.config.adminCanExecute == Status.ON && qq.isAdmin()
        val bb = qq.id == master.toLong()
        val bbb = groupEntity.config.adminList.contains(qq.id)
        if (!(b || bb || bbb)) throw mif.at(qq).plus("权限不足，无法执行").toThrowable()
    }

    @Action("{operate} {statusStr}")
    fun operateStatus(operate: String, statusStr: String, groupEntity: GroupEntity, qq: Member): String? {
        val status = statusStr.contains("开")
        val config = groupEntity.config
        when (operate) {
            "复读" -> config.repeat = status.toStatus()
            "撤回通知" -> config.recallNotify = status.toStatus()
            "闪照通知" -> config.flashImageNotify = status.toStatus()
            "退群拉黑" -> config.leaveToBlack = status.toStatus()
            "r18" -> config.loLiConR18 = status.toStatus()
            "进群验证" -> config.entryVerification = status.toStatus()
            "群管权限" -> config.adminCanExecute = status.toStatus()
            else -> return null
        }
        before(qq, groupEntity)
        groupService.save(groupEntity)
        return "${operate}${if (status) "开启" else "关闭"}成功"
    }

    @Action("加{operate}")
    fun add(operate: String, session: ContextSession, groupEntity: GroupEntity, qq: Member, group: Group): String? {
        val config = groupEntity.config
        when (operate) {
            "违禁词" -> {
                group.sendMessage(mif.at(qq).plus("请发送违禁词列表，多个使用空格分割"))
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ")
                config.prohibitedWords.addAll(arr)
            }
            "黑名单" -> {
                group.sendMessage(mif.at(qq).plus("请发送黑名单列表，多个使用空格分割"))
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ").map { it.toLong() }
                config.blackList.addAll(arr)
            }
            "问答" -> {
                group.sendMessage(mif.at(qq).plus("请发送问题"))
                val q = session.waitNextMessage().toCodeString()
                group.sendMessage(mif.at(qq).plus("请发送机器人回复的答案"))
                val a = session.waitNextMessage().toCodeString()
                group.sendMessage(mif.at(qq).plus("请发送数字，1表示精准匹配，2表示模糊匹配"))
                val ss = session.waitNextMessage().firstString()
                val type = if (ss == "1") QaType.EXACT else QaType.FUZZY
                val qa = Qa(q, a, type)
                config.qaList.add(qa)
            }
            "管" -> {
                group.sendMessage(mif.at(qq).plus("请发送qq号或者直接at"))
                val message = session.waitNextMessage()
                val set = mutableSetOf<Long>()
                message.body.forEach {
                    if (it is At) {
                        set.add(it.user)
                    } else if (it is Text) {
                        it.text.toLongOrNull()?.let { qq ->
                            set.add(qq)
                        }
                    }
                }
                config.adminList.addAll(set)
            }
            "拦截" -> {
                group.sendMessage(mif.at(qq).plus("请发送需要拦截的指令"))
                val ss = session.waitNextMessage().firstString()
                groupEntity.config.interceptList.addAll(ss.split(" "))
            }
            "指令限制" -> {
                group.sendMessage(mif.at(qq).plus("请发送需要限制的指令"))
                val command = session.waitNextMessage().firstString()
                group.sendMessage(mif.at(qq).plus("请发送该指令限制的次数（每十分钟限制次数）"))
                val count = session.waitNextMessage().firstString().toIntOrNull() ?: return "您发送的不为数字，退出上下文"
                group.sendMessage(mif.at(qq).plus("请发送该指令限制针对群还是针对群员，1为针对群，2为针对群员"))
                val ty = session.waitNextMessage().firstString().toIntOrNull() ?: return "您发送的不为数字，退出上下文"
                val type = if (ty == 1) CommandLimitType.GROUP else CommandLimitType.QQ
                groupEntity.config.commandLimitList.add(CommandLimit(command, count, type))
            }
            else -> return null
        }
        before(qq, groupEntity)
        groupService.save(groupEntity)
        return "添加${operate}成功"
    }

    @Action("删{operate}")
    fun delete(operate: String, session: ContextSession, groupEntity: GroupEntity, qq: Member, group: Group): String? {
        val config = groupEntity.config
        when (operate) {
            "违禁词" -> {
                group.sendMessage(mif.at(qq).plus("请发送需要删除的违禁词列表，多个使用空格分割"))
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ")
                val prohibitedWords = config.prohibitedWords
                arr.forEach(prohibitedWords::remove)
            }
            "黑名单" -> {
                group.sendMessage(mif.at(qq).plus("请发送需要删除的黑名单列表，多个使用空格分割"))
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ").map { it.toLong() }
                val blackList = config.blackList
                arr.forEach(blackList::remove)
            }
            "问答" -> {
                group.sendMessage(mif.at(qq).plus("请发送需要删除的问答的问题"))
                val str = session.waitNextMessage().firstString()
                val arr = str.split(" ")
                val qaList = config.qaList
                for (s in arr) {
                    qaList.removeIf { it.q == s }
                }
            }
            "管" -> {
                group.sendMessage(mif.at(qq).plus("请发送需要删除的机器人管理，qq号和at都可"))
                val message = session.waitNextMessage()
                val set = mutableSetOf<Long>()
                message.body.forEach {
                    if (it is At) {
                        set.add(it.user)
                    } else if (it is Text) {
                        it.text.toLongOrNull()?.let { qq ->
                            set.add(qq)
                        }
                    }
                }
                config.adminList.removeAll(set)
            }
            "拦截" -> {
                group.sendMessage(mif.at(qq).plus("请发送需要删除的指令"))
                val ss = session.waitNextMessage().firstString()
                config.interceptList.removeAll(ss.split(" ").toSet())
            }
            "指令限制" -> {
                group.sendMessage(mif.at(qq).plus("请发送需要删除的指令"))
                val remove = session.waitNextMessage().firstString().split(" ")
                val removeList = config.commandLimitList.filter { remove.contains(it.command) }
                config.commandLimitList.removeAll(removeList.toSet())
            }
            else -> return null
        }
        before(qq, groupEntity)
        groupService.save(groupEntity)
        return "删除${operate}成功"
    }

    @Action("查{operate}")
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
            "管" -> {
                config.adminList.forEach { sb.append(it).append(" ") }
                sb.toString()
            }
            "拦截" -> {
                config.interceptList.forEach { sb.append(it).append(" ") }
                sb.toString()
            }
            "指令限制" -> {
                config.commandLimitList.forEach {
                    sb.appendLine("${it.command}-${it.type}-${it.limit}")
                }
                sb.toString()
            }
            else -> return null
        }.ifEmpty { "木得" }
    }

    @Action("t {qqNo}")
    fun kick(qqNo: Long, group: Group): String {
        return kotlin.runCatching {
            group[qqNo].kick()
            "踢出成功！"
        }.getOrDefault("踢出失败，可能权限不足！")
    }

    @Action("{day}天未发言")
    fun notSpeak(day: String, group: Group, session: ContextSession, qq: Long): String? {
        val list = mutableListOf<Long>()
        val members = group.members
        for ((k, v) in members) {
            val lastMessageTime = v.lastMessageTime
            if ((System.currentTimeMillis() - lastMessageTime) / (1000 * 60 * 60 * 24) > Integer.parseInt(day)){
                list.add(k);
            }
        }
        group.sendMessage(mif.at(qq).plus("""
            本群${day}天未发言的有${list.size}人
        """.trimIndent()))
        return if (list.isNotEmpty()) {
            group.sendMessage(mif.at(qq).plus("您可以发送<踢出>来踢出本次查询到的未发言人"))
            val ss = session.waitNextMessage().firstString()
            if (ss == "踢出") {
                list.forEach {
                    kotlin.runCatching {
                        members[it]?.kick("${day}天为发言被踢出")
                    }
                }
                "踢出成功"
            } else null
        } else null
    }

    @Action("撤回 {qqNo}")
    fun recall(qqNo: Long, group: Long, @PathVar(value = 2, type = PathVar.Type.Integer) num: Int?): String {
        val list = messageService.findByGroupAndQqOrderByIdDesc(group, qqNo)
        val n = if ((num ?: 1) > list.size) list.size else num ?: 1
        for (i in 0 until n) {
            for (messageEntity in list) {
                messageEntity.messageSource?.recall()
            }
        }
        return "撤回成功"
    }

    @Action("禁言 {qqNo}")
    fun shutUp(qq: Member, qqNo: Long, @PathVar(2) timeStr: String?): String {
        val time = if (timeStr == null) 0
        else {
            val newTime = timeStr.trim()
            if (newTime.length == 1) return "未发言时间单位，单位可为s,m,h,d"
            val num = newTime.substring(0, newTime.length - 1).toIntOrNull() ?: return "发送的时间不为数字"
            when (newTime.last()) {
                's' -> num
                'm' -> num * 60
                'h' -> num * 60 * 60
                'd' -> num * 60 * 60 * 24
                else -> return "禁言时间格式不正确"
            }
        }
        qq.ban(time)
        return "禁言成功"
    }

    @Action("全体禁言 {status}")
    fun allShutUp(status: Boolean, group: Group): String {
        if (status) group.banAll()
        else group.unBanAll()
        return "全体禁言${if (status) "开启" else "关闭"}成功"
    }
}

fun Boolean.toStatus() = if (this) Status.ON else Status.OFF

fun valid(groupEntity: GroupEntity, qq: Member, master: Long): Boolean {
    val b = groupEntity.config.adminCanExecute == Status.ON && qq.isAdmin()
    val bb = qq.id == master
    val bbb = groupEntity.config.adminList.contains(qq.id)
    return b || bb || bbb
}

@file:Suppress("DuplicatedCode")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import me.kuku.yuq.entity.ForwardEntity
import me.kuku.yuq.entity.ForwardService
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@GroupController
class ForwardController @Inject constructor(
    private val forwardService: ForwardService
) {

    private val groupRegex = Regex("[1-9][0-9]+")

    @Action("指令转发")
    @QMsg(at = true, atNewLine = true)
    fun instructionForward(session: ContextSession, group: Group, qq: Long): String {
        group.sendMessage(mif.at(qq).plus("请发送转发指令的群"))
        val groupStr = session.waitNextMessage().firstString()
        if (!groupStr.matches(groupRegex)) return "您发送的群号格式不正确"
        val groupNum = groupStr.toLong()
        if (yuq.groups[groupNum] == null) return "机器人没有加入这个群"
        group.sendMessage(mif.at(qq).plus("请发送转发指令的机器人QQ"))
        val qqStr = session.waitNextMessage().firstString()
        if (!qqStr.matches(groupRegex)) return "您发送的qq号格式不正确"
        val qqNum = qqStr.toLong()
        if (yuq.groups[groupNum]?.get(qqNum) == null) return "这个群里面没有这个人"
        group.sendMessage(mif.at(qq).plus("请发送转发的指令"))
        val instruction = session.waitNextMessage().firstString()
        val forwardEntity = forwardService.findByInstruction(instruction)
            ?: ForwardEntity().also { it.instruction = instruction }
        forwardEntity.group = groupNum
        forwardEntity.qq = qqNum
        forwardService.save(forwardEntity)
        return "添加指令转发成功"
    }

    @Action("指令转发 {groupNum} {qqNum}")
    @QMsg(at = true, atNewLine = true)
    fun instructionForwardAnother(session: ContextSession, group: Group, qq: Long, groupNum: Long, qqNum: Long): String {
        if (yuq.groups[groupNum] == null) return "机器人没有加入这个群"
        if (yuq.groups[groupNum]?.get(qqNum) == null) return "这个群里面没有这个人"
        group.sendMessage(mif.at(qq).plus("请发送转发的指令"))
        val instruction = session.waitNextMessage().firstString()
        val forwardEntity = forwardService.findByInstruction(instruction)
            ?: ForwardEntity().also { it.instruction = instruction }
        forwardEntity.group = groupNum
        forwardEntity.qq = qqNum
        forwardService.save(forwardEntity)
        return "添加指令转发成功"
    }

    @Action("删指令转发 {instruction}")
    fun del(instruction: String): String {
        forwardService.deleteByInstruction(instruction)
        return "删除指令转发成功"
    }

    @Action("查指令转发")
    fun query(): String {
        val sb = StringBuilder("指令转发列表：\n")
        val list = forwardService.findAll()
        list.forEach { sb.append(it.group).append("-").appendLine(it.instruction) }
        return sb.removeSuffix("\n").toString()
    }

}

@EventListener
class ForwardEvent {

    private val forward = ConcurrentHashMap<String, Continuation<Message>>()
    @Inject
    private lateinit var forwardService: ForwardService

    @Event
    fun forward(e: GroupMessageEvent) {
        val group = e.group.id
        val message = kotlin.runCatching { e.message.firstString() }.getOrNull() ?: return
        val list = forwardService.findByInstructionStartsWith(e.message.path[0].toString())
        if (list.isEmpty()) return
        val forwardEntity = list[0]
        val qq = forwardEntity.qq
        val forwardGroup = forwardEntity.group
        if (group == forwardGroup) return
        yuq.groups[forwardGroup]?.sendMessage(message)
        runBlocking {
            try {
                withTimeout(1000 * 30) {
                    val msg = suspendCoroutine<Message> {
                        forward["$forwardGroup$qq"] = it
                    }
                    e.group.sendMessage(msg)
                }
            } catch (e: Exception) {
                forward.remove("$forwardGroup$qq")
            }
        }
    }

    @Event
    fun resumeForward(e: GroupMessageEvent) {
        val group = e.group.id
        val qq = e.sender.id
        forward.remove("$group$qq")?.resume(e.message)
    }

}
package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.*
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.message.buildMessage
import com.icecreamqaq.yuq.mif
import me.kuku.utils.DateTimeFormatterUtils
import me.kuku.utils.JobManager
import me.kuku.utils.MyUtils
import me.kuku.yuq.entity.*
import me.kuku.yuq.transaction
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@EventListener
class GroupManagerEvent @Inject constructor(
    private val groupService: GroupService,
    private val qqGroupService: QqGroupService,
    private val messageService: MessageService
) {

    private val lastMessage = ConcurrentHashMap<Long, String>()
    private val lastQq = ConcurrentHashMap<Long, Long>()
    private val lastRepeatMessage = ConcurrentHashMap<Long, String>()

    private val verifyMap = ConcurrentHashMap<String, Boolean>()

    @Event
    fun inter(e: GroupMessageEvent) = transaction {
        val group = e.group
        val groupNum = group.id
        val sender = e.sender
        val qq = sender.id
        val groupEntity = groupService.findByGroup(groupNum) ?: return@transaction
        val qqEntity = groupEntity.get(qq) ?: return@transaction
        val codeString = e.message.toCodeString()
        val config = groupEntity.config
        val prohibitedWords = config.prohibitedWords
        for (prohibitedWord in prohibitedWords) {
            if (codeString.contains(prohibitedWord)) {
                kotlin.runCatching {
                    sender.ban(60 * 10)
                    val qqGroupEntity = qqGroupService.findByQqGroupId(QqGroupId(qqEntity.id!!, groupEntity.id!!))!!
                    group.sendMessage(mif.at(qq).plus("""
                        您已触发违禁词"$prohibitedWord"，您已被禁言10分钟。
                        您已违规${qqGroupEntity.config.prohibitedCount}次
                    """.trimIndent()))
                    qqGroupEntity.config.prohibitedCount += 1
                    qqGroupService.save(qqGroupEntity)
                    e.cancel = true
                    return@transaction
                }
            }
        }
    }

    @Event(weight = Event.Weight.low)
    fun repeat(e: GroupMessageEvent) {
        val group = e.group
        val groupNum = group.id
        val groupEntity = groupService.findByGroup(groupNum) ?: return
        if (groupEntity.config.repeat == Status.ON) {
            val qq = e.sender.id
            val nowMsg = e.message.toCodeString()
            if (lastMessage.containsKey(groupNum)) {
                val oldMsg = lastMessage[groupNum]
                if (oldMsg == nowMsg && nowMsg != lastRepeatMessage[groupNum] && lastQq[groupNum] != qq) {
                    lastRepeatMessage[groupNum] = nowMsg
                    group.sendMessage(e.message)
                }
            }
            lastMessage[groupNum] = nowMsg
            lastQq[groupNum] = qq
        }
    }

    @Event
    fun memberLeave(e: GroupMemberLeaveEvent) {
        val group = e.group
        val groupNum = group.id
        val member = e.member
        val groupEntity = groupService.findByGroup(groupNum) ?: return
        if (groupEntity.config.leaveToBlack == Status.ON) {
            groupEntity.config.blackList.add(member.id)
            groupService.save(groupEntity)
            group.sendMessage("""
                ${member.nameCardOrName()}离开了我们，他（她）已被加入到黑名单
            """.trimIndent())
        } else {
            group.sendMessage("""
                ${member.nameCardOrName()}离开了我们
            """.trimIndent())
        }
        val messageEntityList = messageService.findByGroupAndQqOrderByIdDesc(groupNum, member.id)
        if (messageEntityList.isEmpty()) {
            group.sendMessage("""
                他（她）好像还没有说过话，那没事了
            """.trimIndent())
        } else {
            val messageEntity = messageEntityList[0]
            group.sendMessage("""
                但是我们永远也不要忘记他（她）在群里的${messageEntity.localDateTime.format(DateTimeFormatterUtils.creat("yyyy-MM-dd HH:mm:ss"))}说的最后一句话
            """.trimIndent())
            group.sendMessage(messageEntity.content.toMessageByRainCode())
        }
    }

    @Event
    fun newRequest(e: NewRequestEvent) {
        val qq: Long
        val groupEntity = when (e) {
            is GroupInviteEvent -> {
                qq = e.qq.id
                val id = e.group.id
                groupService.findByGroup(id)
            }
            is GroupMemberRequestEvent -> {
                qq = e.qq.id
                val id = e.group.id
                groupService.findByGroup(id)
            }
            else -> return
        } ?: return
        val blackList = groupEntity.config.blackList
        if (blackList.contains(qq)) {
            e.accept = false
            e.rejectMessage = "你是黑名单用户，无法加入本群"
            e.cancel = true
        } else {
            e.accept = true
            e.cancel = true
        }
    }

    @Event
    fun friend(e: NewFriendRequestEvent) {

    }

    @Event
    fun verification(e: GroupMemberJoinEvent) {
        val group = e.group
        val member = e.member
        val qq = e.member.id
        val groupEntity = groupService.findByGroup(group.id) ?: return
        if (groupEntity.config.entryVerification == Status.ON) {
            val captchaCode = MyUtils.randomStr(4)
            val message = buildMessage {
                at(qq)
                line()
                textLine("欢迎加入本群，请您发送验证码，如若在5分钟内未发送，将会被移除本群")
                text("验证码为$captchaCode")
            }
            group.sendMessage(message)
            val key = group.id.toString() + qq + captchaCode
            verifyMap[key] = false
            JobManager.delay(1000 * 60 * 5) {
                val status = verifyMap[key] ?: return@delay
                verifyMap.remove(key)
                if (!status) {
                    member.kick("超时未验证")
                    group.sendMessage("${qq}超时未验证，已被移出本群")
                }
            }
        }
    }

    @Event
    fun verify(e: GroupMessageEvent) {
        val group = e.group.id
        val qq = e.sender.id
        val captchaCode = kotlin.runCatching {
            e.message.firstString()
        }.getOrNull() ?: return
        val key = group.toString() + qq + captchaCode
        verifyMap.remove(key)
    }
}


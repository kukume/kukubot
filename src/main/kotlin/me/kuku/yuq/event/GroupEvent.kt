package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.*
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.toMessage
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.GroupQQService
import me.kuku.yuq.service.QQGroupService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@EventListener
class GroupEvent {
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var daoService: DaoService
    @Inject
    private lateinit var groupQQService: GroupQQService

    private val messages = mutableMapOf<Long, Message>()
    private val alreadyMessage = mutableMapOf<Long, Message>()
    private val prefixQQ = mutableMapOf<Long, Long>()

    @Event
    fun repeat(e: GroupMessageEvent){
        val group = e.group.id
        val qq = e.sender.id
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.repeat == false) return
        val nowMessage = e.message
        if (messages.containsKey(group)){
            val oldMessage = messages.getValue(group)
            if (nowMessage.bodyEquals(oldMessage) &&
                    qq != prefixQQ[group] &&
                    !nowMessage.bodyEquals(alreadyMessage[group])){
                e.group.sendMessage(nowMessage)
                alreadyMessage[group] = nowMessage
            }
        }
        prefixQQ[group] = qq
        messages[group] = nowMessage
    }

    @Event
    fun groupMemberRequest(e: GroupMemberRequestEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: return
        if (qqGroupEntity.autoReview == true) {
            var status = true
            val blackJsonArray = qqGroupEntity.getBlackJsonArray()
            for (i in blackJsonArray.indices) {
                val black = blackJsonArray.getLong(i)
                if (black == e.qq) {
                    status = false
                    break
                }
            }
            e.accept = status
            e.cancel = true
        }
    }

    @Event
    fun groupMemberLeave(e: GroupMemberLeaveEvent){
        val qq = e.member.id
        val group = e.group.id
        daoService.delQQ(qq)
        groupQQService.delByQQAndGroup(qq, group)
        val qqGroupEntity = qqGroupService.findByGroup(group) ?: return
        val msg = if (qqGroupEntity.leaveGroupBlack == true) {
            val blackJsonArray = qqGroupEntity.getBlackJsonArray()
            blackJsonArray.add(qq.toString())
            qqGroupEntity.blackList = blackJsonArray.toString()
            qqGroupService.save(qqGroupEntity)
            "刚刚，${e.member.name}退群了，已加入本群黑名单！！"
        }else "刚刚，${e.member.name}离开了我们！！"
        e.group.sendMessage(msg.toMessage())
    }

    @Event
    fun groupMemberKick(e: GroupMemberKickEvent){
        val qq = e.member.id
        val group = e.group.id
        val qqGroupEntity = qqGroupService.findByGroup(group) ?: return
        val blackJsonArray = qqGroupEntity.getBlackJsonArray()
        blackJsonArray.add(qq.toString())
        qqGroupEntity.blackList = blackJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        daoService.delQQ(qq)
        groupQQService.delByQQAndGroup(qq, group)
    }

    @Event
    fun groupMemberJoin(e: GroupMemberJoinEvent){
        val group = e.group.id
        val qq = e.member.id
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.welcomeMsg == true) {
            e.group.sendMessage(
                    mif.at(qq).plus(
                            """
                    欢迎您加入本群
                    您是本群的第${e.group.members.size + 1}位成员
                    您可以愉快的与大家交流啦
                """.trimIndent()
                    ).plus(mif.image("https://q.qlogo.cn/g?b=qq&nk=$qq&s=640")).plus(
                            "一言：${toolLogic.hiToKoTo().getValue("text")}\n" +
                                    SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA).format(Date())
                    )
            )
        }
    }
}
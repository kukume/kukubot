package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.event.*
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mf
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.DaoService
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

    private val messages = mutableMapOf<Long, Message>()
    private val alreadyMessage = mutableMapOf<Long, Message>()

    @Event
    fun repeat(e: GroupMessageEvent){
        val group = e.message.group!!
        val nowMessage = e.message
        if (messages.containsKey(group)){
            val oldMessage = messages.getValue(group)
            if (nowMessage.bodyEquals(oldMessage) &&
                    nowMessage.qq!! != oldMessage.qq!! &&
                    !nowMessage.bodyEquals(alreadyMessage[group])){
                yuq.sendMessage(mf.newGroup(group).plus(nowMessage))
                alreadyMessage[group] = nowMessage
            }
        }
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
        daoService.delQQ(e.member.id)
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: return
        val msg = if (qqGroupEntity.leaveGroupBlack == true) {
            val blackJsonArray = qqGroupEntity.getBlackJsonArray()
            blackJsonArray.add(e.member.id.toString())
            qqGroupEntity.blackList = blackJsonArray.toString()
            qqGroupService.save(qqGroupEntity)
            "刚刚，${e.member.name}退群了，已加入本群黑名单！！"
        }else "刚刚，${e.member.name}离开了我们！！"
        yuq.sendMessage(mf.newGroup(e.group.id).plus(msg))
    }

    @Event
    fun groupMemberKick(e: GroupMemberKickEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: return
        val blackJsonArray = qqGroupEntity.getBlackJsonArray()
        blackJsonArray.add(e.member.id.toString())
        qqGroupEntity.blackList = blackJsonArray.toString()
        qqGroupService.save(qqGroupEntity)
        daoService.delQQ(e.member.id)
    }

    @Event
    fun groupMemberJoin(e: GroupMemberJoinEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id)
        if (qqGroupEntity?.welcomeMsg == true)
            yuq.sendMessage(mf.newGroup(e.group.id).plus(this.welcomeMessage(e.member.id, e.group)))
    }

    private fun welcomeMessage(qq: Long, group: Group): Message {
        return mif.at(qq).plus(
                """
                    欢迎您加入本群
                    您是本群的第${group.members.size + 1}位成员
                    您可以愉快的与大家交流啦
                """.trimIndent()
        ).plus(mif.image("https://q.qlogo.cn/g?b=qq&nk=$qq&s=640")).plus(
                "一言：${toolLogic.hiToKoTo().getValue("text")}\n" +
                        SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA).format(Date())
        )
    }


}
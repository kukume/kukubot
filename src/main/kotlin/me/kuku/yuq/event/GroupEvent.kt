@file:Suppress("unused")

package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.*
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.QQService
import me.kuku.yuq.service.GroupService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@EventListener
class GroupEvent {
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var daoService: DaoService
    @Inject
    private lateinit var qqService: QQService

    @Event
    fun groupMemberRequest(e: GroupMemberRequestEvent){
        val qqGroupEntity = groupService.findByGroup(e.group.id) ?: return
        if (qqGroupEntity.autoReview) {
            var status = true
            val blackJsonArray = qqGroupEntity.blackJsonArray
            for (i in blackJsonArray.indices) {
                val black = blackJsonArray.getLong(i)
                if (black == e.qq.id) {
                    status = false
                    break
                }
            }
            e.accept = status
            e.cancel = true
        }
    }

    @Event
    fun groupMemberLeave(e: GroupMemberLeaveEvent.Leave){
        val qq = e.member.id
        val group = e.group.id
        daoService.delQQ(qq)
        qqService.delByQQAndGroup(qq, group)
        val qqGroupEntity = groupService.findByGroup(group) ?: return
        val msg = if (qqGroupEntity.leaveGroupBlack) {
            val blackJsonArray = qqGroupEntity.blackJsonArray
            blackJsonArray.add(qq.toString())
            qqGroupEntity.blackList = blackJsonArray.toString()
            groupService.save(qqGroupEntity)
            "刚刚，${e.member.name}退群了，已加入本群黑名单！！"
        }else "刚刚，${e.member.name}离开了我们！！"
        e.group.sendMessage(msg.toMessage())
    }

    @Event
    fun groupMemberKick(e: GroupMemberLeaveEvent.Kick){
        val qq = e.member.id
        val group = e.group.id
        val qqGroupEntity = groupService.findByGroup(group) ?: return
        val blackJsonArray = qqGroupEntity.blackJsonArray
        blackJsonArray.add(qq.toString())
        qqGroupEntity.blackList = blackJsonArray.toString()
        groupService.save(qqGroupEntity)
        daoService.delQQ(qq)
        qqService.delByQQAndGroup(qq, group)
    }

    @Event
    fun groupMemberJoin(e: GroupMemberJoinEvent){
        val group = e.group.id
        val qq = e.member.id
        val qqGroupEntity = groupService.findByGroup(group)
        if (qqGroupEntity?.welcomeMsg == true) {
            e.group.sendMessage(
                    mif.at(qq).plus(
                            """
                    欢迎您加入本群
                    您是本群的第${e.group.members.size + 1}位成员
                    您可以愉快的与大家交流啦
                """.trimIndent()
                    ).plus(mif.imageByUrl("https://q.qlogo.cn/g?b=qq&nk=$qq&s=640")).plus(
                            "一言：${toolLogic.hiToKoTo().getValue("text")}\n" +
                                    SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA).format(Date())
                    )
            )
        }
    }
}
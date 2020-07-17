package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.event.GroupMemberJoinEvent
import com.icecreamqaq.yuq.event.GroupMemberLeaveEvent
import com.icecreamqaq.yuq.event.GroupMemberRequestEvent
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mf
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.QQGroupService
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

@EventListener
class GroupEvent {
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var daoService: DaoService

    @Event
    fun groupMemberRequest(e: GroupMemberRequestEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id)
        var status = true
        if (qqGroupEntity != null){
            val list = qqGroupEntity.blackList.removeSuffix("|").split("|")
            for (i in list){
                if (i == e.qq.toString()){
                    status = false
                    break
                }
            }
        }
        e.accept = status
        e.cancel = true
        thread {
            if (status) {
                TimeUnit.SECONDS.sleep(3)
                yuq.sendMessage(mf.newGroup(e.group.id).plus(this.welcomeMessage(e.qq, e.group)))
            }
        }
    }

    @Event
    fun groupMemberLeave(e: GroupMemberLeaveEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: return
        val msg = if (qqGroupEntity.leaveGroupBlack == true) {
            var blackList = qqGroupEntity.blackList
            blackList += "${e.member.id}|"
            qqGroupEntity.blackList = blackList
            qqGroupService.save(qqGroupEntity)
            daoService.delQQ(e.member.id)
            "刚刚，${e.member.name}退群了，已加入本群黑名单！！"
        }else "刚刚，${e.member.name}离开了我们！！"
        yuq.sendMessage(mf.newGroup(e.group.id).plus(msg))
    }

    @Event
    fun groupMemberJoin(e: GroupMemberJoinEvent){
        yuq.sendMessage(mf.newGroup(e.group.id).plus(this.welcomeMessage(e.member.id, e.group)))
    }

    private fun welcomeMessage(qq: Long, group: Group): Message {
        return mif.at(qq).plus(
                """
                    欢迎您加入本群
                    您是本群的第${group.members.size + 1}位成员
                    您可以畅快的与大家交流啦
                    菜单见：https://u.iheit.com/kuku/bot/menu.html
                """.trimIndent()
        ).plus(mif.image("https://q.qlogo.cn/g?b=qq&nk=$qq&s=640")).plus(
                "一言：${toolLogic.hiToKoTo().getValue("text")}\n" +
                        SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA).format(Date())
        )
    }


}
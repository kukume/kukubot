package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.GroupMemberJoinEvent
import com.icecreamqaq.yuq.event.GroupMemberLeaveEvent
import com.icecreamqaq.yuq.event.GroupMemberRequestEvent
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
    }

    @Event
    fun groupMemberLeave(e: GroupMemberLeaveEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: return
        var blackList = qqGroupEntity.blackList
        blackList += "${e.member.id}|"
        qqGroupEntity.blackList = blackList
        qqGroupService.save(qqGroupEntity)
        //删除该qq的信息。。。框架的dao有bug   删个屁
        daoService.delQQ(e.member.id)
        yuq.sendMessage(mf.newGroup(e.group.id).plus("刚刚，${e.member.name}退群了，已加入本群黑名单！！"))
    }

    @Event
    fun groupMemberJoin(e: GroupMemberJoinEvent){
        val message = mif.at(e.member.id).plus(
                """
                    欢迎您加入本群
                    您是本群的第${e.group.members.size + 1}位成员
                    您可以畅快的与大家交流啦
                    菜单见：https://u.iheit.com/kuku/bot/menu.html
                """.trimIndent()
        ).plus(mif.image(e.member.avatar)).plus(
                "一言：${toolLogic.hiToKoTo().getValue("text")}\n" +
                        SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒", Locale.CHINA).format(Date())
        )
        yuq.sendMessage(mf.newGroup(e.group.id).plus(message))
    }


}
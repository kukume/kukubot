package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.BotLeaveGroupEvent
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.event.MessageEvent
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.transaction
import javax.inject.Inject

@EventListener
class Save @Inject constructor(
    private val groupService: GroupService,
    private val qqService: QqService
) {

    @Event(weight = Event.Weight.high)
    fun savePeople(e: MessageEvent) {
        transaction {
            val qq = e.sender.id
            var isSave = false
            val qqEntity = qqService.findByQq(qq) ?: QqEntity().also {
                it.qq = qq
                isSave = true
            }
            if (e is GroupMessageEvent) {
                val group = e.group.id
                if (!qqEntity.groups.any { it.group == group }) {
                    val groupEntity = groupService.findByGroup(group) ?: GroupEntity().also {
                        it.group = group
                        isSave = true
                    }
                    qqEntity.groups.add(groupEntity)
                    isSave = true
                }
            }
            if (isSave) qqService.save(qqEntity)
        }
    }

    @Event
    fun leave(e: BotLeaveGroupEvent) {
        val id = e.group.id
        groupService.deleteByGroup(id)
    }

}
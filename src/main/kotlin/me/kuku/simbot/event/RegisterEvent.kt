@file:Suppress("SpellCheckingInspection")

package me.kuku.simbot.event

import love.forte.simbot.api.message.events.MsgGet
import love.forte.simbot.component.mirai.message.MiraiMessageChainContent
import love.forte.simbot.component.mirai.message.asAccountInfo
import love.forte.simbot.component.mirai.message.event.AbstractMiraiMsgGet
import love.forte.simbot.component.mirai.message.result.MiraiGroupInfo
import love.forte.simbot.component.mirai.utils.registerEventSolver
import net.mamoe.mirai.event.events.*
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


abstract class MiraiMessagePostSendEvent<out ME: MessagePostSendEvent<*>>(event: ME): AbstractMiraiMsgGet<ME>(event){
    override val text = MiraiMessageChainContent(event.message).msg
    val msg = text
    override val accountInfo = event.bot.asAccountInfo()
    override val id = try {
        event.receipt?.source?.ids?.get(0)?.toString() ?: "-1"
    }catch (e: Exception){
        "-1"
    }
}

class MiraiGroupMessagePostSendEvent(event: GroupMessagePostSendEvent): MiraiMessagePostSendEvent<GroupMessagePostSendEvent>(event), MsgGet {
    val groupInfo = MiraiGroupInfo(event.target)
}

class MiraiTempMessagePostSendEvent(event: GroupTempMessagePostSendEvent): MiraiMessagePostSendEvent<GroupTempMessagePostSendEvent>(event){
    val qq = event.target.id
    val groupInfo = MiraiGroupInfo(event.group)
}

class MiraiFriendMessagePostSendEvent(event: FriendMessagePostSendEvent): MiraiMessagePostSendEvent<FriendMessagePostSendEvent>(event){
    val qq = event.target.id
}

@Component
class RegisterMessagePostSendEvent{
    @PostConstruct
    fun registerGroupMessage(){
        registerEventSolver(GroupMessagePostSendEvent::class, MiraiGroupMessagePostSendEvent::class) {
            MiraiGroupMessagePostSendEvent(it)
        }
    }

    @PostConstruct
    fun registerTempMessage(){
        registerEventSolver(GroupTempMessagePostSendEvent::class, MiraiTempMessagePostSendEvent::class) {
            MiraiTempMessagePostSendEvent(it)
        }
    }

    @PostConstruct
    fun registerFriendMessage(){
        registerEventSolver(FriendMessagePostSendEvent::class, MiraiFriendMessagePostSendEvent::class) {
            MiraiFriendMessagePostSendEvent(it)
        }
    }
}
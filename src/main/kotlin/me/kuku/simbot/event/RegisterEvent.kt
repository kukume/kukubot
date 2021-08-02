@file:Suppress("SpellCheckingInspection")

package me.kuku.simbot.event

import love.forte.simbot.api.message.containers.AccountInfo
import love.forte.simbot.api.message.events.MsgGet
import love.forte.simbot.component.mirai.MiraiBotAccountInfo
import love.forte.simbot.component.mirai.message.MiraiFriendAccountInfo
import love.forte.simbot.component.mirai.message.MiraiMemberAccountInfo
import love.forte.simbot.component.mirai.message.MiraiMessageChainContent
import love.forte.simbot.component.mirai.message.asAccountInfo
import love.forte.simbot.component.mirai.message.event.AbstractMiraiMsgGet
import love.forte.simbot.component.mirai.message.result.MiraiGroupInfo
import love.forte.simbot.component.mirai.utils.registerEventSolver
import net.mamoe.mirai.event.events.*
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


abstract class MiraiMessagePostSendEvent<out ME: MessagePostSendEvent<*>>(event: ME): AbstractMiraiMsgGet<ME>(event){
    override val text = MiraiMessageChainContent(event.message).msg
    val msg = text
    override val botInfo = MiraiBotAccountInfo.getInstance(event.bot)
    override val id = try {
        event.receipt?.source?.ids?.get(0)?.toString() ?: "-1"
    }catch (e: Exception){
        "-1"
    }
}

class MiraiGroupMessagePostSendEvent(event: GroupMessagePostSendEvent): MiraiMessagePostSendEvent<GroupMessagePostSendEvent>(event), MsgGet {
    val groupInfo = MiraiGroupInfo(event.target)
    override val accountInfo = event.bot.asAccountInfo()
}

open class MiraiUserMessagePostSendEvent(event: UserMessagePostSendEvent<*>): MiraiMessagePostSendEvent<UserMessagePostSendEvent<*>>(event), MsgGet {
    val qq = event.target.id
    override val accountInfo = event.bot.asAccountInfo()
}

class MiraiTempMessagePostSendEvent(event: GroupTempMessagePostSendEvent): MiraiUserMessagePostSendEvent(event){
    val groupInfo = MiraiGroupInfo(event.group)
    override val accountInfo = MiraiMemberAccountInfo(event.target)
}

class MiraiFriendMessagePostSendEvent(event: FriendMessagePostSendEvent): MiraiUserMessagePostSendEvent(event){
    override val accountInfo = MiraiFriendAccountInfo(event.target)
}

@Component
class RegisterMessagePostSendEvent: BeanPostProcessor{
    @PostConstruct
    fun register(){
        registerEventSolver(GroupMessagePostSendEvent::class, MiraiGroupMessagePostSendEvent::class) {
            MiraiGroupMessagePostSendEvent(it)
        }
        registerEventSolver(GroupTempMessagePostSendEvent::class, MiraiTempMessagePostSendEvent::class) {
            MiraiTempMessagePostSendEvent(it)
        }
        registerEventSolver(FriendMessagePostSendEvent::class, MiraiFriendMessagePostSendEvent::class) {
            MiraiFriendMessagePostSendEvent(it)
        }
        registerEventSolver(UserMessagePostSendEvent::class, MiraiUserMessagePostSendEvent::class) {
            MiraiUserMessagePostSendEvent(it)
        }
    }
}
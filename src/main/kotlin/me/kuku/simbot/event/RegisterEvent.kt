package me.kuku.simbot.event

import love.forte.simbot.api.message.events.MsgGet
import love.forte.simbot.component.mirai.message.MiraiMessageChainContent
import love.forte.simbot.component.mirai.message.asAccountInfo
import love.forte.simbot.component.mirai.message.event.AbstractMiraiMsgGet
import love.forte.simbot.component.mirai.message.result.MiraiGroupInfo
import love.forte.simbot.component.mirai.utils.registerEventSolver
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

class MiraiMessagePostSendEvent(event: GroupMessagePostSendEvent): AbstractMiraiMsgGet<GroupMessagePostSendEvent>(event), MsgGet {
    override val text = MiraiMessageChainContent(event.message).msg
    val msg = text
    val groupInfo = MiraiGroupInfo(event.target)
    override val accountInfo = event.bot.asAccountInfo()
    override val id = try {
        event.receipt?.source?.ids?.get(0).toString() ?: "-1"
    }catch (e: Exception){
        "-1"
    }
}

@Component
class RegisterMessagePostSendEvent: BeanPostProcessor{
    @PostConstruct
    fun register(){
        registerEventSolver(GroupMessagePostSendEvent::class, MiraiMessagePostSendEvent::class) {
            MiraiMessagePostSendEvent(it)
        }
    }
}
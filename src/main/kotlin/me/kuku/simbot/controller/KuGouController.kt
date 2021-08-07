package me.kuku.simbot.controller

import catcode.StringTemplate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import love.forte.simbot.annotation.Filter
import love.forte.simbot.annotation.ListenGroup
import love.forte.simbot.annotation.OnGroup
import love.forte.simbot.api.message.MessageContentBuilderFactory
import love.forte.simbot.api.message.events.GroupMsg
import love.forte.simbot.api.sender.MsgSender
import me.kuku.simbot.annotation.SkipListenGroup
import me.kuku.simbot.entity.KuGouEntity
import me.kuku.simbot.entity.KuGouService
import me.kuku.simbot.entity.QqEntity
import me.kuku.simbot.interceptor.CheckExistInterceptor
import me.kuku.simbot.logic.KuGouLogic
import me.kuku.simbot.logic.ToolLogic
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@OnGroup
@ListenGroup("kuGou")
@Service
class KuGouController(
    private val kuGouService: KuGouService,
    private val kuGouLogic: KuGouLogic,
    private val messageContentBuilderFactory: MessageContentBuilderFactory,
    private val stringTemplate: StringTemplate,
    private val toolLogic: ToolLogic
) {
    @Filter("酷狗二维码")
    @SkipListenGroup
    fun qrcode(groupMsg: GroupMsg, msgSender: MsgSender, qqEntity: QqEntity){
        val qq = groupMsg.accountInfo.accountCodeNumber
        val qrcode = kuGouLogic.getQrcode()
        val messageContent = messageContentBuilderFactory.getMessageContentBuilder()
            .at(qq).image(toolLogic.creatQr(qrcode.url)).text("请使用酷狗音乐APP扫码登录").build()
        msgSender.SENDER.sendGroupMsg(groupMsg, messageContent)
        GlobalScope.launch {
            val msg: String
            while (true){
                delay(3000)
                val result = kuGouLogic.checkQrcode(qrcode)
                if (result.code == 200){
                    val newKuGouEntity = result.data
                    val kuGouEntity = kuGouService.findByQqEntity(qqEntity) ?: KuGouEntity(qqEntity = qqEntity)
                    kuGouEntity.token = newKuGouEntity.token
                    kuGouEntity.userid = newKuGouEntity.userid
                    kuGouService.save(kuGouEntity)
                    msg = stringTemplate.at(qq) + "绑定酷狗音乐成功"
                    break
                }else if (result.code == 500) {
                    msg = stringTemplate.at(qq) + result.message
                    break
                }
            }
            msgSender.SENDER.sendGroupMsg(groupMsg, msg)
        }
    }

    @Filter("酷狗音乐人签到")
    fun musicianSign(kuGouEntity: KuGouEntity): String = kuGouLogic.musicianSign(kuGouEntity).message
}






@Component
class KuGouInterceptor: CheckExistInterceptor<KuGouService>(){
    override val groupRange: Array<String>
        get() = arrayOf("kuGou")

    override fun notExistMsg() = "您没有绑定酷狗账号，请发送<酷狗二维码>进行绑定"
}
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
import me.kuku.simbot.annotation.RegexFilter
import me.kuku.simbot.annotation.SkipListenGroup
import me.kuku.simbot.entity.QqEntity
import me.kuku.simbot.entity.QqMusicEntity
import me.kuku.simbot.entity.QqMusicService
import me.kuku.simbot.interceptor.CheckExistInterceptor
import me.kuku.simbot.logic.QqMusicLogic
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import javax.annotation.Resource

@Service
@OnGroup
@ListenGroup("qqMusic")
class QqMusicController {

    @Resource
    private lateinit var qqMusicService: QqMusicService
    @Resource
    private lateinit var qqMusicLogic: QqMusicLogic
    @Resource
    private lateinit var stringTemplate: StringTemplate
    @Resource
    private lateinit var messageContentBuilderFactory: MessageContentBuilderFactory

    @Filter("qq音乐二维码")
    @SkipListenGroup
    fun getQrcode(groupMsg: GroupMsg, msgSender: MsgSender, qqEntity: QqEntity){
        val qq = groupMsg.accountInfo.accountCodeNumber
        val qrcode = qqMusicLogic.getQrcode()
        val messageContent = messageContentBuilderFactory.getMessageContentBuilder()
            .at(qq).image(qrcode.bytes).text("请使用qq扫码登录qq音乐").build()
        msgSender.SENDER.sendGroupMsg(groupMsg, messageContent)
        GlobalScope.launch {
            val msg: String
            while (true){
                delay(3000)
                val result = qqMusicLogic.checkQrcode(qrcode)
                if (result.code == 200){
                    val newQqMusicEntity = result.data
                    val qqMusicEntity = qqMusicService.findByQqEntity(qqEntity) ?: QqMusicEntity(qqEntity = qqEntity)
                    qqMusicEntity.cookie = newQqMusicEntity.cookie
                    qqMusicEntity.qqMusicKey = newQqMusicEntity.qqMusicKey
                    qqMusicService.save(qqMusicEntity)
                    msg = stringTemplate.at(qq) + "绑定qq音乐成功"
                    break
                }else if (result.code == 500) {
                    msg = stringTemplate.at(qq) + result.message
                    break
                }
            }
            msgSender.SENDER.sendGroupMsg(groupMsg, msg)
        }
    }

    @Filter("qq音乐签到")
    fun qqMusicSign(qqMusicEntity: QqMusicEntity): String{
        val result = qqMusicLogic.sign(qqMusicEntity)
        return result.message
    }

    @Filter("qq音乐人签到")
    fun qqMusicianSign(qqMusicEntity: QqMusicEntity): String{
        val result = qqMusicLogic.musicianSign(qqMusicEntity)
        return result.message
    }

    @RegexFilter("qq音乐发布动态{{content}}")
    fun qqMusicPublishNews(qqMusicEntity: QqMusicEntity, content: String): String{
        return qqMusicLogic.publishNews(qqMusicEntity, content).message
    }

    @RegexFilter("qq音乐随机回复评论{{content}}")
    fun qqMusicRandomComment(qqMusicEntity: QqMusicEntity, content: String): String{
        return qqMusicLogic.randomReplyComment(qqMusicEntity, content).message
    }
}



@Component
class QqMusicInterceptor: CheckExistInterceptor<QqMusicService>(){
    override val groupRange: Array<String>
        get() = arrayOf("qqMusic")

    override fun notExistMsg(): String {
        return "你还没有绑定qq音乐信息，请发送<qq音乐二维码>进行绑定"
    }
}
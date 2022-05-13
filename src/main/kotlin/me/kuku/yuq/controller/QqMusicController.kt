package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.utils.base64Decode
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.QqMusicEntity
import me.kuku.yuq.entity.QqMusicService
import me.kuku.yuq.logic.QqMusicLogic
import me.kuku.yuq.utils.hitokoto
import org.springframework.stereotype.Component

@Component
@GroupController
@PrivateController
class QqMusicController(
    private val qqMusicService: QqMusicService,
    private val qqMusicLogic: QqMusicLogic
) {

    @Before(except = ["login"])
    fun before(qqEntity: QqEntity, context: BotActionContext): QqMusicEntity {
        return qqMusicService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定qq音乐，操作失败").toThrowable()
    }

    @Action("qq音乐登录")
    suspend fun login(session: ContextSession, context: BotActionContext, qqEntity: QqEntity, qq: Long): Any? {
        context.source.sendMessage(mif.at(qq).plus("请发送登录的类型，1为扫码登录，2为密码登录，3为手动绑定"))
        val type = session.waitNextMessage().firstString().toIntOrNull() ?: return "发送的类型不正确"
        val qqMusicEntity = qqMusicService.findByQqEntity(qqEntity) ?: QqMusicEntity().also {
            it.qqEntity = qqEntity
        }
        return when (type) {
            1 -> {
                val qrcode = qqMusicLogic.getQrcode()
                context.source.sendMessage(mif.at(qq).plus(mif.imageByByteArray(qrcode.imageBase.base64Decode())).plus("请使用qq扫码登录qq音乐"))
                while (true) {
                    val result = qqMusicLogic.checkQrcode(qrcode)
                    return when (result.code) {
                        200 -> {
                            val newEntity = result.data
                            qqMusicEntity.qqMusicKey = newEntity.qqMusicKey
                            qqMusicService.save(qqMusicEntity)
                            "绑定qq音乐成功"
                        }
                        0 -> continue
                        else -> result.message
                    }
                }
            }
            2 -> {
                context.source.sendMessage(mif.at(qq).plus("请发送qq密码"))
                val password = session.waitNextMessage().firstString()
                val result = qqMusicLogic.loginByPassword(qq, password)
                if (result.isFailure) {
                    "登录失败，${result.message}"
                } else {
                    qqMusicEntity.cookie = result.data.cookie
                    qqMusicEntity.config.password = password
                    qqMusicService.save(qqMusicEntity)
                    "绑定qq音乐成功"
                }
            }
            3 -> {
                context.source.sendMessage(mif.at(qq).plus("请发送qq音乐的cookie"))
                val cookie = session.waitNextMessage().firstString()
                qqMusicEntity.cookie = cookie
                qqMusicService.save(qqMusicEntity)
                "绑定qq音乐成功"
            }
            else -> {
                null
            }
        }
    }

    @Action("qq音乐签到")
    suspend fun qqMusicSign(qqMusicEntity: QqMusicEntity): String{
        val result = qqMusicLogic.sign(qqMusicEntity)
        return result.message
    }

    @Action("qq音乐人签到")
    suspend fun qqMusicianSign(qqMusicEntity: QqMusicEntity): String{
        val result = qqMusicLogic.musicianSign(qqMusicEntity)
        return result.message
    }

    @Action("qq音乐发布动态")
    suspend fun qqMusicPublishNews(qqMusicEntity: QqMusicEntity): String{
        return qqMusicLogic.publishNews(qqMusicEntity, hitokoto()).message
    }

    @Action("qq音乐随机回复评论")
    suspend fun qqMusicRandomComment(qqMusicEntity: QqMusicEntity): String{
        return qqMusicLogic.randomReplyComment(qqMusicEntity, hitokoto()).message
    }

    @Action("qq音乐回复评论")
    suspend fun qqMusicComment(qqMusicEntity: QqMusicEntity): String{
        return qqMusicLogic.replyComment(qqMusicEntity, hitokoto()).message
    }

    @Action("qq音乐人兑换绿钻")
    suspend fun qqMusicConvert(qqMusicEntity: QqMusicEntity): String = qqMusicLogic.convertGreenDiamond(qqMusicEntity).message

    @Action("qq音乐自动评论 {status}")
    @Synonym(["qq音乐自动发布动态 {status}"])
    fun sss(qqMusicEntity: QqMusicEntity, @PathVar(0) type: String, status: Boolean): String{
        when (type){
            "qq音乐自动评论" -> qqMusicEntity.config.comment = status.toStatus()
            "qq音乐自动发布动态" -> qqMusicEntity.config.view = status.toStatus()
        }
        qqMusicService.save(qqMusicEntity)
        return "$type${if (status) "开启" else "关闭"}成功"
    }

    @Action("qq音乐分享")
    suspend fun qqMusicShare(qqMusicEntity: QqMusicEntity): String{
        val result = qqMusicLogic.shareMusic(qqMusicEntity)
        return if (result.isSuccess) "应该是分享成功了吧！"
        else result.message
    }



}
package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.QqMusicEntity
import me.kuku.yuq.entity.QqMusicService
import me.kuku.yuq.logic.QqMusicLogic
import javax.inject.Inject

@GroupController
class QqMusicController {
    @Inject
    private lateinit var qqMusicService: QqMusicService
    @Inject
    private lateinit var qqMusicLogic: QqMusicLogic

    @Before(except = ["getQrcode"])
    fun before(qqEntity: QqEntity, qq: Long) = qqMusicService.findByQqEntity(qqEntity)
        ?: throw mif.at(qq).plus("你还没有绑定qq音乐信息，请发送<qq音乐二维码>进行绑定").toThrowable()

    @Action("qq音乐二维码")
    @DelicateCoroutinesApi
    fun getQrcode(group: Group, qq: Long, qqEntity: QqEntity){
        val qrcode = qqMusicLogic.getQrcode()
        group.sendMessage(mif.at(qq).plus(mif.imageByByteArray(qrcode.bytes)).plus(
            mif.text("请使用qq扫码登录qq音乐")))
        GlobalScope.launch {
            val msg: Message
            while (true){
                delay(3000)
                val result = qqMusicLogic.checkQrcode(qrcode)
                if (result.code == 200){
                    val newQqMusicEntity = result.data
                    val qqMusicEntity = qqMusicService.findByQqEntity(qqEntity) ?: QqMusicEntity(qqEntity = qqEntity)
                    qqMusicEntity.cookie = newQqMusicEntity.cookie
                    qqMusicEntity.qqMusicKey = newQqMusicEntity.qqMusicKey
                    qqMusicService.save(qqMusicEntity)
                    msg = mif.at(qq).plus("绑定qq音乐成功")
                    break
                }else if (result.code == 500) {
                    msg = mif.at(qq).plus(result.message)
                    break
                }
            }
            group.sendMessage(msg)
        }
    }

    @Action("qq音乐签到")
    @QMsg(at = true)
    fun qqMusicSign(qqMusicEntity: QqMusicEntity): String{
        val result = qqMusicLogic.sign(qqMusicEntity)
        return result.message
    }

    @Action("qq音乐人签到")
    @QMsg(at = true)
    fun qqMusicianSign(qqMusicEntity: QqMusicEntity): String{
        val result = qqMusicLogic.musicianSign(qqMusicEntity)
        return result.message
    }

    @Action("qq音乐发布动态 {content}")
    @QMsg(at = true)
    fun qqMusicPublishNews(qqMusicEntity: QqMusicEntity, content: String): String{
        return qqMusicLogic.publishNews(qqMusicEntity, content).message
    }

    @Action("qq音乐随机回复评论 {content}")
    @QMsg(at = true)
    fun qqMusicRandomComment(qqMusicEntity: QqMusicEntity, content: String): String{
        return qqMusicLogic.randomReplyComment(qqMusicEntity, content).message
    }

}
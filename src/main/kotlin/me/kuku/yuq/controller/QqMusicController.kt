package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.QqMusicEntity
import me.kuku.yuq.entity.QqMusicService
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.logic.QqMusicLogic
import me.kuku.yuq.logic.ToolLogic
import javax.inject.Inject

@GroupController
@PrivateController
class QqMusicController {
    @Inject
    private lateinit var qqMusicService: QqMusicService
    @Inject
    private lateinit var qqMusicLogic: QqMusicLogic
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var qqService: QqService

    @Before(except = ["getQrcode", "bindCookie", "loginByPassword"])
    fun before(qqEntity: QqEntity, qq: Long) = qqMusicService.findByQqEntity(qqEntity)
        ?: throw mif.at(qq).plus("你还没有绑定qq音乐信息，请发送<qq音乐二维码>进行绑定").toThrowable()

    @Action("qq音乐 {cookie}")
    fun bindCookie(qqEntity: QqEntity, cookie: String): String{
        val qqMusicEntity = qqMusicService.findByQqEntity(qqEntity)
            ?: QqMusicEntity(qqEntity = qqEntity)
        val qqMusicKey = OkHttpUtils.getCookie(cookie, "qqmusic_key") ?: ""
        qqMusicEntity.cookie = cookie
        qqMusicEntity.qqMusicKey = qqMusicKey
        qqMusicService.save(qqMusicEntity)
        return "绑定qq音乐成功！"
    }

    @Action("qq音乐登录 {password}")
    @Transactional
    fun loginByPassword(password: String, qq: Long, qqEntity: QqEntity): String{
        val result = qqMusicLogic.loginByPassword(qq, password)
        return if (result.isFailure) result.message
        else {
            val qqMusicEntity = qqMusicService.findByQqEntity(qqEntity) ?: QqMusicEntity(qqEntity = qqEntity)
            val newMusicEntity = result.data
            qqMusicEntity.cookie = newMusicEntity.cookie
            qqMusicEntity.qqMusicKey = newMusicEntity.qqMusicKey
            qqMusicService.save(qqMusicEntity)
            qqEntity.password = password
            qqService.save(qqEntity)
            "绑定qq音乐成功！"
        }
    }

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

    @Action("qq音乐随机回复评论")
    @QMsg(at = true)
    fun qqMusicRandomComment(qqMusicEntity: QqMusicEntity, @PathVar(1) content: String?): String{
        return qqMusicLogic.randomReplyComment(qqMusicEntity, content ?: toolLogic.hiToKoTo()?.get("text") ?: "这也太好听了把！").message
    }

    @Action("qq音乐人兑换绿钻")
    @QMsg(at = true)
    fun qqMusicConvert(qqMusicEntity: QqMusicEntity): String = qqMusicLogic.convertGreenDiamond(qqMusicEntity).message

    @Action("qq音乐人兑换绿钻 {status}")
    @QMsg(at = true)
    fun qqMusicConvertAuto(qqMusicEntity: QqMusicEntity, status: Boolean): String{
        qqMusicEntity.convertGreenDiamond = status
        qqMusicService.save(qqMusicEntity)
        return "qq音乐人每日自动兑换绿钻${if (status) "开启" else "关闭"}成功"
    }

    @Action("删除qq音乐")
    @QMsg(at = true)
    fun delete(qqMusicEntity: QqMusicEntity): String{
        qqMusicService.delete(qqMusicEntity)
        return "删除qq音乐信息成功！"
    }
}
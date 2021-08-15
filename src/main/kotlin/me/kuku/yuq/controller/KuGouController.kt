@file:Suppress("DuplicatedCode")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kuku.yuq.entity.KuGouEntity
import me.kuku.yuq.entity.KuGouService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.KuGouLogic
import me.kuku.yuq.logic.ToolLogic
import javax.inject.Inject

@GroupController
@PrivateController
class KuGouController @Inject constructor(
    private val kuGouService: KuGouService,
    private val kuGouLogic: KuGouLogic,
    private val toolLogic: ToolLogic
) {
    @Before(except = ["qrcode"])
    fun before(qqEntity: QqEntity, qq: Long) = kuGouService.findByQqEntity(qqEntity)
        ?: throw mif.at(qq).plus("您没有绑定酷狗账号，请发送<酷狗二维码>进行绑定").toThrowable()

    @Action("酷狗 {username} {password}")
    fun loginByPassword(qqEntity: QqEntity, username: String, password: String): String{
        var kuGouEntity = kuGouService.findByQqEntity(qqEntity)
        val mid = kuGouEntity?.mid ?: kuGouLogic.mid()
        val result = kuGouLogic.login(username, password, mid)
        return if (result.isSuccess){
            if (kuGouEntity == null) kuGouEntity = KuGouEntity(qqEntity = qqEntity)
            val newKuGouEntity = result.data
            kuGouEntity.token = newKuGouEntity.token
            kuGouEntity.userid = newKuGouEntity.userid
            kuGouEntity.mid = mid
            kuGouEntity.kuGoo = newKuGouEntity.kuGoo
            kuGouService.save(kuGouEntity)
            "绑定酷狗音乐成功！"
        }else "绑定酷狗音乐失败：${result.message}"
    }

    @Action("酷狗 {phone}")
    fun loginByCode(phone: String, session: ContextSession, qq: Contact, qqEntity: QqEntity): String{
        val mid = kuGouLogic.mid()
        val re = kuGouLogic.sendMobileCode(phone, mid)
        return if (re.isFailure) "酷狗发送短信验证码失败：" + re.message
        else {
            qq.sendMessage("请输入验证码！".toMessage())
            val nextMessage = session.waitNextMessage(1000 * 60 * 2)
            val code = nextMessage.firstString()
            val result = kuGouLogic.verifyCode(phone, code, mid)
            if (result.isSuccess){
                val newKuGouEntity = result.data
                val kuGouEntity = kuGouService.findByQqEntity(qqEntity) ?: KuGouEntity(qqEntity = qqEntity)
                kuGouEntity.token = newKuGouEntity.token
                kuGouEntity.userid = newKuGouEntity.userid
                kuGouEntity.mid = mid
                kuGouEntity.kuGoo = newKuGouEntity.kuGoo
                kuGouService.save(kuGouEntity)
                "绑定酷狗音乐成功"
            }else result.message
        }
    }

    @DelicateCoroutinesApi
    @Action("酷狗二维码")
    @QMsg(at = true)
    fun qrcode(group: Group, qqEntity: QqEntity, qq: Long){
        val qrcode = kuGouLogic.getQrcode()
        group.sendMessage(mif.at(qq).plus(mif.imageByInputStream(toolLogic.creatQr(qrcode.url)).plus("请使用酷狗音乐APP扫码登录")))
        GlobalScope.launch {
            val msg: Message
            while (true){
                delay(3000)
                val result = kuGouLogic.checkQrcode(qrcode)
                if (result.code == 200){
                    val newKuGouEntity = result.data
                    val kuGouEntity = kuGouService.findByQqEntity(qqEntity) ?: KuGouEntity(qqEntity = qqEntity)
                    kuGouEntity.token = newKuGouEntity.token
                    kuGouEntity.userid = newKuGouEntity.userid
                    kuGouEntity.mid = newKuGouEntity.mid
                    kuGouEntity.kuGoo = newKuGouEntity.kuGoo
                    kuGouService.save(kuGouEntity)
                    msg = mif.at(qq).plus("绑定酷狗音乐成功")
                    break
                }else if (result.code == 500) {
                    msg = mif.at(qq).plus(result.message)
                    break
                }
            }
            group.sendMessage(msg)
        }
    }

    @Action("酷狗音乐人签到")
    @QMsg(at = true)
    fun musicianSign(kuGouEntity: KuGouEntity): String {
        return kuGouLogic.musicianSign(kuGouEntity).message
    }
}
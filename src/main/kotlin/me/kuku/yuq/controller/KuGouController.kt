package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.KuGouEntity
import me.kuku.yuq.entity.KuGouService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.KuGouLogic
import org.springframework.stereotype.Component

@GroupController
@PrivateController
@Component
class KuGouController (
    private val kuGouLogic: KuGouLogic,
    private val kuGouService: KuGouService
) {

    @Action("酷狗登录")
    suspend fun kuGouLogin(context: BotActionContext, session: ContextSession, qqEntity: QqEntity): String {
        context.source.sendMessage("请发送手机号")
        val phone = session.waitNextMessage().firstString().toLongOrNull() ?: return "发送的手机号有误"
        val kuGouEntity = kuGouService.findByQqEntity(qqEntity) ?: KuGouEntity().also {
            it.mid = kuGouLogic.mid()
            it.qqEntity = qqEntity
        }
        val mid = kuGouEntity.mid
        val result = kuGouLogic.sendMobileCode(phone.toString(), mid)
        return if (result.isSuccess) {
            context.source.sendMessage("请发送短信验证码")
            val code = session.waitNextMessage(1000 * 60 * 2).firstString()
            val verifyResult = kuGouLogic.verifyCode(phone.toString(), code, mid)
            if (verifyResult.isSuccess) {
                val newKuGouEntity = verifyResult.data
                kuGouEntity.kuGoo = newKuGouEntity.kuGoo
                kuGouEntity.token = newKuGouEntity.token
                kuGouEntity.userid = newKuGouEntity.userid
                kuGouService.save(kuGouEntity)
                "绑定成功"
            } else verifyResult.message
        } else result.message
    }

    @Before(except = ["kuGouLogin"])
    fun before(qqEntity: QqEntity): KuGouEntity {
        val kuGouEntity = kuGouService.findByQqEntity(qqEntity)
        return kuGouEntity ?: throw mif.at(qqEntity.qq).plus("您没有绑定酷狗音乐账号，操作失败").toThrowable()
    }

    @Action("酷狗音乐人签到")
    suspend fun kuGouMusicianSign(kuGouEntity: KuGouEntity): String {
        val ss = kuGouLogic.musicianSign(kuGouEntity)
        return if (ss.isSuccess) "酷狗音乐人签到成功" else "酷狗音乐人签到失败，${ss.message}"
    }

    @Action("酷狗听歌")
    suspend fun kuGouListenMusic(kuGouEntity: KuGouEntity): String {
        val ss = kuGouLogic.listenMusic(kuGouEntity)
        return if (ss.isSuccess) "酷狗听歌成功" else "酷狗听歌失败，${ss.message}"
    }

    @Action("酷狗自动签到 {status}")
    fun autoSign(status: Boolean, kuGouEntity: KuGouEntity): String {
        kuGouEntity.config.sign = status.toStatus()
        kuGouService.save(kuGouEntity)
        return "酷狗音乐自动签到${if (status) "开启" else "关闭"}成功"
    }

}
package me.kuku.mirai.subscribe

import me.kuku.mirai.entity.KuGouEntity
import me.kuku.mirai.entity.KuGouService
import me.kuku.mirai.entity.toStatus
import me.kuku.mirai.logic.KuGouLogic
import me.kuku.mirai.utils.MessageSubscribe
import me.kuku.mirai.utils.at
import me.kuku.mirai.utils.firstArg
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import org.springframework.stereotype.Component

@Component
class KuGouSubscribe(
    private val kuGouService: KuGouService
) {

    suspend fun MessageSubscribe.login() {
        "酷狗登录" atReply  {
            val kuGouEntity = kuGouService.findByQq(sender.id) ?: KuGouEntity().apply {
                mid = KuGouLogic.mid()
                qq = sender.id
            }
            subject.sendMessage(at() + "请发送手机号")
            val phone = nextMessage(30000) { it.message.contentToString().length == 11 }.content
            val mid = kuGouEntity.mid
            val result = KuGouLogic.sendMobileCode(phone, mid)
            if (result.success()) {
                val code = nextMessage(1000 * 60 * 2).content
                val res = KuGouLogic.verifyCode(phone, code, mid)
                if (res.success()) {
                    val newEntity = res.data()
                    kuGouEntity.kuGoo = newEntity.kuGoo
                    kuGouEntity.token = newEntity.token
                    kuGouEntity.userid = newEntity.userid
                    kuGouService.save(kuGouEntity)
                    "绑定酷狗音乐成功"
                } else "酷狗音乐登录验证验证码失败，${res.message}"
            } else "酷狗音乐登录发送验证码失败，${result.message}"
        }
    }

    suspend fun MessageSubscribe.manager() {
        before {
            set(kuGouService.findByQq(sender.id) ?: error("未绑定酷狗音乐"))
        }
        "酷狗音乐人签到" atReply  {
            KuGouLogic.musicianSign(firstAttr())
        }
        "酷狗听歌" atReply {
            KuGouLogic.listenMusic(firstAttr())
        }
        regex("酷狗音乐人签到 开|关") atReply {
            val kuGouEntity = firstAttr<KuGouEntity>()
            kuGouEntity.sign = firstArg<PlainText>().content.toStatus()
            kuGouService.save(kuGouEntity)
            "酷狗音乐人签到状态更新成功"
        }
    }


}

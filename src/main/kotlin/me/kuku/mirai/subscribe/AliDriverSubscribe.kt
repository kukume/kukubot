package me.kuku.mirai.subscribe

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import me.kuku.mirai.entity.AliDriverEntity
import me.kuku.mirai.entity.AliDriverService
import me.kuku.mirai.entity.toStatus
import me.kuku.mirai.logic.AliDriverLogic
import me.kuku.mirai.utils.MiraiSubscribe
import me.kuku.mirai.utils.at
import me.kuku.mirai.utils.firstArg
import me.kuku.utils.client
import me.kuku.utils.toUrlEncode
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class AliDriverSubscribe(
    private val aliDriverService: AliDriverService
) {

    suspend fun MiraiSubscribe<MessageEvent>.login() {

        "阿里云盘登录" atReply  {
            val qrcode = AliDriverLogic.login1()
            client.get("https://api.kukuqaq.com/qrcode?text=${qrcode.qrcodeUrl.toUrlEncode()}").body<InputStream>().toExternalResource().use {
                val messageChain = subject.uploadImage(it) + "请使用阿里云盘app扫码登录"
                subject.sendMessage(at() + messageChain)
            }
            var i = 0
            while (true) {
                if (++i > 20) {
                    subject.sendMessage(at() + "阿里云盘登陆二维码已过期")
                    break
                }
                delay(3000)
                val commonResult = AliDriverLogic.login2(qrcode)
                if (commonResult.success()) {
                    val data = commonResult.data()
                    val refreshToken = data.refreshToken
                    val aliDriverEntity = aliDriverService.findByQq(sender.id) ?: AliDriverEntity().also {
                        it.qq = sender.id
                    }
                    aliDriverEntity.refreshToken = refreshToken
                    aliDriverService.save(aliDriverEntity)
                    subject.sendMessage(at() + "绑定阿里云盘成功")
                    break
                } else if (commonResult.code != 0) {
                    subject.sendMessage(at() + commonResult.message)
                    break
                }
            }

        }

    }

    suspend fun MiraiSubscribe<MessageEvent>.manager() {
        before {
            set(aliDriverService.findByQq(sender.id) ?: error("未绑定阿里云盘"))
        }
        "阿里云盘签到" atReply {
            val aliDriverEntity = firstAttr<AliDriverEntity>()
            AliDriverLogic.sign(aliDriverEntity)
        }
        regex("阿里云盘签到 开|关") atReply {
            val aliDriverEntity = firstAttr<AliDriverEntity>()
            aliDriverEntity.sign = firstArg<PlainText>().content.toStatus()
            aliDriverService.save(aliDriverEntity)
            "更新阿里云盘状态成功"
        }
        "阿里云盘删除" atReply {
            aliDriverService.delete(firstAttr())
            "删除阿里云盘成功"
        }
    }


}

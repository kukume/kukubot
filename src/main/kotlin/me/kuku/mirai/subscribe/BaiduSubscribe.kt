package me.kuku.mirai.subscribe

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import me.kuku.mirai.entity.BaiduEntity
import me.kuku.mirai.entity.BaiduService
import me.kuku.mirai.entity.toStatus
import me.kuku.mirai.logic.BaiduLogic
import me.kuku.mirai.utils.MessageSubscribe
import me.kuku.mirai.utils.at
import me.kuku.mirai.utils.firstArg
import me.kuku.utils.client
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class BaiduSubscribe(
    private val baiduLogic: BaiduLogic,
    private val baiduService: BaiduService
) {

    suspend fun MessageSubscribe.login() {
        "百度登录" atReply {
            val qrcode = baiduLogic.getQrcode()
            client.get(qrcode.image).body<InputStream>().toExternalResource().use {
                val image = subject.uploadImage(it)
                subject.sendMessage(at() + image + "请使用百度系app扫码登录（百度网盘，百度贴吧等均可）")
            }
            val baiduEntity = baiduService.findByQq(sender.id) ?: BaiduEntity().apply {
                qq = sender.id
            }
            var i = 0
            while (true) {
                if (++i > 10) error("百度二维码已超时")
                delay(3000)
                try {
                    val result = baiduLogic.checkQrcode(qrcode)
                    if (result.success()) {
                        val newEntity = result.data()
                        baiduEntity.cookie = newEntity.cookie
                        baiduService.save(baiduEntity)
                        subject.sendMessage(at() + "绑定百度成功")
                        break
                    }
                } catch (ignore: Exception) {}
            }
        }
    }

    suspend fun MessageSubscribe.manager() {
        before {
            set(baiduService.findByQq(sender.id) ?: "未绑定百度")
        }
        "贴吧签到" atReply {
            baiduLogic.tieBaSign(firstAttr())
        }
        "游帮帮加速器签到" atReply {
            baiduLogic.ybbSign(firstAttr())
        }
        "游帮帮加速器看广告" atReply {
            baiduLogic.ybbWatchAd(firstAttr())
            baiduLogic.ybbWatchAd(firstAttr(), "v3")
        }
        regex("百度签到 开|关") atReply {
            val baiduEntity = firstAttr<BaiduEntity>()
            baiduEntity.sign = firstArg<PlainText>().content.toStatus()
            "百度签到状态更新成功"
        }
    }

}

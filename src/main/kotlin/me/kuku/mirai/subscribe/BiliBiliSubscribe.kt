package me.kuku.mirai.subscribe

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import me.kuku.mirai.entity.BiliBiliEntity
import me.kuku.mirai.entity.BiliBiliService
import me.kuku.mirai.entity.toStatus
import me.kuku.mirai.logic.BiliBiliLogic
import me.kuku.mirai.utils.MessageSubscribe
import me.kuku.mirai.utils.at
import me.kuku.mirai.utils.firstArg
import me.kuku.utils.client
import me.kuku.utils.toUrlEncode
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
class BiliBiliSubscribe(
    private val biliBiliService: BiliBiliService
) {

    suspend fun MessageSubscribe.login() {
        "哔哩哔哩登录" atReply {
            val qrCodeUrl = BiliBiliLogic.loginByQr1()
            client.get("https://api.kukuqaq.com/qrcode?text=${qrCodeUrl.toUrlEncode()}").body<InputStream>().toExternalResource().use {
                val image = subject.uploadImage(it)
                subject.sendMessage(at() + image + "请使用哔哩哔哩app扫码登录")
            }
            var i = 0
            while (true) {
                if (i++ > 20) error("哔哩哔哩二维码已失效")
                delay(3000)
                val result = BiliBiliLogic.loginByQr2(qrCodeUrl)
                when (result.code) {
                    0 -> continue
                    200 -> {
                        val newEntity = result.data()
                        val biliBiliEntity = biliBiliService.findByQq(sender.id) ?: BiliBiliEntity().also { entity ->
                            entity.qq = sender.id
                        }
                        biliBiliEntity.cookie = newEntity.cookie
                        biliBiliEntity.userid = newEntity.userid
                        biliBiliEntity.token = newEntity.token
                        biliBiliService.save(biliBiliEntity)
                        subject.sendMessage(at() + "绑定哔哩哔哩成功")
                        break
                    }
                    else -> {
                        subject.sendMessage(at() + "绑定哔哩哔哩失败，${result.message}")
                        break
                    }
                }
            }
        }

    }


    suspend fun MessageSubscribe.manager() {
        before {
            set(biliBiliService.findByQq(sender.id) ?: error("未绑定哔哩哔哩"))
        }
        "哔哩哔哩签到" atReply {
            val biliBiliEntity = firstAttr<BiliBiliEntity>()
            val firstRank = BiliBiliLogic.ranking()[0]
            BiliBiliLogic.report(biliBiliEntity, firstRank.aid, firstRank.cid, 300)
            BiliBiliLogic.share(biliBiliEntity, firstRank.aid)
            BiliBiliLogic.liveSign(biliBiliEntity)
            "哔哩哔哩签到成功"
        }
        regex("哔哩哔哩签到 开|关") atReply {
            val biliBiliEntity = firstAttr<BiliBiliEntity>()
            biliBiliEntity.sign = firstArg<PlainText>().content.toStatus()
            "哔哩哔哩状态更新成功"
        }
        regex("哔哩哔哩推送 开|关") atReply {
            val biliBiliEntity = firstAttr<BiliBiliEntity>()
            biliBiliEntity.push = firstArg<PlainText>().content.toStatus()
            "哔哩哔哩状态更新成功"
        }
        regex("哔哩哔哩开播提醒 开|关") atReply {
            val biliBiliEntity = firstAttr<BiliBiliEntity>()
            biliBiliEntity.live = firstArg<PlainText>().content.toStatus()
            "哔哩哔哩状态更新成功"
        }
    }

}

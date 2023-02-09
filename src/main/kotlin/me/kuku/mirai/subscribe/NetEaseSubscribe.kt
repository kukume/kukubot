package me.kuku.mirai.subscribe

import kotlinx.coroutines.delay
import me.kuku.mirai.entity.NetEaseEntity
import me.kuku.mirai.entity.NetEaseService
import me.kuku.mirai.entity.toStatus
import me.kuku.mirai.logic.NetEaseLogic
import me.kuku.mirai.utils.MessageSubscribe
import me.kuku.mirai.utils.at
import me.kuku.mirai.utils.firstArg
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.nextMessage
import org.springframework.stereotype.Component

@Component
class NetEaseSubscribe(
    private val netEaseService: NetEaseService
) {

    suspend fun MessageSubscribe.login() {
        "网易云音乐扫码登录" atReply {

        }

        "网易云音乐密码登录" atReply  {
            subject.sendMessage(at() + "请发送账号")
            val account = nextMessage(30000).content
            subject.sendMessage(at() + "请发送密码")
            val password = nextMessage(30000).content
            val result = NetEaseLogic.login(account, password)
            if (result.success()) {
                val newEntity = result.data()
                val netEaseEntity = netEaseService.findByQq(sender.id) ?: NetEaseEntity().also { it.qq = sender.id }
                netEaseEntity.csrf = newEntity.csrf
                netEaseEntity.musicU = newEntity.musicU
                netEaseService.save(netEaseEntity)
                "绑定网易云音乐成功"
            } else "登录网易云音乐失败，${result.message}"
        }

    }

    suspend fun MessageSubscribe.manager() {
        before {
            set(netEaseService.findByQq(sender.id) ?: error("未绑定网易云音乐"))
        }

        "网易云音乐签到" atReply {
            val result = NetEaseLogic.sign(firstAttr())
            if (result.success()) {
                NetEaseLogic.listenMusic(firstAttr())
                "网易云音乐签到成功"
            } else {
                "网易云音乐签到失败，${result.message}"
            }
        }

        regex("网易云音乐签到 开|关") atReply {
            val entity: NetEaseEntity = firstAttr()
            entity.sign = firstArg<PlainText>().content.toStatus()
            netEaseService.save(entity)
            "网易云音乐签到状态更新成功"
        }

        regex("网易云音乐人签到 开|关") atReply {
            val entity: NetEaseEntity = firstAttr()
            entity.musicianSign = firstArg<PlainText>().content.toStatus()
            netEaseService.save(entity)
            "网易云音乐签到状态更新成功"
        }

        "网易云音乐人签到" atReply {
            val result = NetEaseLogic.musicianSign(firstAttr())
            if (result.success()) {
                delay(3000)
                NetEaseLogic.publish(firstAttr())
                delay(3000)
                NetEaseLogic.publishMLog(firstAttr())
                "网易云音乐人签到成功"
            } else {
                "网易云音乐人签到失败，${result.message}"
            }
        }

    }


}

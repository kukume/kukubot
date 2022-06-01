package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.delay
import me.kuku.utils.toUrlEncode
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.yuq.entity.BiliBiliService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.utils.openOrClose
import org.springframework.stereotype.Component

@GroupController
@PrivateController
@Component
class BiliBiliController (
    private val biliBiliService: BiliBiliService
) {

    @Action("哔哩哔哩登录")
    suspend fun login(qqEntity: QqEntity, context: BotActionContext) {
        val qr = BiliBiliLogic.loginByQr1()
        context.source.sendMessage(mif.at(qqEntity.qq).plus(mif.imageByUrl("https://api.pwmqr.com/qrcode/create/?url=${qr.toUrlEncode()}")).plus("请使用哔哩哔哩APP扫码登录").toMessage())
        while (true) {
            delay(3000)
            val result = BiliBiliLogic.loginByQr2(qr)
            when (result.code) {
                0 -> continue
                200 -> {
                    val newEntity = result.data()
                    val biliBiliEntity = biliBiliService.findByQqEntity(qqEntity) ?: BiliBiliEntity().also {
                        it.qqEntity = qqEntity
                    }
                    biliBiliEntity.cookie = newEntity.cookie
                    biliBiliEntity.userid = newEntity.userid
                    biliBiliEntity.token = newEntity.token
                    biliBiliService.save(biliBiliEntity)
                    context.source.sendMessage(mif.at(qqEntity.qq).plus("绑定哔哩哔哩成功"))
                    break
                }
                else -> {
                    context.source.sendMessage(mif.at(qqEntity.qq).plus(result.message))
                    break
                }
            }
        }
    }

    @Before(except = ["login"])
    fun before(qqEntity: QqEntity): BiliBiliEntity {
        return biliBiliService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("没有绑定哔哩哔哩，操作失败").toThrowable()
    }

    @Action("哔哩哔哩签到")
    suspend fun sign(biliBiliEntity: BiliBiliEntity): String {
        val firstRank = BiliBiliLogic.ranking()[0]
        BiliBiliLogic.report(biliBiliEntity, firstRank.aid, firstRank.cid, 300)
        BiliBiliLogic.share(biliBiliEntity, firstRank.aid)
        BiliBiliLogic.liveSign(biliBiliEntity)
        return "哔哩哔哩签到成功"
    }

    @Action("哔哩哔哩开播提醒 {status}")
    fun biliBiliLive(status: Boolean, biliBiliEntity: BiliBiliEntity): String {
        biliBiliEntity.config.live = status.toStatus()
        biliBiliService.save(biliBiliEntity)
        return "哔哩哔哩开播提醒${status.openOrClose()}成功"
    }

    @Action("哔哩哔哩自动签到 {status}")
    fun biliBiliAutoSign(status: Boolean, biliBiliEntity: BiliBiliEntity): String {
        biliBiliEntity.config.sign = status.toStatus()
        biliBiliService.save(biliBiliEntity)
        return "哔哩哔哩自动签到${status.openOrClose()}成功"
    }

    @Action("哔哩哔哩自动投币 {status}")
    fun autoCoin(status: Boolean, biliBiliEntity: BiliBiliEntity): String {
        biliBiliEntity.config.coin = status.toStatus()
        biliBiliService.save(biliBiliEntity)
        return "哔哩哔哩自动投币${status.openOrClose()}成功"
    }

    @Action("哔哩哔哩推送 {status}")
    fun biliBiliPush(status: Boolean, biliBiliEntity: BiliBiliEntity): String {
        biliBiliEntity.config.push = status.toStatus()
        biliBiliService.save(biliBiliEntity)
        return "哔哩哔哩推送${status.openOrClose()}成功"
    }

}
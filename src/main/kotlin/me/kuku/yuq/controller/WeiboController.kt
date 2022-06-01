package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.delay
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.entity.WeiboService
import me.kuku.yuq.logic.WeiboLogic
import org.springframework.stereotype.Component

@GroupController
@PrivateController
@Component
class WeiboController (
    private val weiboService: WeiboService
) {

    @Action("微博登录")
    suspend fun login(qqEntity: QqEntity, context: BotActionContext) {
        val weiboQrcode = WeiboLogic.loginByQr1()
        val url = weiboQrcode.url
        val bytes = OkHttpUtils.getBytes("https:$url")
        context.source.sendMessage(mif.at(qqEntity.qq).plus(mif.imageByByteArray(bytes)).plus("请使用微博APP扫码登录（第三方微博也可以）").toMessage())
        while (true) {
            delay(3000)
            val result = WeiboLogic.loginByQr2(weiboQrcode)
            if (result.success()) {
                val newWeiboEntity = result.data()
                val weiboEntity = weiboService.findByQqEntity(qqEntity) ?: WeiboEntity().also {
                    it.qqEntity = qqEntity
                }
                weiboEntity.pcCookie = newWeiboEntity.pcCookie
                weiboEntity.mobileCookie = newWeiboEntity.mobileCookie
                weiboService.save(weiboEntity)
                context.source.sendMessage(mif.at(qqEntity.qq).plus("绑定微博成功"))
                break
            } else if (result.code in listOf(201, 202)) continue
            else {
                context.source.sendMessage(mif.at(qqEntity.qq).plus(result.message))
                break
            }
        }
    }

    @Before(except = ["login"])
    fun before(qqEntity: QqEntity): WeiboEntity {
        return weiboService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您未绑定微博账号，操作失败").toThrowable()
    }

    @Action("微博超话签到")
    suspend fun superSign(weiboEntity: WeiboEntity): String {
        val result = WeiboLogic.superTalkSign(weiboEntity)
        return if (result.success()) "微博超话签到成功"
        else "微博超话签到失败，${result.message}"
    }

    @Action("微博推送 {status}")
    fun weiboPush(status: Boolean, weiboEntity: WeiboEntity): String {
        weiboEntity.config.push = status.toStatus()
        weiboService.save(weiboEntity)
        return "微博推送${if (status) "开启" else "关闭"}成功"
    }

    @Action("微博自动签到 {status}")
    fun weiboAutoSign(status: Boolean, weiboEntity: WeiboEntity): String {
        weiboEntity.config.sign = status.toStatus()
        weiboService.save(weiboEntity)
        return "微博自动签到${if (status) "开启" else "关闭"}成功"
    }


}
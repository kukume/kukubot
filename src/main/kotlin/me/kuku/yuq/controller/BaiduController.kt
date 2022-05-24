package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.withTimeoutOrNull
import me.kuku.utils.MyUtils
import me.kuku.yuq.entity.BaiduEntity
import me.kuku.yuq.entity.BaiduService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.BaiduLogic
import me.kuku.yuq.utils.openOrClose
import org.springframework.stereotype.Service

@GroupController
@PrivateController
@Service
class BaiduController (
    private val baiduService: BaiduService,
    private val baiduLogic: BaiduLogic
) {

    @Action("百度登录")
    suspend fun baiduLogin(qqEntity: QqEntity, context: BotActionContext, session: ContextSession): String {
        context.source.sendMessage(mif.at(qqEntity.qq).plus("请发送登陆方式，1为扫码登陆，2为手动绑定"))
        val baiduEntity = baiduService.findByQqEntity(qqEntity) ?: BaiduEntity().also {
            it.qqEntity = qqEntity
        }
        return when (session.waitNextMessage().firstString().toIntOrNull()) {
            1 -> {
                val qrcode = baiduLogic.getQrcode()
                context.source.sendMessage(mif.at(qqEntity.qq).plus(mif.imageByUrl(qrcode.image).plus("请使用百度APP扫码登陆，百度网盘等均可")))
                withTimeoutOrNull(1000 * 60 * 2) {
                    while (true) {
                        try {
                            val result = baiduLogic.checkQrcode(qrcode)
                            if (result.success()) {
                                val newEntity = result.data()
                                baiduEntity.cookie = newEntity.cookie
                                baiduService.save(baiduEntity)
                                return@withTimeoutOrNull "绑定百度成功"
                            }
                        } catch (ignore: Exception) {}
                    }
                } as? String ?: ""
            }
            2 -> {
                context.source.sendMessage(mif.at(qqEntity.qq).plus("请发送百度的cookie，cookie中应包含BDUSS和STOKEN"))
                val cookie = session.waitNextMessage().toString()
                val bdUss = MyUtils.regex("BDUSS=", ";", cookie)
                val sToken= MyUtils.regex("STOKEN=", ";", cookie)
                if (bdUss == null || sToken == null) {
                    return "cookie格式不正确，cookie应包含BDUSS和BAIDUID"
                }
                baiduEntity.cookie = "BDUSS=$bdUss; STOKEN=$sToken; "
                baiduService.save(baiduEntity)
                "绑定百度成功"
            }
            else -> "已退出"
        }
    }

    @Before(except = ["baiduLogin"])
    fun before(qqEntity: QqEntity): BaiduEntity {
        return baiduService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定百度，操作失败").toThrowable()
    }

    @Action("百度贴吧签到")
    suspend fun tieBaSign(baiduEntity: BaiduEntity): String {
        val result = baiduLogic.tieBaSign(baiduEntity)
        return if (result.isSuccess) "百度贴吧签到成功"
        else "百度贴吧签到失败，${result.message}"
    }

    @Action("百度自动签到 {status}")
    fun baiduAutoSign(baiduEntity: BaiduEntity, status: Boolean): String {
        baiduEntity.config.sign = status.toStatus()
        baiduService.save(baiduEntity)
        return "百度自动签到${status.openOrClose()}成功"
    }

    @Action("游帮帮看广告")
    suspend fun watchAd(baiduEntity: BaiduEntity): String {
        val result = baiduLogic.ybbWatchAd(baiduEntity)
        return if (result.isSuccess) "游帮帮观看广告成功"
        else "游帮帮观看广告失败，${result.message}"
    }

    @Action("游帮帮签到")
    suspend fun ybbSign(baiduEntity: BaiduEntity): String {
        val result = baiduLogic.ybbSign(baiduEntity)
        return if (result.isSuccess) "游帮帮签到成功"
        else "游帮帮签到失败，${result.message}"
    }



}
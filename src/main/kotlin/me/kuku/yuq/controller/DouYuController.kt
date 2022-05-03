package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.delay
import me.kuku.utils.base64Decode
import me.kuku.yuq.entity.DouYuEntity
import me.kuku.yuq.entity.DouYuService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.DouYuLogic
import org.springframework.stereotype.Component

@Component
@GroupController
@PrivateController
class DouYuController(
    private val douYuLogic: DouYuLogic,
    private val douYuService: DouYuService
) {


    @Action("斗鱼登录")
    suspend fun login(qqEntity: QqEntity, context: BotActionContext, qq: Long) {
        val qrcode = douYuLogic.getQrcode()
        val imageBase = qrcode.qqLoginQrcode.imageBase
        context.source.sendMessage(mif.at(qq).plus(mif.imageByByteArray(imageBase.base64Decode()).plus(mif.text("请使用斗鱼绑定qq，然后使用qq扫码登录"))))
        while (true) {
            delay(3000)
            val result = douYuLogic.checkQrcode(qrcode)
            when (result.code) {
                0 -> continue
                200 -> {
                    val newEntity = result.data()
                    val douYuEntity = douYuService.findByQqEntity(qqEntity) ?: DouYuEntity().also {
                        it.qqEntity = qqEntity
                    }
                    douYuEntity.cookie = newEntity.cookie
                    douYuService.save(douYuEntity)
                    context.source.sendMessage(mif.at(qq).plus("绑定斗鱼成功"))
                    break
                }
                else -> {
                    context.source.sendMessage(mif.at(qq).plus(result.message))
                    break
                }
            }
        }
    }

    @Before(except = ["login"])
    fun before(qqEntity: QqEntity) =
        douYuService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定斗鱼账号，操作失败").toThrowable()


    @Action("斗鱼开播提醒 {status}")
    fun live(douYuEntity: DouYuEntity, status: Boolean): String {
        douYuEntity.config.live = status.toStatus()
        douYuService.save(douYuEntity)
        return "斗鱼开播提醒${if (status) "开启" else "关闭"}成功"
    }


}
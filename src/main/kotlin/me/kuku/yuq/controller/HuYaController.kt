package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.delay
import me.kuku.utils.base64Decode
import me.kuku.yuq.entity.HuYaEntity
import me.kuku.yuq.entity.HuYaService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.HuYaLogic
import org.springframework.stereotype.Component

@Component
@PrivateController
@GroupController
class HuYaController(
    private val huYaLogic: HuYaLogic,
    private val huYaService: HuYaService
) {

    @Action("虎牙登录")
    suspend fun login(qqEntity: QqEntity, context: BotActionContext, qq: Long): String {
        val qrcode = huYaLogic.getQrcode()
        val imageBase = qrcode.qqLoginQrcode.imageBase
        context.source.sendMessage(mif.at(qq).plus(mif.imageByByteArray(imageBase.base64Decode()).plus(mif.text("请使用虎牙绑定qq，然后使用qq扫码登录"))))
        while (true) {
            delay(3000)
            val result = huYaLogic.checkQrcode(qrcode)
            return when (result.code) {
                0 -> continue
                200 -> {
                    val newEntity = result.data()
                    val huYaEntity = huYaService.findByQqEntity(qqEntity) ?: HuYaEntity().also {
                        it.qqEntity = qqEntity
                    }
                    huYaEntity.cookie = newEntity.cookie
                    huYaService.save(huYaEntity)
                    "绑定虎牙成功"
                }
                else -> {
                    result.message
                }
            }
        }
    }

    @Before(except = ["login"])
    fun before(qqEntity: QqEntity) =
        huYaService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定虎牙账号，操作失败").toThrowable()

    @Action("虎牙开播提醒 {status}")
    fun live(huYaEntity: HuYaEntity, status: Boolean): String {
        huYaEntity.config.live = status.toStatus()
        huYaService.save(huYaEntity)
        return "虎牙开播提醒${if (status) "开启" else "关闭"}成功"
    }


}
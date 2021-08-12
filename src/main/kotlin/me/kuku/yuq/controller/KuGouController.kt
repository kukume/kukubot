package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kuku.yuq.entity.KuGouEntity
import me.kuku.yuq.entity.KuGouService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.KuGouLogic
import me.kuku.yuq.logic.ToolLogic
import javax.inject.Inject

@GroupController
class KuGouController @Inject constructor(
    private val kuGouService: KuGouService,
    private val kuGouLogic: KuGouLogic,
    private val toolLogic: ToolLogic
) {
    @Before(except = ["qrcode"])
    fun before(qqEntity: QqEntity, qq: Long) = kuGouService.findByQqEntity(qqEntity)
        ?: throw mif.at(qq).plus("您没有绑定酷狗账号，请发送<酷狗二维码>进行绑定").toThrowable()

    @DelicateCoroutinesApi
    @Action("酷狗二维码")
    @QMsg(at = true)
    fun qrcode(group: Group, qqEntity: QqEntity, qq: Long){
        val qrcode = kuGouLogic.getQrcode()
        group.sendMessage(mif.at(qq).plus(mif.imageByInputStream(toolLogic.creatQr(qrcode.url)).plus("请使用酷狗音乐APP扫码登录")))
        GlobalScope.launch {
            val msg: Message
            while (true){
                delay(3000)
                val result = kuGouLogic.checkQrcode(qrcode)
                if (result.code == 200){
                    val newKuGouEntity = result.data
                    val kuGouEntity = kuGouService.findByQqEntity(qqEntity) ?: KuGouEntity(qqEntity = qqEntity)
                    kuGouEntity.token = newKuGouEntity.token
                    kuGouEntity.userid = newKuGouEntity.userid
                    kuGouEntity.mid = newKuGouEntity.mid
                    kuGouEntity.kuGoo = newKuGouEntity.kuGoo
                    kuGouService.save(kuGouEntity)
                    msg = mif.at(qq).plus("绑定酷狗音乐成功")
                    break
                }else if (result.code == 500) {
                    msg = mif.at(qq).plus(result.message)
                    break
                }
            }
            group.sendMessage(msg)
        }
    }

    @Action("酷狗音乐人签到")
    @QMsg(at = true)
    fun musicianSign(kuGouEntity: KuGouEntity): String {
        val refresh = kuGouLogic.refresh(kuGouEntity)
        if (refresh.isFailure) return refresh.message
        kuGouService.save(refresh.data)
        return kuGouLogic.musicianSign(kuGouEntity).message
    }
}
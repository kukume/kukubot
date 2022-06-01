package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.OppoShopEntity
import me.kuku.yuq.entity.OppoShopService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.OppoShopLogic
import org.springframework.stereotype.Component

@GroupController
@PrivateController
@Component
class OppoShopController(
    private val oppoShopService: OppoShopService
) {

    @Action("oppo商城登陆")
    fun bind(context: BotActionContext, qq: Long, session: ContextSession, qqEntity: QqEntity): String {
        context.source.sendMessage(mif.at(qq).plus("请发送oppo商城的cookie"))
        val ss = session.waitNextMessage().firstString()
        val oppoShopEntity = oppoShopService.findByQqEntity(qqEntity) ?: OppoShopEntity().also {
            it.qqEntity = qqEntity
        }
        oppoShopEntity.cookie = ss
        oppoShopService.save(oppoShopEntity)
        return "绑定oppo商城成功"
    }

    @Before(except = ["bind"])
    fun before(qqEntity: QqEntity) =
        oppoShopService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定oppo商城，操作失败").toThrowable()


    @Action("oppo商城签到")
    suspend fun sign(oppoShopEntity: OppoShopEntity): String {
        val result = OppoShopLogic.sign(oppoShopEntity)
        if (result.failure()) return "签到失败，" + result.message
        OppoShopLogic.shareGoods(oppoShopEntity)
        OppoShopLogic.viewGoods(oppoShopEntity)
        return "oppo商城签到成功"
    }

    @Action("oppo商城自动签到 {status}")
    fun open(status: Boolean, oppoShopEntity: OppoShopEntity): String {
        oppoShopEntity.config.sign = status.toStatus()
        oppoShopService.save(oppoShopEntity)
        return "oppo商城自动签到${if (status) "开启" else "关闭"}成功"
    }

    @Action("oppo商城早睡打卡 {status}")
    fun early(status: Boolean, oppoShopEntity: OppoShopEntity): String {
        oppoShopEntity.config.earlyToBedClock = status.toStatus()
        oppoShopService.save(oppoShopEntity)
        return "oppo商城早睡打卡${if (status) "开启" else "关闭"}成功"
    }


}
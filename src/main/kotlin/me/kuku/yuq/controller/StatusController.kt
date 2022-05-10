package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.entity.Status
import org.springframework.stereotype.Component

@GroupController
@PrivateController
@Component
class StatusController (
    private val qqService: QqService
) {

    @Action("我的状态")
    fun query(qq: Long): String {
        val qqEntity = qqService.findByQqOrderById(qq)!!
        val sb = StringBuilder().appendLine("您的状态：")
        qqEntity.baiduEntity?.config?.let {
            sb.appendLine("百度自动签到：${it.sign.str()}")
        }
        qqEntity.biliBiliEntity?.config?.let {
            sb.appendLine("哔哩哔哩自动签到：${it.sign.str()}")
            sb.appendLine("哔哩哔哩开播提醒：${it.live.str()}")
            sb.appendLine("哔哩哔哩新帖推送：${it.push.str()}")
        }
        qqEntity.hostLocEntity?.config?.let {
            sb.appendLine("HostLoc新帖推送：${it.push.str()}")
            sb.appendLine("HostLoc自动签到：${it.sign.str()}")
        }
        qqEntity.kuGouEntity?.config?.let {
            sb.appendLine("酷狗音乐自动签到：${it.sign.str()}")
        }
        qqEntity.miHoYoEntity?.config?.let {
            sb.appendLine("原神自动签到：${it.sign.str()}")
        }
        qqEntity.oppoShopEntity?.config?.let {
            sb.appendLine("oppo商城自动签到：${it.sign.str()}")
            sb.appendLine("oppo商城晚睡打卡：${it.earlyToBedClock.str()}")
        }
        qqEntity.stepEntity?.config?.let {
            sb.appendLine("自动刷步数：${it.step}")
        }
        qqEntity.weiboEntity?.config?.let {
            sb.appendLine("微博自动签到：${it.sign.str()}")
            sb.appendLine("微博新帖推送：${it.push.str()}")
        }
        qqEntity.netEaseEntity?.config?.let {
            sb.appendLine("网易云音乐自动签到：${it.sign.str()}")
            sb.appendLine("网易云音乐人自动签到：${it.musicianSign.str()}")
        }
        qqEntity.douYuEntity?.config?.let {
            sb.appendLine("斗鱼开播提醒：${it.live.str()}")
        }
        qqEntity.huYaEntity?.config?.let {
            sb.appendLine("虎牙开播提醒：${it.live.str()}")
        }

        return sb.removeSuffix("\n").toString()
    }

    @Action("群状态")
    fun groupStatus(groupEntity: GroupEntity?): String? {
        if (groupEntity == null) return null
        val config = groupEntity.config
        return """
            群状态：
            复读：${config.repeat.str()}
            撤回通知：${config.recallNotify.str()}
            闪照通知：${config.flashImageNotify.str()}
            退群拉黑：${config.leaveToBlack.str()}
            r18：${config.loLiConR18.str()}
            进群验证：${config.entryVerification.str()}
            群管权限：${config.adminCanExecute.str()}
        """.trimIndent()
    }

    private fun Status.str(): String {
        return if (this.name == "ON") "开启"
        else "关闭"
    }

}
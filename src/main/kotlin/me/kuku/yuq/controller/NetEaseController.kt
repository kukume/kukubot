package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import kotlinx.coroutines.delay
import me.kuku.utils.toUrlEncode
import me.kuku.yuq.entity.NetEaseEntity
import me.kuku.yuq.entity.NetEaseService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.NetEaseLogic
import javax.inject.Inject

@GroupController
@PrivateController
class NetEaseController @Inject constructor(
    private val netEaseService: NetEaseService
): QQController() {

    @Action("网易登录")
    suspend fun login(qqEntity: QqEntity, qq: Long, session: ContextSession): String? {
        reply(mif.at(qq).plus("请选择扫码登陆or密码登陆，1为扫码，2为密码").toMessage())
        val ss = session.waitNextMessage().firstString()
        if (ss.toInt() == 1) {
            val key = NetEaseLogic.qrcode()
            val url = "http://music.163.com/login?codekey=$key"
            val newUrl =
                "https://tool.lu/qrcode/basic.html?text=${url.toUrlEncode()}&tolerance=15&size=250&margin=0&front_color=%23000000&background_color=%23ffffff"
            reply(mif.at(qq).plus(mif.imageByUrl(newUrl)).plus("请使用网易云音乐APP扫码登陆").toMessage())
            var scan = true
            while (true) {
                delay(3000)
                val result = NetEaseLogic.checkQrcode(key)
                when (result.code) {
                    200 -> {
                        val netEaseEntity = result.data
                        val newEntity = netEaseService.findByQqEntity(qqEntity) ?: NetEaseEntity().also {
                            it.qqEntity = qqEntity
                        }
                        newEntity.csrf = netEaseEntity.csrf
                        newEntity.musicU = netEaseEntity.musicU
                        netEaseService.save(newEntity)
                        reply(mif.at(qq).plus("绑定网易云音乐成功").toMessage())
                        break
                    }
                    500 -> {
                        reply(mif.at(qq).plus(result.message).toMessage())
                        break
                    }
                    1 -> {
                        if (scan) {
                            reply(mif.at(qq).plus(result.message).toMessage())
                            scan = false
                        }
                    }
                }
            }
            return null
        } else {
            reply(mif.at(qq).plus("请发送手机号").toMessage())
            val phone = session.waitNextMessage().firstString()
            reply(mif.at(qq).plus("请发送密码").toMessage())
            val password = session.waitNextMessage().firstString()
            val result = NetEaseLogic.login(phone, password)
            return if (result.isSuccess) {
                val netEaseEntity = result.data
                val newEntity = netEaseService.findByQqEntity(qqEntity) ?: NetEaseEntity().also {
                    it.qqEntity = qqEntity
                }
                newEntity.csrf = netEaseEntity.csrf
                newEntity.musicU = netEaseEntity.musicU
                netEaseService.save(newEntity)
                "绑定网易云音乐成功"
            } else result.message
        }
    }


    @Before(except = ["login"])
    fun before(qqEntity: QqEntity): NetEaseEntity {
        val netEaseEntity = netEaseService.findByQqEntity(qqEntity)
        return netEaseEntity ?: throw mif.at(qqEntity.qq).plus("您没有绑定网易云音乐账号，操作失败").toThrowable()
    }

    @Action("网易签到")
    suspend fun sign(netEaseEntity: NetEaseEntity): String {
        val result = NetEaseLogic.sign(netEaseEntity)
        return if (result.isSuccess) "网易云音乐签到成功"
        else "网易云音乐签到失败，${result.message}"
    }

    @Action("网易听歌")
    suspend fun listenMusic(netEaseEntity: NetEaseEntity): String {
        val result = NetEaseLogic.listenMusic(netEaseEntity)
        return if (result.isSuccess) "网易云音乐听歌成功"
        else "网易云音乐听歌失败，${result.message}"
    }

    @Action("网易音乐人签到")
    suspend fun musicianSign(netEaseEntity: NetEaseEntity): String {
        val res = NetEaseLogic.musicianSign(netEaseEntity)
        return if (res.isSuccess) "网易云音乐人签到成功"
        else "网易云音乐人签到失败，${res.message}"
    }

    @Action("网易自动签到 {status}")
    fun signOpen(status: Boolean, netEaseEntity: NetEaseEntity): String {
        netEaseEntity.config.sign = status.toStatus()
        netEaseService.save(netEaseEntity)
        return "网易云音乐自动签到${if (status) "开启" else "关闭"}成功"
    }

    @Action("网易发布动态")
    suspend fun dy(netEaseEntity: NetEaseEntity): String {
        val result = NetEaseLogic.publish(netEaseEntity)
        return if (result.isSuccess) "网易云音乐发布动态成功"
        else "网易云音乐发布动态失败，${result.message}"
    }

    @Action("网易发布mlog")
    suspend fun pub(netEaseEntity: NetEaseEntity): String {
        val result = NetEaseLogic.publishMLog(netEaseEntity)
        return if (result.isSuccess) "网易云音乐发布mlog成功"
        else "网易云音乐发布mlog失败，${result.message}"
    }

    @Action("网易主创说")
    suspend fun commentMyMusic(netEaseEntity: NetEaseEntity): String {
        val result = NetEaseLogic.myMusicComment(netEaseEntity)
        return if (result.isSuccess) "网易云音乐发布主创说成功"
        else "网易云音乐发布主创说失败，${result.message}"
    }
}


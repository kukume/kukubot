package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.ContextController
import com.icecreamqaq.yuq.annotation.NextContext
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.MessageFactory
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.dao.SteamDao
import me.kuku.yuq.dao.SuperCuteDao
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.SteamEntity
import me.kuku.yuq.entity.SuperCuteEntity
import me.kuku.yuq.service.impl.SteamServiceImpl
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQQrCodeLoginUtils
import me.kuku.yuq.utils.QQUtils
import me.kuku.yuq.utils.image
import javax.inject.Inject
import kotlin.concurrent.thread

@PrivateController
@ContextController
class BindController {
    @Inject
    private lateinit var qqDao: QQDao
    @Inject
    private lateinit var superCuteDao: SuperCuteDao
    @Inject
    private lateinit var steamDao: SteamDao
    @Inject
    private lateinit var steamService: SteamServiceImpl
    @Inject
    private lateinit var botController: BotController
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var mf: MessageFactory

    @Action("qq")
    fun bindQQ(@PathVar(1) password: String?, qq: Long): Any? {
        return if (password != null){
            val commonResult = QQPasswordLoginUtils.login(qq = qq.toString(), password = password)
            if (commonResult.code == 200){
                val map = commonResult.t
                QQUtils.saveOrUpdate(qqDao, map, qq, password)
                "绑定或者更新成功！"
            }else commonResult.msg
        }else "请输入QQ密码"
    }

    @Action("萌宠")
    @NextContext("nextBindSuperCute")
    fun bindSuperCute() = "请输入需要绑定的超级萌宠的token"

    @Action("nextBindSuperCute")
    fun nextBindSuperCute(qq: Long, message: Message): String{
        val superCuteEntity = superCuteDao.findByQQ(qq) ?: SuperCuteEntity(qq = qq)
        var token = message.body[0].toPath()
        if (token.startsWith("Bearer")) token = token.removePrefix("Bearer ")
        superCuteEntity.token = token
        superCuteDao.singSaveOrUpdate(superCuteEntity)
        return "绑定或者更新超级萌宠token成功"
    }

    @Action("steam")
    @NextContext("nextBindSteam")
    fun bindSteam() = "请输入需要绑定的steam信息"

    @Action("nextBindSteam")
    fun nextBindSteam(@PathVar(0) username: String?, @PathVar(1) password: String?, @PathVar(2) code: String, qq: Long): String{
        return if (username != null && password != null) {
            val commonResult = steamService.login(username, password, code)
            if (commonResult.code == 200){
                val map = commonResult.t
                val steamEntity = steamDao.findByQQ(qq) ?: SteamEntity(null, qq)
                steamEntity.cookie = map.getValue("cookie")
                steamEntity.steamId = map.getValue("steamId")
                steamEntity.username = username
                steamEntity.password = password
                steamDao.singleSaveOrUpdate(steamEntity)
                "绑定或者更新成功"
            } else commonResult.msg
        }else "缺少参数[账号 密码 二次验证码（令牌）]"
    }

    @Action("group")
    fun addGroupCookie(qq: Long): Message{
        val qqEntity = botController.qqEntity
        return if (qqEntity != null) {
            val map = QQQrCodeLoginUtils.getQrCode("715030901", "73")
            val bytes = map.getValue("qrCode") as ByteArray
            thread {
                val commonResult = QQUtils.qrCodeLoginVerify(map.getValue("sig").toString(), "715030901", "73", "https://qun.qq.com")
                val msg = if (commonResult.code == 200) {
                    //登录成功
                    qqEntity.groupPsKey = commonResult.t.getValue("p_skey")
                    "绑定或更新成功！"
                } else {
                    commonResult.msg
                }
                yuq.sendMessage(mf.newPrivate(qq).plus(msg))
            }
            mif.image(bytes).plus("将为机器人更新qun.qq.com的cookie")
        }else mif.text("获取失败").toMessage()
    }

}
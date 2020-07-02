package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.ContextController
import com.icecreamqaq.yuq.annotation.NextContext
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.message.Message
import me.kuku.yuq.entity.SteamEntity
import me.kuku.yuq.entity.SuperCuteEntity
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.SteamService
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import javax.inject.Inject

@PrivateController
@ContextController
class BindController {
    @Inject
    private lateinit var daoService: DaoService
    @Inject
    private lateinit var steamService: SteamService

    @Action("qq")
    fun bindQQ(@PathVar(1) password: String?, qq: Long): Any? {
        val qqEntity = daoService.findQQByQQ(qq)
        val pwd = password ?: qqEntity?.password
        return if (pwd != null){
            val commonResult = QQPasswordLoginUtils.login(qq = qq.toString(), password = pwd)
            if (commonResult.code == 200){
                val map = commonResult.t
                QQUtils.saveOrUpdate(daoService, map, qq, pwd)
                "绑定或者更新成功！"
            }else commonResult.msg
        }else "缺少参数[密码]"
    }

    @Action("萌宠")
    @NextContext("nextBindSuperCute")
    fun bindSuperCute() = "请输入需要绑定的超级萌宠的token"

    @Action("nextBindSuperCute")
    fun nextBindSuperCute(qq: Long, message: Message): String{
        val superCuteEntity = daoService.findSuperCuteByQQ(qq) ?: SuperCuteEntity(qq = qq)
        var token = message.body[0].toPath()
        if (token.startsWith("Bearer")) token = token.removePrefix("Bearer ")
        superCuteEntity.token = token
        daoService.saveOrUpdateSuperCute(superCuteEntity)
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
                val steamEntity = daoService.findSteamByQQ(qq) ?: SteamEntity(null, qq)
                steamEntity.cookie = map.getValue("cookie")
                steamEntity.steamId = map.getValue("steamId")
                steamEntity.username = username
                steamEntity.password = password
                daoService.saveOrUpdateSteam(steamEntity)
                "绑定或者更新成功"
            } else commonResult.msg
        }else "缺少参数[账号 密码 二次验证码（令牌）]"
    }
}
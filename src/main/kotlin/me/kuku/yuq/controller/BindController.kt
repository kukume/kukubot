package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.NextContext
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.firstString
import com.icecreamqaq.yuq.message.Message
import me.kuku.yuq.entity.NeTeaseEntity
import me.kuku.yuq.entity.SteamEntity
import me.kuku.yuq.entity.SuperCuteEntity
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.logic.NeTeaseLogic
import me.kuku.yuq.logic.SteamLogic
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.service.*
import me.kuku.yuq.utils.MD5Utils
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import javax.inject.Inject

@PrivateController
class BindController: QQController() {
    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var superCuteService: SuperCuteService
    @Inject
    private lateinit var steamService: SteamService
    @Inject
    private lateinit var steamLogic: SteamLogic
    @Inject
    private lateinit var neTeaseLogic: NeTeaseLogic
    @Inject
    private lateinit var neTeaseService: NeTeaseService
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var weiboService: WeiboService

    @Action("qq")
    fun bindQQ(@PathVar(1) password: String?, qq: Long, session: ContextSession, message: Message): Any? {
        val qqEntity = qqService.findByQQ(qq)
        val newGroup = message.group ?: 0L
        val pwd = password ?: qqEntity?.password
        return if (pwd != null){
            val commonResult = QQPasswordLoginUtils.login(qq = qq.toString(), password = pwd)
            when (commonResult.code) {
                200 -> {
                    val map = commonResult.t
                    QQUtils.saveOrUpdate(qqService, map, qq, pwd, newGroup)
                    "绑定或者更新成功！"
                }
                10009 -> {
                    reply(commonResult.msg)
                    val map = commonResult.t
                    val codeMessage = session.waitNextMessage(1000 * 60 * 2)
                    val code = codeMessage.firstString()
                    val loginResult = QQPasswordLoginUtils.loginBySms(qq = qq.toString(), password = pwd, randStr = map["randStr"].toString(),
                            ticket = map["ticket"].toString(), cookie = map["cookie"].toString(), smsCode = code)
                    if (loginResult.code != 200) return "验证码输入错误，请重新登录！！"
                    QQUtils.saveOrUpdate(qqService, loginResult.t, qq, pwd, newGroup)
                    "绑定或者更新成功！"
                }
                else -> commonResult.msg
            }
        }else "缺少参数[密码]"
    }

    @Action("萌宠")
    fun bindSuperCute(qq: Long, session: ContextSession): String {
        reply("请输入需要绑定的超级萌宠的token")
        val tokenMessage = session.waitNextMessage(30 * 1000)
        var token = tokenMessage.firstString()
        val superCuteEntity = superCuteService.findByQQ(qq) ?: SuperCuteEntity(qq = qq)
        if (!token.startsWith("Bearer")) return "Token不规范，请重新绑定！！"
        token = token.removePrefix("Bearer ")
        superCuteEntity.token = token
        superCuteService.save(superCuteEntity)
        return "绑定或者更新超级萌宠token成功"
    }

    @Action("steam")
    @NextContext("nextBindSteam")
    fun bindSteam() = "请输入需要绑定的steam信息"

    @Action("nextBindSteam")
    fun nextBindSteam(@PathVar(0) username: String?, @PathVar(1) password: String?, @PathVar(2) code: String, qq: Long): String{
        return if (username != null && password != null) {
            val commonResult = steamLogic.login(username, password, code)
            if (commonResult.code == 200){
                val map = commonResult.t
                val steamEntity = steamService.findByQQ(qq) ?: SteamEntity(null, qq)
                steamEntity.cookie = map.getValue("cookie")
                steamEntity.steamId = map.getValue("steamId")
                steamEntity.username = username
                steamEntity.password = password
                steamService.save(steamEntity)
                "绑定或者更新成功"
            } else commonResult.msg
        }else "缺少参数[账号 密码 二次验证码（令牌）]"
    }

    @Action("md5 {str}")
    fun md5(str: String): String = MD5Utils.toMD5(str)

    @Action("网易")
    fun bindNeTease(qq: Long, session: ContextSession): String{
        reply("请输入网易云音乐账号！！")
        val accountMessage = session.waitNextMessage(30 * 1000)
        val account = accountMessage.firstString()
        reply("请输入网易云音乐密码，密码必须为32位md5，不可以传入明文，如需使用机器人md5摘要，请发送md5 内容")
        reply("md5在线加密网站：https://md5jiami.51240.com/，请使用32位小写！！")
        val pwdMessage = session.waitNextMessage(60 * 1000 * 2)
        val password = pwdMessage.firstString()
        val commonResult = neTeaseLogic.loginByPhone(account, password)
        return if (commonResult.code == 200){
            val neTeaseEntity = neTeaseService.findByQQ(qq) ?: NeTeaseEntity(null, qq)
            val newNeTeaseEntity = commonResult.t
            newNeTeaseEntity.id = neTeaseEntity.id
            newNeTeaseEntity.qq = qq
            neTeaseService.save(newNeTeaseEntity)
            "绑定成功！！"
        }else "绑定失败！！${commonResult.msg}"
    }

    @Action("wb {username} {password}")
    fun bindWb(username: String, password: String, session: ContextSession, qq: Long, message: Message): String{
        val weiboEntity = weiboService.findByQQ(qq) ?: WeiboEntity(null, qq)
        val commonResult = weiboLogic.login(username, password)
        val mutableMap = commonResult.t ?: return commonResult.msg
        reply("请输入短信验证码！！！")
        loop@ do {
            val codeMessage = session.waitNextMessage(60 * 1000 * 2)
            val code = codeMessage.firstString()
            val loginCommonResult = weiboLogic.loginBySms(mutableMap.getValue("token"), mutableMap.getValue("phone"), code)
            when (loginCommonResult.code){
                200 -> {
                    val newWeiboEntity = loginCommonResult.t
                    weiboEntity.pcCookie = newWeiboEntity.pcCookie
                    weiboEntity.mobileCookie = newWeiboEntity.mobileCookie
                    weiboEntity.username = username
                    weiboEntity.password = password
                    weiboEntity.group_ = message.group ?: 0L
                    break@loop
                }
                500 -> {
                    return loginCommonResult.msg
                }
                402 -> reply("验证码输入错误，请重新输入！！！")
            }
        }while (loginCommonResult.code == 402)
        weiboService.save(weiboEntity)
        return "绑定或更新成功！！！"
    }
}
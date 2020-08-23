package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.firstString
import com.icecreamqaq.yuq.message.*
import com.icecreamqaq.yuq.toMessage
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.yuq.entity.NeTeaseEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.logic.*
import me.kuku.yuq.service.*
import me.kuku.yuq.utils.*
import javax.inject.Inject
import kotlin.concurrent.thread

@GroupController
class QQController: QQController() {
    @Inject
    private lateinit var leXinMotionLogic: LeXinMotionLogic
    @Inject
    private lateinit var motionService: MotionService
    @Inject
    private lateinit var qqMailLogic: QQMailLogic
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var qqZoneLogic: QQZoneLogic
    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var neTeaseLogic: NeTeaseLogic
    @Inject
    private lateinit var neTeaseService: NeTeaseService
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var weiboService: WeiboService
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic
    @Inject
    private lateinit var biliBiliService: BiliBiliService

    @Before
    fun checkBind(@PathVar(0) str: String, qq: Long, actionContext: BotActionContext, group: Long){
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.qqStatus == true) {
            val qqEntity = qqService.findByQQ(qq)
            when {
                str.toLowerCase() == "qq" -> return
                qqEntity?.status == false -> throw "您的QQ已失效，请更新QQ！！".toMessage()
                qqEntity != null -> actionContext.session["qqEntity"] = qqEntity
                else -> throw mif.at(qq).plus("没有绑定QQ！，请先发送qq进行扫码登录绑定，如需密码登录绑定请私聊机器人发送qq")
            }
        }else throw "QQ功能已关闭！！请联系管理员开启！！".toMessage()
    }

    @Action("qq")
    @Synonym(["QQ", "qQ", "Qq"])
    @QMsg(at = true)
    fun bindQQ(group: Group, qq: Long): Message{
        val map = QQQrCodeLoginUtils.getQrCode()
        val bytes = map.getValue("qrCode") as ByteArray
        thread {
            val commonResult = QQUtils.qrCodeLoginVerify(map.getValue("sig").toString())
            val msg = if (commonResult.code == 200){
                //登录成功
                QQUtils.saveOrUpdate(qqService, commonResult.t!!, qq, group = group.id)
                "绑定或更新成功！"
            }else{
                commonResult.msg
            }
            group.sendMessage(mif.at(qq).plus(msg))
        }
        return mif.image(bytes).plus("qzone.qq.com的扫码登录")
    }

    @Action("气泡")
    @QMsg(at = true)
    fun bubble(@PathVar(1) text: String?, @PathVar(2) name: String?, qqEntity: QQEntity): String{
        return if (text != null){
            qqLogic.diyBubble(qqEntity, text, name)
        }else "缺少参数：diy气泡文本内容！"
    }

    @Action("业务")
    @QMsg(at = true)
    fun queryVip(qqEntity: QQEntity) = qqLogic.queryVip(qqEntity)

    @Action("昵称")
    @QMsg(at = true)
    fun modifyNickname(@PathVar(1) str: String?, qqEntity: QQEntity): String{
        return if (str != null){
            qqLogic.modifyNickname(qqEntity, str)
        }else qqLogic.modifyNickname(qqEntity, " ")
    }

    @Action("头像")
    @QMsg(at = true)
    fun modifyAvatar(qqEntity: QQEntity, message: Message): String{
        val singleBody = message.body.getOrNull(1)
        val url = if (singleBody != null) {
            if (singleBody is Image){
                singleBody.url
            }else "请携带一张头像"
        }else "http://qqpublic.qpic.cn/qq_public/0/0-3083588061-157B50D7A4036953784514241D7DDC19/0"
        return qqLogic.modifyAvatar(qqEntity, url)
    }

    @Action("送花")
    @QMsg(at = true)
    fun sendFlower(qqEntity: QQEntity, message: Message, group: Long): String{
        val singleBody = message.body.getOrNull(1)
        val qq: String =  if (singleBody != null){
            if (singleBody is At){
                singleBody.user.toString()
            }else singleBody.toPath()
        }else return "缺少参数，送花的对象！"
        return qqLogic.sendFlower(qqEntity, qq.toLong(), group)
    }

    @Action("群礼物")
    @QMsg(at = true)
    fun lottery(qqEntity: QQEntity): String{
        val commonResult = qqZoneLogic.queryGroup(qqEntity)
        return if (commonResult.code == 200){
            val list = commonResult.t
            val sb = StringBuilder()
            list?.forEach {
                val group = it.getValue("group").toLong()
                val result = qqLogic.groupLottery(qqEntity, group)
                if (result.contains("成功")) sb.appendln(result)
            }
            val str = sb.removeSuffixLine().toString()
            if (str == "") "啥都没抽到"
            else str
        }else commonResult.msg
    }

    @Action("拒绝添加")
    @QMsg(at = true)
    fun refuseAdd(qqEntity: QQEntity) = qqLogic.refuseAdd(qqEntity)

    @Action("超级签到")
    @QMsg(at = true)
    @Synchronized fun allSign(qqEntity: QQEntity, group: Long, qq: Long): String{
        reply(mif.at(qq).plus("请稍后！！！正在为您签到中~~~"))
        val str1 = qqLogic.qqSign(qqEntity)
        return if (!str1.contains("更新QQ")){
            val sb = StringBuilder()
            qqLogic.anotherSign(qqEntity)
            val str2 = qqLogic.groupLottery(qqEntity, group)
            val str3 = if ("失败" in qqLogic.vipSign(qqEntity)) "签到失败" else "签到成功"
            val str4 = qqLogic.phoneGameSign(qqEntity)
            val str5 = qqLogic.yellowSign(qqEntity)
            val str6 = qqLogic.qqVideoSign1(qqEntity)
            val str7 = qqLogic.qqVideoSign2(qqEntity)
            val str8 = qqLogic.bigVipSign(qqEntity)
            val str9 = if ("失败" in qqLogic.qqMusicSign(qqEntity)) "签到失败" else "签到成功"
            val str10 = if ("成功" in qqLogic.gameSign(qqEntity)) "签到成功" else "签到失败"
            val str11 = if ("失败" in qqLogic.qPetSign(qqEntity)) "领取失败" else "领取成功"
            val str12 = if ("成功" in qqLogic.tribeSign(qqEntity)) "领取成功" else "领取失败"
            val str13 = qqLogic.motionSign(qqEntity)
            val str14 = if ("成功" in qqLogic.blueSign(qqEntity)) "签到成功" else "签到失败"
            val str15 = qqLogic.sVipMornSign(qqEntity)
            val str16 = qqLogic.weiYunSign(qqEntity)
            val str17 = qqLogic.weiShiSign(qqEntity)
            val str18 = qqLogic.growthLike(qqEntity)
            sb.appendln("手机打卡：$str1")
                    .appendln("群等级抽奖：$str2")
                    .appendln("会员签到：$str3")
                    .appendln("手游加速：$str4")
                    .appendln("黄钻签到：$str5")
                    .appendln("腾讯视频签到1：$str6")
                    .appendln("腾讯视频签到2：$str7")
                    .appendln("大会员签到；$str8")
                    .appendln("音乐签到：$str9")
                    .appendln("游戏签到：$str10")
                    .appendln("大乐斗签到：$str11")
                    .appendln("兴趣部落：$str12")
                    .appendln("运动签到：$str13")
                    .appendln("蓝钻签到：$str14")
                    .appendln("svip打卡报名：$str15")
                    .appendln("微云签到：$str16")
                    .appendln("微视签到：$str17")
                    .append("排行榜点赞：$str18")
            sb.toString()
        }else "超级签到失败，请更新QQ！"
    }

    @Action("赞说说")
    @QMsg(at = true)
    fun likeTalk(qqEntity: QQEntity): String{
        val friendTalk = qqZoneLogic.friendTalk(qqEntity)
        return if (friendTalk != null) {
            friendTalk.forEach {
                if (it["like"] == null || it["like"] != "1") {
                    qqZoneLogic.likeTalk(qqEntity, it)
                }
            }
            "赞说说成功！！！"
        }else "赞说说失败，请更新QQ！"
    }

    @Action("成长")
    @QMsg(at = true)
    fun growth(qqEntity: QQEntity): String = qqLogic.vipGrowthAdd(qqEntity)

    @Action("中转站")
    @QMsg(at = true)
    fun mailFile(qqEntity: QQEntity, qq: Long): String {
        if (qqEntity.password == "") return "获取QQ邮箱文件中转站分享链接，需要使用密码登录QQ！"
        reply(mif.at(qq).plus("正在获取中，请稍后~~~~~"))
        val commonResult = qqMailLogic.getFile(qqEntity)
        return if (commonResult.code == 200){
            val list = commonResult.t!!
            val sb = StringBuilder().appendln("QQ邮箱文件中转站文件如下：")
            for (i in list.indices){
                val map = list[i]
                val url = "http://mail.qq.com/cgi-bin/ftnExs_download?k=${map.getValue("sKey")}&t=exs_ftn_download&code=${map.getValue("sCode")}"
                sb.appendln("文件名：${map.getValue("sName")}")
                sb.appendln("链接：${BotUtils.shortUrl(url)}")
            }
            sb.removeSuffix("\r\n").toString()
        }else commonResult.msg
    }

    @Action("续期")
    @QMsg(at = true)
    fun renew(qqEntity: QQEntity, qq: Long): String{
        if (qqEntity.password == "") return "续期QQ邮箱中转站文件失败！！，需要使用密码登录QQ！"
        reply(mif.at(qq).plus("正在续期中，请稍后~~~~~"))
        return qqMailLogic.fileRenew(qqEntity)
    }

    @Action("好友 {qqNo}")
    @QMsg(at = true)
    fun addFriend(qqEntity: QQEntity, qqNo: Long, @PathVar(2) msg: String?, @PathVar(3) realName: String?, @PathVar(4) groupName: String?): String{
        return qqZoneLogic.addFriend(qqEntity, qqNo, msg ?: "加个好友呗！！", realName, groupName)
    }

    @Action("复制 {qqNo}")
    @QMsg(at = true)
    fun copyAvatar(qqNo: Long, qqEntity: QQEntity): String{
        val url = "https://q.qlogo.cn/g?b=qq&nk=${qqNo}&s=640"
        return qqLogic.modifyAvatar(qqEntity, url)
    }

    @Action("删除qq")
    @Synonym(["删除QQ"])
    @QMsg(at = true)
    fun delQQ(qqEntity: QQEntity): String{
        qqService.delByQQ(qqEntity.qq)
        return "删除QQ成功！！！"
    }

    @Action("群列表")
    @QMsg(at = true)
    fun groupList(qqEntity: QQEntity): String{
        val commonResult = qqZoneLogic.queryGroup(qqEntity)
        return if (commonResult.code == 200){
            val list = commonResult.t!!
            val sb = StringBuilder("群号        群名\n")
            list.forEach {
                sb.appendln("${it.getValue("groupName")}\t\t${it.getValue("group")}")
            }
            sb.removeSuffix("\r\n").toString()
        }else "获取群列表失败，请更新QQ！！"
    }

    @Action("#加管 {qqNo}")
    @QMsg(at = true)
    fun addAdmin(qqNo: Long, qqEntity: QQEntity, group: Long) =
            qqLogic.setGroupAdmin(qqEntity, qqNo, group, true)

    @Action("#去管 {qqNo}")
    @QMsg(at = true)
    fun cancelAdmin(qqNo: Long, qqEntity: QQEntity, group: Long) =
            qqLogic.setGroupAdmin(qqEntity, qqNo, group, false)

    @Action("#步数/{step}")
    @QMsg(at = true)
    fun step(qqEntity: QQEntity, qq: Long, step: Int): String{
        val motionEntity = motionService.findByQQ(qq)
        if (motionEntity != null){
            val msg = leXinMotionLogic.modifyStepCount(step, motionEntity)
            if ("成功" in msg) return msg
            reply(mif.at(qq).plus("lexin运动的cookie已过期，正在为您重新登录并执行！！"))
        }
        val commonResult = leXinMotionLogic.loginByQQ(qqEntity)
        return if (commonResult.code == 200){
            val newMotionEntity = commonResult.t!!
            newMotionEntity.id = motionEntity?.id
            newMotionEntity.qq = qq
            motionService.save(newMotionEntity)
            leXinMotionLogic.modifyStepCount(step, newMotionEntity)
        }else commonResult.msg
    }

    @Action("网易")
    @QMsg(at = true)
    fun neTeaseLogin(qqEntity: QQEntity, qq: Long): String{
        val commonResult = neTeaseLogic.loginByQQ(qqEntity)
        return if (commonResult.code == 200){
            val neTeaseEntity = neTeaseService.findByQQ(qq) ?: NeTeaseEntity(null)
            val newNeTeaseEntity = commonResult.t!!
            newNeTeaseEntity.id = neTeaseEntity.id
            newNeTeaseEntity.qq = qq
            neTeaseService.save(newNeTeaseEntity)
            "绑定成功"
        }else "绑定失败！！${commonResult.msg}"
    }

    @Action("自定义机型 {iMei}")
    @QMsg(at = true)
    fun changePhoneOnline(qqEntity: QQEntity, iMei: String, qq: Long, session: ContextSession): String{
        reply(mif.at(qq).plus("请输入您需要自定义的机型！！"))
        val nextMessage = session.waitNextMessage(30 * 1000)
        val phone = nextMessage.firstString()
        return qqLogic.changePhoneOnline(qqEntity, iMei, phone)
    }

    @Action("访问空间 {qqNo}")
    @QMsg(at = true)
    fun visit(qqNo: Long, qqEntity: QQEntity) = qqZoneLogic.visitQZoneMobile(qqEntity, qqNo)

    @Action("互访")
    @QMsg(at = true)
    fun visitAll(qq: Long): String{
        val list = qqService.findByActivity()
        list.forEach { qqZoneLogic.visitQZone(it, qq) }
        return "互访成功！！"
    }

    @Action("wblogin")
    @QMsg(at = true)
    fun weiboLogin(qqEntity: QQEntity, session: ContextSession, qq: Long, group: Long): String{
        reply(mif.at(qq).plus("确保你的微博账号已绑定QQ！！如果是请发送1"))
        val nextMessage = session.waitNextMessage()
        return if (nextMessage.firstString() == "1"){
            val commonResult = weiboLogic.loginByQQ(qqEntity)
            val weiboEntity = commonResult.t ?: return commonResult.msg
            val newWeiboEntity = weiboService.findByQQ(qq) ?: WeiboEntity(null, qq, group)
            newWeiboEntity.mobileCookie = weiboEntity.mobileCookie
            newWeiboEntity.pcCookie = weiboEntity.pcCookie
            weiboService.save(newWeiboEntity)
            "绑定或者更新成功！！"
        }else "已退出上下文！！"
    }

    @Action("bilibililogin")
    @QMsg(at = true)
    fun biliBiliLogin(qqEntity: QQEntity, qq: Long, group: Long): String{
        val commonResult = biliBiliLogic.loginByQQ(qqEntity)
        val cookie = commonResult.t ?: return commonResult.msg
        val biliBiliEntity = biliBiliService.findByQQ(qq) ?: BiliBiliEntity(null, qq)
        biliBiliEntity.cookie = cookie
        biliBiliEntity.group_ = group
        biliBiliService.save(biliBiliEntity)
        return "绑定或者更新成功！！"
    }
}


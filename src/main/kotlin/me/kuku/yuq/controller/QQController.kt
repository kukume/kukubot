package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.message.*
import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.service.impl.QQMailServiceImpl
import me.kuku.yuq.service.impl.QQServiceImpl
import me.kuku.yuq.service.impl.QQZoneServiceImpl
import me.kuku.yuq.service.impl.ToolServiceImpl
import me.kuku.yuq.utils.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

@GroupController
class QQController {
    @Inject
    private lateinit var toolService: ToolServiceImpl
    @Inject
    private lateinit var qqMailService: QQMailServiceImpl
    @Inject
    private lateinit var qqService: QQServiceImpl
    @Inject
    private lateinit var qqZoneService: QQZoneServiceImpl
    @Inject
    private lateinit var qqDao: QQDao
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var mf: MessageFactory

    @Before
    fun checkBind(@PathVar(0) str: String, qq: Long, actionContext: BotActionContext){
        val qqEntity = qqDao.findByQQ(qq)
        when {
            qqEntity != null -> actionContext.session["qqEntity"] = qqEntity
            str == "qq" -> return
            else -> throw mif.text("没有绑定QQ！").toMessage()
        }
    }

    @Action("qq")
    fun bindQQ(group: Long, qq: Long): Message{
        val map = QQQrCodeLoginUtils.getQrCode()
        val bytes = map.getValue("qrCode") as ByteArray
        thread {
            val msg: String
            val sig = map.getValue("sig").toString()
            var commonResult: CommonResult<Map<String, String>>
            do {
                TimeUnit.SECONDS.sleep(3)
                commonResult = QQQrCodeLoginUtils.checkQrCode(sig = sig)
            }while (commonResult.code == 0)
            msg = if (commonResult.code == 200){
                //登录成功
                QQUtils.saveOrUpdate(qqDao, commonResult.t, qq, group = group)
                "绑定或更新成功！"
            }else{
                commonResult.msg
            }
            yuq.sendMessage(mf.newGroup(group).plus(msg))
        }
        return mif.image(bytes).plus("扫码登录它来了！！！")
    }

    @Action("签到")
    @QMsg(at = true)
    fun groupSign(qqEntity: QQEntity, group: Long): String{
        val arr = arrayOf(178, 124, 120, 180, 181, 127, 125, 126)
        val id = arr.random()
        val map = toolService.hiToKoTo()
        return qqService.groupSign(qqEntity, group, map["from"] ?: "你猜", map.getValue("text"), "{\"category_id\":9,\"page\":0,\"pic_id\":$id}")
    }

    @Action("气泡")
    fun bubble(@PathVar(1) text: String?, @PathVar(2) name: String?, qqEntity: QQEntity): String{
        return if (text != null){
            qqService.diyBubble(qqEntity, text, name)
        }else "缺少参数：diy气泡文本内容！"
    }

    @Action("业务")
    fun queryVip(qqEntity: QQEntity) = qqService.queryVip(qqEntity)

    @Action("昵称")
    fun modifyNickname(@PathVar(1) str: String?, qqEntity: QQEntity): String{
        return if (str != null){
            qqService.modifyNickname(qqEntity, str)
        }else qqService.modifyNickname(qqEntity, " ")
    }

    @Action("头像")
    fun modifyAvatar(qqEntity: QQEntity, message: Message): String{
        val singleBody = message.body.getOrNull(1)
        val url = if (singleBody != null) {
            if (singleBody is Image){
                singleBody.url
            }else "请携带一张头像"
        }else "http://qqpublic.qpic.cn/qq_public/0/0-3083588061-157B50D7A4036953784514241D7DDC19/0"
        return qqService.modifyAvatar(qqEntity, url)
    }

    @Action("送花")
    fun sendFlower(qqEntity: QQEntity, message: Message, group: Long): String{
        val singleBody = message.body.getOrNull(1)
        val qq: String =  if (singleBody != null){
            if (singleBody is At){
                singleBody.user.toString()
            }else singleBody.toPath()
        }else return "缺少参数，送花的对象！"
        return qqService.sendFlower(qqEntity, qq.toLong(), group)
    }

    @Action("拒绝添加")
    fun refuseAdd(qqEntity: QQEntity) = qqService.refuseAdd(qqEntity)

    @Action("超级签到")
    fun allSign(qqEntity: QQEntity, group: Long, qq: Long): String{
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请稍后！！！正在为您签到中~~~"))
        val str1 = qqService.qqSign(qqEntity)
        return if ("成功" in str1){
            val sb = StringBuilder()
            qqService.anotherSign(qqEntity)
            val str2 = qqService.groupLottery(qqEntity, group)
            val str3 = if ("失败" in qqService.vipSign(qqEntity)) "签到失败" else "签到成功"
            val str4 = qqService.phoneGameSign(qqEntity)
            val str5 = qqService.yellowSign(qqEntity)
            val str6 = qqService.qqVideoSign1(qqEntity)
            val str7 = qqService.qqVideoSign2(qqEntity)
            val str8 = qqService.bigVipSign(qqEntity)
            val str9 = qqService.qqMusicSign(qqEntity)
            val str10 = if ("成功" in qqService.gameSign(qqEntity)) "签到成功" else "签到失败"
            val str11 = qqService.qPetSign(qqEntity)
            val str12 = qqService.tribeSign(qqEntity)
            val str13 = qqService.motionSign(qqEntity)
            val str14 = if ("成功" in qqService.blueSign(qqEntity)) "签到成功" else "签到失败"
            val str15 = qqService.sVipMornSign(qqEntity)
            val str16 = qqService.weiYunSign(qqEntity)
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
                    .append("微云签到：$str16")
            sb.toString()
        }else "超级签到失败，请更新QQ！"
    }

    @Action("赞说说")
    fun likeTalk(qqEntity: QQEntity): String{
        val friendTalk = qqZoneService.friendTalk(qqEntity)
        return if (friendTalk != null) {
            val sb = StringBuilder()
            friendTalk.forEach {
                if (it["like"] == null || it["like"] != "1") {
                    val str = qqZoneService.likeTalk(qqEntity, it)
                    sb.appendln(str)
                }
            }
            if (sb.toString() == "") "没有要赞的说说"
            else sb.toString()
        }else "赞说说失败，请更新QQ！"
    }

    @Action("成长")
    fun growth(qqEntity: QQEntity): String = qqService.vipGrowthAdd(qqEntity)

    @Action("中转站")
    fun mailFile(qqEntity: QQEntity, group: Long): String {
        yuq.sendMessage(mf.newGroup(group).plus("正在获取中，请稍后~~~~~"))
        val commonResult = qqMailService.getFile(qqEntity)
        return if (commonResult.code == 200){
            val list = commonResult.t
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
    fun renew(qqEntity: QQEntity, group: Long): String{
        yuq.sendMessage(mf.newGroup(group).plus("正在获取中，请稍后~~~~~"))
        return qqMailService.fileRenew(qqEntity)
    }

}


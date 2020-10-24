@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.At
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.QQLoginEntity
import me.kuku.yuq.entity.QQJobEntity
import me.kuku.yuq.logic.*
import me.kuku.yuq.service.*
import me.kuku.yuq.utils.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

@GroupController
class QQController: QQController() {
    @Inject
    private lateinit var qqMailLogic: QQMailLogic
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var qqZoneLogic: QQZoneLogic
    @Inject
    private lateinit var qqLoginService: QQLoginService
    @Inject
    private lateinit var groupService: GroupService

    @Before
    fun checkBind(qq: Long, actionContext: BotActionContext){
        val qqLoginEntity = qqLoginService.findByQQ(qq) ?:
            throw mif.at(qq).plus("没有绑定QQ！，请先发送qq进行扫码登录绑定，如需密码登录绑定请私聊机器人发送qq").toThrowable()
        if (qqLoginEntity.status) actionContext.session["qqLoginEntity"] = qqLoginEntity
        else throw mif.at(qq).plus("您的QQ已失效，请更新QQ！！").toThrowable()
    }

    @Action("气泡")
    @QMsg(at = true)
    fun bubble(@PathVar(1) text: String?, @PathVar(2) name: String?, qqLoginEntity: QQLoginEntity): String{
        return if (text != null){
            qqLogic.diyBubble(qqLoginEntity, text, name)
        }else "缺少参数：diy气泡文本内容！"
    }

    @Action("业务")
    @QMsg(at = true, atNewLine = true)
    fun queryVip(qqLoginEntity: QQLoginEntity) = qqLogic.queryVip(qqLoginEntity)

    @Action("昵称")
    @QMsg(at = true)
    fun modifyNickname(@PathVar(1) str: String?, qqLoginEntity: QQLoginEntity): String{
        return if (str != null){
            qqLogic.modifyNickname(qqLoginEntity, str)
        }else qqLogic.modifyNickname(qqLoginEntity, " ")
    }

    @Action("头像")
    @QMsg(at = true)
    fun modifyAvatar(qqLoginEntity: QQLoginEntity, message: Message): String{
        val singleBody = message.body.getOrNull(1)
        val url = if (singleBody != null) {
            if (singleBody is Image){
                singleBody.url
            }else "请携带一张头像"
        }else "http://qqpublic.qpic.cn/qq_public/0/0-3083588061-157B50D7A4036953784514241D7DDC19/0"
        return qqLogic.modifyAvatar(qqLoginEntity, url)
    }

    @Action("送花")
    @QMsg(at = true)
    fun sendFlower(qqLoginEntity: QQLoginEntity, message: Message, group: Long): String{
        val singleBody = message.body.getOrNull(1)
        val qq: String =  if (singleBody != null){
            if (singleBody is At){
                singleBody.user.toString()
            }else singleBody.toPath()
        }else return "缺少参数，送花的对象！"
        return qqLogic.sendFlower(qqLoginEntity, qq.toLong(), group)
    }

    @Action("群礼物")
    @QMsg(at = true)
    fun lottery(qqLoginEntity: QQLoginEntity, group: Group, qq: Long): String{
        val commonResult = qqZoneLogic.queryGroup(qqLoginEntity)
        return if (commonResult.code == 200){
            thread {
                val list = commonResult.t
                val sb = StringBuilder()
                list?.forEach {
                    TimeUnit.SECONDS.sleep(3)
                    val itGroup = it.getValue("group").toLong()
                    val result = qqLogic.groupLottery(qqLoginEntity, itGroup)
                    if (result.contains("成功")) sb.appendLine(result)
                }
                var str = sb.removeSuffixLine().toString()
                str = if (str == "") "啥都没抽到"
                else str
                group.sendMessage(mif.at(qq).plus(str))
            }
            return "抽取群礼物已在后台运行中！！"
        }else commonResult.msg
    }

    @Action("超级签到")
    @QMsg(at = true, atNewLine = true)
    fun allSign(qqLoginEntity: QQLoginEntity, group: Long, qq: Long): String{
        reply(mif.at(qq).plus("请稍后！！！正在为您签到中~~~"))
        val str1 = qqLogic.qqSign(qqLoginEntity)
        return if (!str1.contains("更新QQ")){
            try {
                val sb = StringBuilder()
                qqLogic.anotherSign(qqLoginEntity)
                val str2 = qqLogic.groupLottery(qqLoginEntity, group)
                val str3 = if ("失败" in qqLogic.vipSign(qqLoginEntity)) "签到失败" else "签到成功"
                val str4 = qqLogic.phoneGameSign(qqLoginEntity)
                val str5 = qqLogic.yellowSign(qqLoginEntity)
                val str6 = qqLogic.qqVideoSign1(qqLoginEntity)
                val str7 = qqLogic.qqVideoSign2(qqLoginEntity)
                val str8 = qqLogic.bigVipSign(qqLoginEntity)
                val str9 = if ("失败" in qqLogic.qqMusicSign(qqLoginEntity)) "签到失败" else "签到成功"
                val str10 = if ("失败" in qqLogic.qPetSign(qqLoginEntity)) "领取失败" else "领取成功"
                val str11 = if ("成功" in qqLogic.tribeSign(qqLoginEntity)) "领取成功" else "领取失败"
                val str12 = qqLogic.motionSign(qqLoginEntity)
                val str13 = if ("成功" in qqLogic.blueSign(qqLoginEntity)) "签到成功" else "签到失败"
                val str14 = qqLogic.sVipMornSign(qqLoginEntity)
                val str15 = qqLogic.weiYunSign(qqLoginEntity)
                val str16 = qqLogic.weiShiSign(qqLoginEntity)
                val str17 = qqLogic.growthLike(qqLoginEntity)
                sb.appendLine("手机打卡：$str1")
                        .appendLine("群等级抽奖：$str2")
                        .appendLine("会员签到：$str3")
                        .appendLine("手游加速：$str4")
                        .appendLine("黄钻签到：$str5")
                        .appendLine("腾讯视频签到1：$str6")
                        .appendLine("腾讯视频签到2：$str7")
                        .appendLine("大会员签到；$str8")
                        .appendLine("音乐签到：$str9")
                        .appendLine("大乐斗签到：$str10")
                        .appendLine("兴趣部落：$str11")
                        .appendLine("运动签到：$str12")
                        .appendLine("蓝钻签到：$str13")
                        .appendLine("svip打卡报名：$str14")
                        .appendLine("微云签到：$str15")
                        .appendLine("微视签到：$str16")
                        .append("排行榜点赞：$str17")
                sb.toString()
                "超级签到成功！！"
            }catch (e: IOException){
                "超级签到失败！！请重试！！"
            }
        }else "超级签到失败，请更新QQ！"
    }

    @Action("赞说说")
    @QMsg(at = true)
    fun likeTalk(qqLoginEntity: QQLoginEntity): String{
        val friendTalk = qqZoneLogic.friendTalk(qqLoginEntity)
        return if (friendTalk != null) {
            friendTalk.forEach {
                if (it["like"] == null || it["like"] != "1") {
                    qqZoneLogic.likeTalk(qqLoginEntity, it)
                }
            }
            "赞说说成功！！！"
        }else "赞说说失败，请更新QQ！"
    }

    @Action("成长")
    @QMsg(at = true, atNewLine = true)
    fun growth(qqLoginEntity: QQLoginEntity): String = qqLogic.vipGrowthAdd(qqLoginEntity)

    @Action("中转站")
    @QMsg(at = true, atNewLine = true)
    fun mailFile(qqLoginEntity: QQLoginEntity, qq: Long): String {
        if (qqLoginEntity.password == "") return "获取QQ邮箱文件中转站分享链接，需要使用密码登录QQ！"
        reply(mif.at(qq).plus("正在获取中，请稍后~~~~~"))
        val commonResult = qqMailLogic.getFile(qqLoginEntity)
        return if (commonResult.code == 200){
            val list = commonResult.t!!
            val sb = StringBuilder().appendLine("QQ邮箱文件中转站文件如下：")
            for (i in list.indices){
                val map = list[i]
                val url = "http://mail.qq.com/cgi-bin/ftnExs_download?k=${map.getValue("sKey")}&t=exs_ftn_download&code=${map.getValue("sCode")}"
                sb.appendLine("文件名：${map.getValue("sName")}")
                sb.appendLine("链接：${BotUtils.shortUrl(url)}")
            }
            sb.removeSuffix("\r\n").toString()
        }else commonResult.msg
    }

    @Action("续期")
    @QMsg(at = true)
    fun renew(qqLoginEntity: QQLoginEntity, qq: Long): String{
        if (qqLoginEntity.password == "") return "续期QQ邮箱中转站文件失败！！，需要使用密码登录QQ！"
        reply(mif.at(qq).plus("正在续期中，请稍后~~~~~"))
        return qqMailLogic.fileRenew(qqLoginEntity)
    }

    @Action("复制 {qqNo}")
    @QMsg(at = true)
    fun copyAvatar(qqNo: Long, qqLoginEntity: QQLoginEntity): String{
        val url = "https://q.qlogo.cn/g?b=qq&nk=${qqNo}&s=640"
        return qqLogic.modifyAvatar(qqLoginEntity, url)
    }

    @Action("删除qq")
    @Synonym(["删除QQ"])
    @QMsg(at = true)
    fun delQQ(qqLoginEntity: QQLoginEntity): String{
        qqLoginService.delByQQ(qqLoginEntity.qq)
        return "删除QQ成功！！！"
    }

    @Action("自定义机型 {iMei}")
    @QMsg(at = true)
    fun changePhoneOnline(qqLoginEntity: QQLoginEntity, iMei: String, qq: Long, session: ContextSession): String{
        reply(mif.at(qq).plus("请输入您需要自定义的机型！！"))
        val nextMessage = session.waitNextMessage(30 * 1000)
        val phone = nextMessage.firstString()
        return qqLogic.changePhoneOnline(qqLoginEntity, iMei, phone)
    }

    @Action("访问空间 {qqNo}")
    @QMsg(at = true)
    fun visit(qqNo: Long, qqLoginEntity: QQLoginEntity) = qqZoneLogic.visitQZone(qqLoginEntity, qqNo)

    @Action("互访")
    @QMsg(at = true)
    fun visitAll(qq: Long): String{
        val list = qqLoginService.findByActivity()
        list.forEach { qqZoneLogic.visitQZone(it, qq) }
        return "互访成功！！"
    }
}

@GroupController
class BindQQController: QQController(){
    @Inject
    private lateinit var qqLoginService: QQLoginService

    @Action("qqlogin qr")
    @QMsg(at = true)
    fun bindQQ(group: Group, qq: Long): Message{
        val map = QQQrCodeLoginUtils.getQrCode()
        val bytes = map.getValue("qrCode") as ByteArray
        thread {
            val commonResult = QQUtils.qrCodeLoginVerify(map.getValue("sig").toString())
            val msg = if (commonResult.code == 200){
                //登录成功
                QQUtils.saveOrUpdate(qqLoginService, commonResult.t!!, qq, group = group.id)
                "绑定或更新成功！"
            }else{
                commonResult.msg
            }
            group.sendMessage(mif.at(qq).plus(msg))
        }
        return mif.imageByInputStream(bytes.inputStream()).plus("qzone.qq.com的扫码登录")
    }
}

@GroupController
class QQJobController {
    @Inject
    private lateinit var qqJobService: QQJobService
    @Inject
    private lateinit var qqLoginService: QQLoginService

    @Before
    fun check(qq: Long){
        qqLoginService.findByQQ(qq) ?: throw mif.at(qq).plus("没有绑定QQ！！").toThrowable()
    }

    @QMsg(at = true)
    @Action("秒赞 {status}")
    fun mzOpen(qq: Long, status: Boolean): String{
        var qqJobEntity = qqJobService.findByQQAndType(qq, "mz")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            jsonObject["status"] = false
            qqJobEntity = QQJobEntity(null, qq, "mz", jsonObject.toString())
        }
        val jsonObject = qqJobEntity.dataJsonObject
        jsonObject["status"] = status
        qqJobEntity.data = jsonObject.toString()
        qqJobService.save(qqJobEntity)
        return "秒赞已${if (status) "开启" else "关闭"}"
    }

    @QMsg(at = true)
    @Action("百变气泡/{text}")
    fun varietyBubble(qq: Long, text: String): String{
        var qqJobEntity = qqJobService.findByQQAndType(qq, "bubble")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            jsonObject["status"] = false
            jsonObject["text"] = ""
            qqJobEntity = QQJobEntity(null, qq, "bubble", jsonObject.toString())
        }
        val jsonObject = qqJobEntity.dataJsonObject
        val msg = if (text == "关") {
            jsonObject["status"] = false
            "百变气泡已关闭！！"
        }else {
            jsonObject["status"] = true
            jsonObject["text"] = text
            "百变气泡已开启！！气泡diy文字为：$text"
        }
        qqJobEntity.data = jsonObject.toString()
        qqJobService.save(qqJobEntity)
        return msg
    }

    @QMsg(at = true)
    @Action("自动签到 {status}")
    fun autoSign(qq: Long, status: Boolean): String{
        var qqJobEntity = qqJobService.findByQQAndType(qq, "autoSign")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            jsonObject["status"] = false
            qqJobEntity = QQJobEntity(null, qq, "autoSign", jsonObject.toString())
        }
        val jsonObject = qqJobEntity.dataJsonObject
        jsonObject["status"] = status
        qqJobEntity.data = jsonObject.toString()
        qqJobService.save(qqJobEntity)
        return "qq自动签到${if (status) "开启" else "关闭"}成功"
    }
}
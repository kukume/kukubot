@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.alibaba.fastjson.JSONArray
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.message.MessageItem
import com.icecreamqaq.yuq.message.Text
import com.icecreamqaq.yuq.mirai.MiraiBot
import me.kuku.yuq.entity.QQLoginEntity
import me.kuku.yuq.logic.QQGroupLogic
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.GroupService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.removeSuffixLine
import net.mamoe.mirai.contact.PermissionDeniedException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@GroupController
class BotController: QQController() {
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var miraiBot: MiraiBot
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var web: OkHttpWebImpl
    @Inject
    private lateinit var qqGroupLogic: QQGroupLogic
    @Inject
    private lateinit var groupService: GroupService

    @Before
    fun before(actionContext: BotActionContext) {
        val qqLoginEntity = BotUtils.toQQEntity(web, miraiBot)
        actionContext["qqLoginEntity"] = qqLoginEntity
    }

    @Action("签个到 {param}")
    fun groupSign(qqLoginEntity: QQLoginEntity, param: MessageItem, group: Long): Any?{
        val place = "你猜"
        val text = "yuq签到！！"
        val str = when (param) {
            is Text -> {
                val commonResult = qqGroupLogic.groupSign(group, place, text, param.text, null, null)
                commonResult.t ?: return commonResult.msg
            }
            is Image -> {
                val commonResult = qqLogic.groupUploadImage(qqLoginEntity, param.url)
                val map = commonResult.t ?: return commonResult.msg
                val signCommonResult = qqGroupLogic.groupSign(group, place, text, "自定义", map["picId"], map["picUrl"])
                signCommonResult.t ?: return commonResult.msg
            }
            else -> return null
        }
        return mif.jsonEx(str)
    }

    @Action("qq上传")
    fun groupUpload(qqLoginEntity: QQLoginEntity, qq: Long, session: ContextSession): String{
        reply(mif.at(qq).plus("请发送您需要上传的图片！！"))
        val nextMessage = session.waitNextMessage()
        val body = nextMessage.body
        val sb = StringBuilder().appendLine("您上传的图片链接如下：")
        val i = 1
        for (item in body){
            if (item is Image){
                val commonResult = qqLogic.groupUploadImage(qqLoginEntity, item.url)
                val url = commonResult.t?.getValue("picUrl") ?: commonResult.msg
                sb.appendLine("$i、$url")
            }
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("查业务 {qqNo}")
    @QMsg(at = true, atNewLine = true)
    fun queryVip(qqNo: Long, qqLoginEntity: QQLoginEntity) = qqLogic.queryFriendVip(qqLoginEntity, qqNo, null)

    @Action("列出{day}天未发言")
    @QMsg(at = true, atNewLine = true)
    fun notSpeak(group: Long, day: String, qqLoginEntity: QQLoginEntity): String? {
        val commonResult = qqLogic.groupMemberInfo(qqLoginEntity, group)
        return if (commonResult.code == 200) {
            val list = commonResult.t!!
            val qqList = mutableListOf<Long>()
            val sb = StringBuilder().appendLine("本群${day}天未发言的成员如下：")
            list.forEach {
                if ((Date().time - it.lastTime) / (1000 * 60 * 60 * 24) > day.toInt()) {
                    sb.appendLine(it.qq)
                    qqList.add(it.qq)
                }
            }
            sb.removeSuffixLine().toString()
        } else return commonResult.msg
    }

    @Action("列出从未发言")
    @QMsg(at = true, atNewLine = true)
    fun neverSpeak(group: Long, qqLoginEntity: QQLoginEntity): String?{
        val commonResult = qqLogic.groupMemberInfo(qqLoginEntity, group)
        val list = commonResult.t ?: return commonResult.msg
        val qqList = mutableListOf<Long>()
        val sb = StringBuilder("本群从未发言的成员如下：\n")
        list.forEach {
            if ((it.lastTime == it.joinTime || it.integral <= 1) && (Date().time - it.joinTime > 1000 * 60 * 60 * 24)) {
                sb.appendLine(it.qq)
                qqList.add(it.qq)
            }
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("查询 {qqNo}")
    @QMsg(at = true, atNewLine = true)
    fun query(group: Long, qqNo: Long): String{
        val commonResult = qqGroupLogic.queryMemberInfo(group, qqNo)
        val groupMember = commonResult.t ?: return commonResult.msg
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val sb = StringBuilder()
        sb.appendLine()
        sb.appendLine("群名片：${groupMember.groupCard}")
        sb.appendLine("Q龄：${groupMember.age}")
        sb.appendLine("入群时间：${sdf.format(Date(groupMember.joinTime))}")
        sb.append("最后发言时间：${sdf.format(Date(groupMember.lastTime))}")
        return sb.toString()
    }

    @QMsg(at = true)
    @Action("群链接")
    fun groupLink(group: Long, qqLoginEntity: QQLoginEntity) = qqLogic.getGroupLink(qqLoginEntity, group)

    @Action("天气 {local}")
    fun weather(local: String, qqLoginEntity: QQLoginEntity, qq: Long): Message {
        val commonResult = toolLogic.weather(local, qqLoginEntity.getCookie())
        return mif.xmlEx(146, commonResult.t ?: return mif.at(qq).plus(commonResult.msg)).toMessage()
    }

    @Action("龙王")
    fun dragonKing(group: Long, qq: Member, @PathVar(value = 1, type = PathVar.Type.Integer) num: Int?): Message{
        val qqGroupEntity = groupService.findByGroup(group)
        val list = qqGroupLogic.groupHonor(group, "talkAtIve")
        if (list.isEmpty()) return mif.at(qq.id).plus("昨天没有龙王！！")
        if (num ?: 1 > list.size){
            return mif.at(qq.id).plus("历史龙王只有${list.size}位哦，超过范围了！！")
        }
        val map = list[(num ?: 1) - 1]
        val resultQQ = map.getValue("qq").toLong()
        if (resultQQ == this.qq.toLong()) return listOf("呼风唤雨", "84消毒", "巨星排面").random().toMessage()
        val jsonArray = qqGroupEntity?.whiteJsonArray ?: JSONArray()
        if (jsonArray.contains(resultQQ.toString())){
            return try {
                qq.ban(60 * 5)
                mif.at(qq.id).plus("迫害白名单用户，您已被禁言！！")
            }catch (e: PermissionDeniedException){
                mif.at(qq.id).plus("禁止迫害白名单用户，禁言迫害者失败，权限不足！！")
            }
        }
        val urlArr = arrayOf(
                "https://u.iheit.com/kuku/61f600415023300.jpg",
                "https://u.iheit.com/kuku/449ab0415103619.jpg",
                "https://u.iheit.com/kuku/51fe90415023311.jpg",
                "https://u.iheit.com/kuku/1d12a0415023726.jpg",
                "https://u.iheit.com/kuku/b04b30415023728.jpg",
                "https://u.iheit.com/kuku/d21200415023730.jpg",
                "https://u.iheit.com/kuku/55f0e0415023731.jpg",
                "https://u.iheit.com/kuku/634cc0415023733.jpg",
                "https://u.iheit.com/kuku/c044b0415023734.jpg",
                "https://u.iheit.com/kuku/ce2270415023735.jpg",
                "https://u.iheit.com/kuku/6e4b20415023737.jpg",
                "https://u.iheit.com/kuku/5f7d70415023738.jpg",
                "https://u.iheit.com/kuku/98d640415023739.jpg",
                "https://u.iheit.com/kuku/26a1a0415023741.jpg",
                "https://u.iheit.com/kuku/ddc810415023745.jpg",
                "https://u.iheit.com/kuku/23ee20415023747.jpg",
                "https://u.iheit.com/kuku/8c4a80415023748.jpg",
                "https://u.iheit.com/kuku/bdb970415023750.jpg",
                "https://u.iheit.com/images/2020/07/23/33609b326b5b30b0.jpg",
                "https://u.iheit.com/images/2020/07/23/3e53644bd75c68e6.jpg",
                "https://u.iheit.com/images/2020/08/05/image.png",
                "https://u.iheit.com/images/2020/08/05/image4046ccd0c6179229.png"
        )
        val url = urlArr[Random.nextInt(urlArr.size)]
        return mif.at(resultQQ).plus(mif.imageByUrl(url)).plus("龙王，已蝉联${map.getValue("desc")}，快喷水！！")
    }

    @Action("群聊炽焰")
    @Synonym(["群聊之火", "冒尖小春笋", "快乐源泉"])
    fun legend(group: Long, qq: Long, @PathVar(0) str: String, @PathVar(value = 1, type = PathVar.Type.Integer) num: Int?): Message{
        val msg: String
        val list: List<Map<String, String>>
        when (str) {
            "群聊炽焰" -> {
                list = qqGroupLogic.groupHonor(group, "legend")
                msg = "快续火！！"
            }
            "群聊之火" -> {
                list = qqGroupLogic.groupHonor(group, "actor")
                msg = "快续火！！"
            }
            "冒尖小春笋" -> {
                list = qqGroupLogic.groupHonor(group, "strongNewBie")
                msg = "快......我也不知道快啥了！！"
            }
            "快乐源泉" -> {
                list = qqGroupLogic.groupHonor(group, "emotion")
                msg = "快发表情包！！"
            }
            else -> return mif.at(qq).plus("类型不匹配，查询失败！！")
        }
        if (list.isEmpty()) return mif.at(qq).plus("该群还没有${str}用户！！")
        if (num ?: 1 > list.size){
            return mif.at(qq).plus("${str}只有${list.size}位哦，超过范围了！！")
        }
        val map = list[(num ?: 1) - 1]
        return mif.at(map.getValue("qq").toLong()).plus(mif.imageByUrl(map.getValue("image"))).plus("$str，${map.getValue("desc")}，$msg")
    }

    @Action("群精华")
    fun essenceMessage(group: Long): String{
        val commonResult = qqGroupLogic.essenceMessage(group)
        val list = commonResult.t ?: return commonResult.msg
        return list[Random.nextInt(list.size)]
    }
}
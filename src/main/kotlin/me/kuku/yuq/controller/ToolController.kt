package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.mif
import me.kuku.pojo.QqLoginPojo
import me.kuku.utils.*
import me.kuku.yuq.entity.*
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.logic.YgoLogic
import me.kuku.yuq.utils.YuqUtils
import okhttp3.MultipartBody
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate

@GroupController
@Component
class ToolController (
    private val messageService: MessageService,
    private val recallService: RecallService
) {

    @Action(value = "\\.*读消息$\\")
    fun readMessage(message: Message, group: Long): String? {
        val messageSource = message.reply ?: return null
        val id = messageSource.id
        val messageEntity = messageService.findByMessageIdAndGroup(id, group) ?: return "没有找到该消息"
        return messageEntity.content
    }

    @Action("龙王")
    suspend fun dragonKing(group: Long, qq: Long, @PathVar(value = 1, type = PathVar.Type.Integer) num: Int?): Any {
        val groupQqLoginPojo = YuqUtils.groupQqLoginPojo()
        val list = QqGroupLogic.groupHonor(groupQqLoginPojo, group, GroupHonorType.TALK_AT_IVE)
        if (list.isEmpty()) return mif.at(qq).plus("没有发现龙王")
        val newNum = num ?: 1
        if (newNum > list.size) return mif.at(qq).plus("历史龙王只有${list.size}位哦，超过范围了")
        val groupHonor = list[newNum - 1]
        if (groupQqLoginPojo.qq == groupHonor.qq) return listOf("呼风唤雨", "84消毒", "巨星排面").random().toMessage()
        val urlList = listOf(
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/0bd0d4c6-0ebb-4811-ba06-a0d65c3a8ed3.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/32c1791e-0cb5-4888-a99f-dd8bdd654423.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/493dfe13-bebb-4cd7-8d77-d0bde395db68.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c4cfb6b0-1e67-4f23-9d6e-80a03fb5f91f.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/568877f3-f62b-4cc1-97ee-0d48da8dfb59.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/f39ebff2-03c0-4cee-8967-206562cc055e.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8bbf31d5-878b-4d42-9aa0-a41fd8e13ea6.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c3aa3d94-5cf7-47e1-ba56-db9116b1bcae.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/d13d84e5-e7fa-4d1b-ae6c-1413ffc78769.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/b465c944-8373-4d8c-beda-56eb7c24fa0b.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/b049eb61-dca3-4541-b3dd-c220ccd94595.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/682c4454-fc52-41c3-9c44-890aaa08c03d.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/00d716cf-f691-42ea-aa71-e28f18a3b4b3.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8635cd24-5d87-4fc8-b429-425e02b22849.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/7309fe37-7e34-4b7e-9304-5a1a854d251c.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c631afd3-9614-403c-a5a1-18413bbe3374.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/cfa9129d-e99d-491b-932d-e353ce7ca2d8.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/40960b38-781d-43b0-863b-8962a5342020.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c3e83c57-242a-4843-af51-85a84f7badaf.gif",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8e4d291b-e6ba-48d9-b8f9-3adc56291c27.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8bcad94b-aff5-4e81-af89-8a1007eda4ae.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/dc8403a0-caec-40e0-98a8-93abdb263712.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/1468ee00-a106-42c7-9ce3-0ced6b2ddc3e.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/959cd1ef-8731-4379-b1ad-0d3bf66e38c0.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/79c484e0-695c-49e9-9514-bcbe294ca7c6.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/f9b48126-fb7e-4482-b5ce-140294f57066.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/732a6387-2595-4c56-80f8-c52fce6214bb.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/bb768136-d96d-451d-891a-5f409f7fbff1.jpg"
        )
        return mif.at(groupHonor.qq).plus(mif.imageByUrl(urlList.random())).plus("龙王，已上位${groupHonor.desc}，快喷水")
    }

    @Action("查撤回 {qqNo}")
    fun queryRecall(qqNo: Long, group: Group, qq: Long, session: ContextSession): Any {
        val list = recallService.findByGroupAndQq(group.id, qqNo)
        if (list.isEmpty()) return mif.at(qq).plus("该qq没有撤回消息哦")
        group.sendMessage(mif.at(qq).plus("该qq有${list.size}条撤回消息，您需要查询第几条"))
        var num = session.waitNextMessage().firstString().toIntOrNull() ?: return mif.at(qq).plus("您输入的不为数字，上下文结束")
        if (num > list.size) return mif.at(qq).plus("您输入数字的已超出撤回消息数，上下文结束")
        num -= 1
        val recallEntity = list[num]
        return recallEntity.messageEntity.content.toMessageByRainCode()
    }

    @Action("查消息 {qqNo}")
    fun queryMessage(qqNo: Long, group: Group, session: ContextSession, qq: Long): Any {
        val list = messageService.findByGroupAndQqOrderByIdDesc(group.id, qqNo)
        if (list.isEmpty()) return mif.at(qq).plus("该qq没有消息记录哦")
        group.sendMessage(mif.at(qq).plus("该qq有${list.size}条消息，您需要查询第几条"))
        var num = session.waitNextMessage().firstString().toIntOrNull() ?: return mif.at(qq).plus("您输入的不为数字，上下文结束")
        if (num > list.size) return mif.at(qq).plus("您输入数字的已超出消息数，上下文结束")
        num -= 1
        val messageEntity = list[num]
        return messageEntity.content.toMessageByRainCode()
    }

    @Action("查发言数")
    @Transactional
    suspend fun queryMessage(group: Group): String {
        val list = messageService.findByGroupAndLocalDateTimeAfter(group.id, LocalDate.now().atStartOfDay())
        val map = mutableMapOf<Long, Int>()
        for (messageEntity in list) {
            val qq = messageEntity.qqEntity?.qq ?: continue
            val i = map[qq] ?: 0
            map[qq] = i + 1
        }
        val ss = map.toSortedMap { o1, o2 -> map[o2]!!.compareTo(map[o1]!!) }
        val sb = StringBuilder()
        var i = 0
        for ((k, v) in ss) {
            if (++i == 5) break
            sb.append(group[k].nameCardOrName()).append("（").append(k).append("）").append(" - ").append(v).appendLine("条")
        }
        return sb.removeSuffix("\n").toString()
    }

}

@GroupController
@PrivateController
@Component
class ToolAllController(
    private val toolLogic: ToolLogic,
    private val ygoLogic: YgoLogic
) {

    @Action("菜单")
    @Synonym(["帮助", "功能"])
    @Transactional
    suspend fun menu(qqEntity: QqEntity): Any {
        val str = """
            机器人帮助（命令）如下：
            http://kkb.im
        """.trimIndent()
        return mif.at(qqEntity.qq).plus(str)
    }

    @Action("百科 {text}")
    suspend fun baiKe(text: String) = toolLogic.baiKe(text)

    @Action("dcloud上传")
    suspend fun dCloudUpload(session: ContextSession, context: BotActionContext, qq: Long): Message {
        context.source.sendMessage(mif.at(qq).plus("请发送需要上传的图片"))
        val message = session.waitNextMessage()
        var url: String? = null
        for (messageItem in message.body) {
            if (messageItem is Image) {
                val tempUrl = messageItem.url
                val jsonObject = OkHttpKtUtils.postJson("https://api.kukuqaq.com/upload", MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("type", "3")
                    .addFormDataPart("file", messageItem.id, OkUtils.streamBody(OkHttpKtUtils.getBytes(tempUrl))).build())
                url = if (jsonObject.getInteger("code") == 200) jsonObject["data"]["url"].asText()
                else jsonObject.getString("message")
                break
            }
        }
        val send = url?.toMessage() ?: "没有发现图片".toMessage()
        send.reply = message.source
        return send
    }

    @Action("舔狗日记")
    suspend fun dog() = OkHttpKtUtils.getStr("https://api.oick.cn/dog/api.php").replace("\"", "")

    @Action("读懂世界")
    suspend fun readWorld(): String =
        OkHttpKtUtils.getJson("https://api.kukuqaq.com/readWorld").getString("data")

    @Action("icp {domain}")
    suspend fun icp(domain: String): String {
        val str = OkHttpKtUtils.getStr("https://api.kukuqaq.com/icp?keyword=${domain.toUrlEncode()}")
        val any = JSON.parse(str)
        return if (any is JSONArray) {
            if (any.isEmpty()) "该域名未查到备案信息"
            else {
                val singleJsonObject = any.getJSONObject(0)
                """
                    网站名称：${singleJsonObject.getString("name")}
                    主办单位名称：${singleJsonObject.getString("unitName")}
                    域名：${singleJsonObject.getString("domain")}
                    主页：${singleJsonObject.getString("homeUrl")}
                    备案号：${singleJsonObject.getString("licence")}
                    更新时间：${singleJsonObject.getString("updateTime")}
                """.trimIndent()
            }
        } else (any as JSONObject).getString("message")
    }


    @Action("摸鱼日历")
    suspend fun fishermanCalendar(): Any {
        if (LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY) return "周日不摸鱼"
        val bytes = OkHttpKtUtils.getBytes("https://api.kukuqaq.com/fishermanCalendar?preview=1")
        return mif.imageByByteArray(bytes)
    }

    @Action("摸鱼日历搜狗")
    suspend fun fishermanCalendarSoGou(): Any {
        if (LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY) return "周日不摸鱼"
        val bytes = OkHttpKtUtils.getBytes("https://api.kukuqaq.com/fishermanCalendar/sogou?preview=1")
        return mif.imageByByteArray(bytes)
    }

    @Action("色图")
    suspend fun color(groupEntity: GroupEntity?) =
        if (groupEntity?.config?.loLiConR18 == Status.ON)
            mif.imageByUrl(OkHttpKtUtils.getJson("https://api.lolicon.app/setu/v2?r18=1")["data"][0]["urls"]["original"].asText().replace("i.pixiv.cat", "i.pixiv.re"))
        else mif.imageByUrl(OkHttpKtUtils.get("https://api.kukuqaq.com/lolicon/random?preview=1").also { it.close() }.header("location")!!)

    @Action("测吉凶")
    fun qqGodLock(qq: Long): String {
        val str = OkHttpUtils.postStr("https://www.buyiju.com/cqq/", mapOf("sjhao" to qq.toString()))
        val element = Jsoup.parse(str).getElementsByClass("content").firstOrNull() ?: return "查询失败"
        val sb = StringBuilder()
        for (ele in element.getElementsByTag("p")) {
            if (ele.getElementsByTag("a").isEmpty())
                sb.appendLine(ele.text())
        }
        return sb.removeSuffix("\n").toString()
    }

    @Action("搜图 {img}")
    suspend fun ss(img: Image): Message {
        val list = toolLogic.saucenao(img.url)
        val message = Message()
        for ((i, result) in list.withIndex()) {
            val ss = mif.text(
                """
                相似度：${result.similarity}
                名字：${result.indexName}
                标题：${result.title}
                预览链接：${YuqUtils.shortUrl(result.thumbnail)}
                源链接：${result.extUrls}
                作者：${result.author} 
                作者主页：${result.authUrl}
                ==============================
            """.trimIndent()
            ).plus("\n")
            message.plus(ss)
            if (i + 1 > 1) break
        }
        return message
    }

    @Action("jetbrains激活服务器")
    suspend fun activeServer(context: BotActionContext, qq: Long): String {
        val str = OkHttpKtUtils.getStr("https://api.kukuqaq.com/jetbrains/server")
        val jsonObject = JSON.parseArray(str).map { it as JSONObject }.filter { it.getInteger("port") != 443 }.random()
        context.source.sendMessage(jsonObject.getString("ipAndPortUrl"))
        return "在JetBrains IDE中激活方式选择激活服务器，填入上述地址即可（如为破解版可能会激活失败，卸载重装即可）"
    }

    @Action("几点了")
    suspend fun nowTime(): Any {
        val response = OkHttpKtUtils.get("https://api.kukuqaq.com/time").also { it.close() }
        val url = response.header("location") ?: return "获取图片失败"
        return mif.imageByUrl("https://api.kukuqaq.com$url")
    }

    @Action("游戏王 {text}")
    suspend fun ygo(text: String, context: BotActionContext, qq: Long, session: ContextSession): Any? {
        val list = ygoLogic.search(text)
        val sb = StringBuilder()
        var i = 1
        for (card in list) {
            sb.appendLine("${i++}、${card.chineseName}")
        }
        val send = "查询到以下卡片，请发送卡片序号\n${sb.removeSuffix("\n")}"
        kotlin.runCatching {
            context.source.sendMessage(mif.at(qq).plus(send))
        }.onFailure {
            context.source.sendMessage(mif.at(qq).plus("查询到以下卡片，请发送卡片序号\n").plus(UbuntuPasteUtils.url(send)))
        }
        val ii = session.waitNextMessage().firstString().toIntOrNull() ?: return "您发送的不为数字"
        if (ii > list.size) return "您发送的数字不符合规范"
        val card = list[ii - 1]
        val res = StringBuilder().append("中文名：").appendLine(card.chineseName)
            .append("英文名：").appendLine(card.englishName)
            .append("日文名：").appendLine(card.japaneseName)
            .appendLine("效果：").append(card.effect)
        context.source.sendMessage(mif.imageByUrl(card.imageUrl))
        kotlin.runCatching {
            context.source.sendMessage(mif.at(qq).plus(res.toString()))
        }.onFailure {
            context.source.sendMessage(mif.at(qq).plus(UbuntuPasteUtils.url("详情：$res")))
        }
        return null
    }
}

object QqGroupLogic {

    suspend fun groupHonor(qqLoginPojo: QqLoginPojo, group: Long, type: GroupHonorType): List<GroupHonor> {
        val typeNum: Int
        val wwv: Int
        val param: String
        val image: String
        val list = mutableListOf<GroupHonor>()
        when (type) {
            GroupHonorType.TALK_AT_IVE -> {
                typeNum = 1
                wwv = 129
                param = "talkativeList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-drgon.png"
            }
            GroupHonorType.LEGEND -> {
                typeNum = 3
                wwv = 128
                param = "legendList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-fire-big.png"
            }
            GroupHonorType.ACTOR -> {
                typeNum = 2
                wwv = 128
                param = "actorList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-fire-small.png"
            }
            GroupHonorType.STRONG_NEW_BIE -> {
                typeNum = 5
                wwv = 128
                param = "strongnewbieList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-shoots-small.png"
            }
            GroupHonorType.EMOTION -> {
                typeNum = 6
                wwv = 128
                param = "emotionList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-happy-stream.png"
            }
        }
        val html = OkHttpKtUtils.getStr("https://qun.qq.com/interactive/honorlist?gc=$group&type=$typeNum&_wv=3&_wwv=$wwv",
            OkUtils.cookie(qqLoginPojo.cookieWithPs))
        val jsonStr = MyUtils.regex("window.__INITIAL_STATE__=", "</script", html)
        val jsonObject = JSON.parseObject(jsonStr)
        val jsonArray = jsonObject.getJSONArray(param)
        jsonArray.forEach {
            val singleJsonObject = it as JSONObject
            list.add(GroupHonor(singleJsonObject.getLong("uin"),
                singleJsonObject.getString("name"), singleJsonObject.getString("desc"),
                image))
        }
        return list
    }
}

enum class GroupHonorType {
    TALK_AT_IVE,
    LEGEND,
    ACTOR,
    STRONG_NEW_BIE,
    EMOTION
}

data class GroupHonor(
    var qq: Long,
    var name: String,
    var desc: String,
    var image: String
)
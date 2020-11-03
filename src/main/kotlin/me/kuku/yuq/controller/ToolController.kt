@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.kuku.yuq.logic.QQAILogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.ConfigService
import me.kuku.yuq.service.MessageService
import me.kuku.yuq.service.GroupService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.removeSuffixLine
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge
import java.net.URLEncoder
import javax.inject.Inject
import kotlin.random.Random

@GroupController
class ToolController: QQController() {
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var qqAiLogic: QQAILogic
    @Inject
    private lateinit var configService: ConfigService
    @Inject
    private lateinit var messageService: MessageService
    @Config("YuQ.Mirai.protocol")
    private lateinit var protocol: String

    @QMsg(at = true)
    @Action("百度 {content}")
    fun teachYouBaidu(content: String) = toolLogic.teachYou(content, "baidu")

    @QMsg(at = true)
    @Action("谷歌 {content}")
    fun teachYouGoogle(content: String) = toolLogic.teachYou(content, "google")

    @QMsg(at = true)
    @Action("bing {content}")
    fun teachYouBing(content: String) = toolLogic.teachYou(content, "bing")

    @QMsg(at = true)
    @Action("搜狗 {content}")
    fun teachYouSouGou(content: String) = toolLogic.teachYou(content, "sougou")

    @QMsg(at = true, atNewLine = true)
    @Action("舔狗日记")
    fun dogLicking() = toolLogic.dogLicking()

    @QMsg(at = true, atNewLine = true)
    @Action("百科 {params}")
    fun baiKe(params: String) = toolLogic.baiKe(params)

    @QMsg(at = true, atNewLine = true)
    @Action("嘴臭")
    @Synonym(["祖安语录"])
    fun mouthOdor() = toolLogic.mouthOdor()

    @QMsg(at = true)
    @Action("毒鸡汤")
    fun poisonousChickenSoup() = toolLogic.poisonousChickenSoup()

    @QMsg(at = true)
    @Action("名言")
    fun saying() = toolLogic.saying()

    @QMsg(at = true)
    @Action("一言")
    fun hiToKoTo() = toolLogic.hiToKoTo().getValue("text")

    @Action("缩短/{params}")
    @QMsg(at = true)
    fun shortUrl(params: String) = BotUtils.shortUrl(params)

    @Action("ip/{params}")
    @QMsg(at = true)
    fun queryIp(params: String) = toolLogic.queryIp(params)

    @Action("whois/{params}")
    @QMsg(at = true, atNewLine = true)
    fun queryWhois(params: String) = toolLogic.queryWhois(params)

    @Action("icp/{params}")
    @QMsg(at = true, atNewLine = true)
    fun queryIcp(params: String) = toolLogic.queryIcp(params)

    @Action("知乎日报")
    @QMsg(at = true, atNewLine = true)
    fun zhiHuDaily() = toolLogic.zhiHuDaily()

    @QMsg(at = true, atNewLine = true)
    @Action("测吉凶")
    fun qqGodLock(qq: Long) = toolLogic.qqGodLock(qq)

    @QMsg(at = true, atNewLine = true)
    @Action("拼音/{params}")
    fun convertPinYin(params: String) = toolLogic.convertPinYin(params)

    @QMsg(at = true, atNewLine = true)
    @Action("笑话")
    fun jokes() = toolLogic.jokes()

    @QMsg(at = true, atNewLine = true)
    @Action("垃圾/{params}")
    fun rubbish(params: String) = toolLogic.rubbish(params)

    @QMsg(at = true)
    @Action("转{str}")
    fun translate(str: String, session: ContextSession, qq: Long): String?{
        val map = mapOf("英" to "en", "中" to "zh", "日" to "jp", "韩" to "kor",
                "粤" to "yue", "法" to "gra", "俄" to "ru", "德" to "de", "文" to "wyw", "简" to "2", "繁" to "1")
        return if (map.containsKey(str)){
            reply(mif.at(qq).plus("请输入需要翻译的内容！！"))
            val nextMessage = session.waitNextMessage(30 * 1000)
            val content = nextMessage.firstString()
            if (str == "简" || str == "繁"){
                toolLogic.convertZh(content, map.getValue(str).toInt())
            }else toolLogic.convertTranslate(content, "auto", map.getValue(str))
        }else null
    }

    @Action("解析/{url}")
    @QMsg(at = true, atNewLine = true)
    fun parseVideo(url: String) = toolLogic.parseVideo(url)

    @Action("还原/{url}")
    @QMsg(at = true)
    fun restoreShortUrl(url: String) = toolLogic.restoreShortUrl(url)

    @Action("ping/{domain}")
    @QMsg(at = true, atNewLine = true)
    fun ping(domain: String) = toolLogic.ping(domain)

    @Action("搜 {question}")
    @QMsg(at = true)
    fun search(question: String) = toolLogic.searchQuestion(question)

    @Action("涩图")
    @Synonym(["色图", "色图来"])
    @Synchronized
    fun colorPic(group: Long, qq: Long): Message {
        val qqGroupEntity = groupService.findByGroup(group)
        if (qqGroupEntity?.colorPic != true) throw mif.at(qq).plus("该功能已关闭").toThrowable()
        val line = System.getProperty("line.separator")
        return when (val type = qqGroupEntity.colorPicType){
            "danbooru" -> {
                val map = toolLogic.danBooRuPic()
                mif.at(qq).plus(line)
                        .plus(mif.imageByUrl(map.getValue("picUrl")))
                        .plus("详情：${map["detailedUrl"]}$line")
                        .plus("链接：${map["url"]}")
            }
            "lolicon", "loliconR18" -> {
                val configEntity = configService.findByType("loLiCon") ?:
                        return mif.at(qq).plus("您还没有配置lolicon的apiKey，无法获取色图！！")
                val apiKey = configEntity.content
                val commonResult = toolLogic.colorPicByLoLiCon(apiKey, type == "loliconR18")
                val map = commonResult.t ?: return mif.at(qq).plus(commonResult.msg)
                val bytes = toolLogic.piXivPicProxy(map.getValue("url"))
                mif.at(qq).plus(line)
                        .plus(mif.imageByInputStream(bytes.inputStream()))
                        .plus("标题：${map["title"]}$line")
                        .plus("当日剩余额度：${map["count"]}$line")
                        .plus("恢复额度时间：${map["time"]}秒")
            }
            else -> "涩图类型不匹配！！".toMessage()
        }
    }

    @Action("qr/{content}")
    @QMsg(at = true, atNewLine = true)
    fun creatQrCode(content: String): Message{
        val url = toolLogic.creatQr(content)
        return mif.imageByInputStream(url.inputStream()).toMessage()
    }

    @Action("看美女")
    fun girl() = mif.imageByUrl(toolLogic.girlImage())

    @QMsg(at = true)
    @Action("蓝奏 {url}")
    fun lanZou(url: String, @PathVar(2) pwd: String?): String{
        val resultUrl =  if (pwd == null){
            "https://v1.alapi.cn/api/lanzou?url=${URLEncoder.encode(url, "utf-8")}"
        }else "https://v1.alapi.cn/api/lanzou?url=${URLEncoder.encode(url, "utf-8")}&pwd=$pwd"
        return BotUtils.shortUrl(resultUrl)
    }

    @Action("lol周免")
    @QMsg(at = true, atNewLine = true)
    fun lolFree() = toolLogic.lolFree()

    @Action("缩写/{content}")
    @QMsg(at = true, atNewLine = true)
    fun abbreviation(content: String) = toolLogic.abbreviation(content)

    @Action("几点了")
    @Synonym(["多久了", "时间"])
    fun time() = mif.imageByInputStream(toolLogic.queryTime().inputStream())

    @Action("网抑")
    fun wy() = mif.xmlEx(1, "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"1\" templateID=\"-1\" action=\"app\" actionData=\"com.netease.cloudmusic\" brief=\"点击启动网抑\" sourceMsgId=\"0\" url=\"https://www.kuku.me/archives/6/\" flag=\"2\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"12\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://imgurl.cloudimg.cc/2020/07/26/2a7410726090854.jpg\" w=\"0\" h=\"0\" /><title>启动网抑音乐</title></item><source name=\"今天你网抑了吗\" icon=\"\" action=\"\" appid=\"0\" /></msg>")

    @QMsg(at = true)
    @Action("网抑云")
    fun wyy() = toolLogic.music163cloud()

    @Action("\\^BV.*\\")
    @Synonym(["\\^bv.*\\"])
    @QMsg(at = true)
    fun bvToAv(message: Message): Message{
        val bv = message.body[0].toPath()
        val commonResult = toolLogic.bvToAv(bv)
        return if (commonResult.code == 200){
            val map = commonResult.t!!
            mif.imageByUrl(map.getValue("pic")).plus(
                    StringBuilder().appendLine("标题：${map["title"]}")
                            .appendLine("描述：${map["desc"]}")
                            .append("链接：${map["url"]}").toString()
            )
        }else commonResult.msg.toMessage()
    }

    @Action("知乎热榜")
    @QMsg(at = true, atNewLine = true)
    fun zhiHuHot(): String{
        val list = toolLogic.zhiHuHot()
        val sb = StringBuilder()
        for (i in list.indices){
            val map = list[i]
            sb.appendLine("${i + 1}、${map["title"]}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("loc")
    @QMsg(at = true, atNewLine = true)
    fun loc(): String{
        val list = toolLogic.hostLocPost()
        val sb = StringBuilder()
        list.forEach { sb.appendLine("${it["title"]}-${it["name"]}-${it["time"]}").appendLine("------------") }
        return sb.removeSuffixLine().toString()
    }

    @Action("分词")
    @QMsg(at = true, atNewLine = true)
    fun wordSegmentation(qq: Long, session: ContextSession): String{
        reply(mif.at(qq).plus("请输入需要中文分词的内容！！"))
        val nextMessage = session.waitNextMessage()
        return toolLogic.wordSegmentation(nextMessage.firstString())
    }

    @Action("acg")
    fun acgPic() = mif.imageByUrl(toolLogic.acgPic())

    @Action("搜图 {img}")
    @QMsg(at = true)
    fun searchImage(img: Image): Message {
        val url = toolLogic.identifyPic(img.url)
        return if (url != null) mif.imageByUrl(img.url).plus(url)
        else "没有找到这张图片！！！".toMessage()
    }

    @Action("OCR {img}")
    @Synonym(["ocr {img}"])
    @QMsg(at = true, atNewLine = true)
    fun ocr(img: Image) = qqAiLogic.generalOCR(img.url)

    @Action("github加速")
    @QMsg(at = true)
    fun githubQuicken(session: ContextSession, qq: Long): String{
        reply(mif.at(qq).plus("请输入github文件下载链接"))
        val url = session.waitNextMessage().firstString()
        return BotUtils.shortUrl(toolLogic.githubQuicken(url))
    }

    @Action("traceroute {domain}")
    @Synonym(["路由追踪 {domain}"])
    fun traceRoute(domain: String) = toolLogic.traceRoute(domain)

    @Action("查发言数")
    fun queryMessage(group: Group): String{
        val map = messageService.findCountQQByGroupAndToday(group.id)
        val sb = StringBuilder().appendLine("本群今日发言数统计如下：")
        for ((k, v) in map){
            sb.appendLine("@${group[k].nameCardOrName()}（$k）：${v}条")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("语音合成 {text}")
    fun voice(text: String, group: Long, qq: Long){
        val commonResult = qqAiLogic.voiceSynthesis(text)
        if (commonResult.code == 200) {
            GlobalScope.launch {
                val miraiGroup = Bot.getInstance(yuq.botId).groups[group]
                miraiGroup.sendMessage(miraiGroup.uploadVoice(commonResult.t!!.inputStream()))
            }
        }else {
            reply(mif.at(qq).plus(commonResult.msg))
        }
    }

    @QMsg(at = true)
    @Action("防红 {url}")
    fun preventRed(url: String): String{
        val b = Random.nextBoolean()
        return if (b) toolLogic.preventQQRed(url)
        else toolLogic.preventQQWechatRed(url)
    }

    @Action("戳 {qqNo}")
    @QMsg(at = true)
    fun stamp(qqNo: Long, group: Long): String{
        if (protocol != "Android") return "戳一戳必须使用Android才能使用！！"
        val bot = Bot.getInstance(yuq.botId)
        val groupObj = bot.groups[group]
        GlobalScope.launch {
            val member = if (qqNo == bot.id) groupObj.botAsMember
            else groupObj.members[qqNo]
            groupObj.sendNudge(member.nudge())
        }
        return "戳成功！！"
    }
}
package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.firstString
import com.icecreamqaq.yuq.message.*
import com.icecreamqaq.yuq.toMessage
import me.kuku.yuq.logic.QQAILogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.ConfigService
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.image
import me.kuku.yuq.utils.removeSuffixLine
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@GroupController
class ToolController: QQController() {
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var qqAiLogic: QQAILogic
    @Inject
    private lateinit var configService: ConfigService
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String

    private var colorPicTime = 0L

    @QMsg(at = true)
    @Action("百度 {content}")
    fun teachYouBaidu(content: String) =
        "点击以下链接即可教您使用百度搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/baidu/index.html?${URLEncoder.encode(content, "utf-8")}")}"

    @QMsg(at = true)
    @Action("谷歌 {content}")
    fun teachYouGoogle(content: String) =
        "点击以下链接即可教您使用谷歌搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/google/index.html?${URLEncoder.encode(content, "utf-8")}")}"

    @QMsg(at = true, atNewLine = true)
    @Action("舔狗日记")
    fun dogLicking() = toolLogic.dogLicking()

    @QMsg(at = true, atNewLine = true)
    @Action("百科 {params}")
    fun baiKe(params: String) = toolLogic.baiKe(params)

    @QMsg(at = true, atNewLine = true)
    @Action("嘴臭")
    @Synonym(["祖安语录"])
    fun mouthOdor(group: Long): String {
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.mouthOdor != true) return "该功能已关闭！！"
        return toolLogic.mouthOdor()
    }

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

    @Action("\\.*\\")
    @QMsg(reply = true, at = true)
    fun chat(message: Message, qq: Contact): String?{
        val body = message.body
        val at = body[0]
        if (at is At && at.user == this.qq.toLong()){
            for (item in body){
                if (item is Text && item.text.trim() != "") {
                    return qqAiLogic.textChat(item.text, qq.id.toString())
                }
            }
        }
        return null
    }

    @Action("搜 {question}")
    @QMsg(at = true)
    fun search(question: String) = toolLogic.searchQuestion(question)

    @Action("\\.*B407F708-A2C6-A506-3420-98DF7CAC4A57.*\\")
    @Synonym(["\\.*65B7E786-ABB3-135D-15DF-2B1032B9A06D.*\\"])
    fun pic(group: Long, qq: Long) = this.colorPic(group, qq)

    @Action("涩图")
    @Synonym(["色图", "色图来"])
    fun colorPic(group: Long, qq: Long): Message {
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.colorPic != true) throw mif.at(qq).plus("该功能已关闭")
        return when (val type = qqGroupEntity.colorPicType){
            "native","r-18" -> {
                val url = toolLogic.colorPic(type)
                if (url.startsWith("http")){
                    val response = OkHttpClientUtils.get(url)
                    val bytes = OkHttpClientUtils.getBytes(response)
                    mif.imageToFlash(mif.image(bytes)).toMessage()
                }else url.toMessage()
            }
            "danbooru" -> mif.imageToFlash(mif.imageByUrl(toolLogic.danBooRuPic())).toMessage()
            "lolicon", "loliconR18" -> {
                val configEntity = configService.findByType("loLiCon") ?:
                        return mif.at(qq).plus("您还没有配置lolicon的apiKey，无法获取色图！！")
                val apiKey = configEntity.content
                val commonResult = toolLogic.colorPicByLoLiCon(apiKey, type == "loliconR18")
                val map = commonResult.t ?: return mif.at(qq).plus(commonResult.msg)
                val bytes = toolLogic.piXivPicProxy(map.getValue("url"))
                val line = System.getProperty("line.separator")
                mif.at(qq)
                        .plus(mif.image(bytes))
                        .plus("标题：${map["title"]}$line")
                        .plus("当日剩余额度：${map["count"]}$line")
                        .plus("恢复额度时间：${map["time"]}秒")
            }
            else -> "涩图类型不匹配！！".toMessage()
        }
    }

    @Action("涩图十连")
    @Synonym(["色图十连"])
    @Synchronized
    fun tenColorPic(group: Long, qq: Long){
        val time = Date().time
        val timeDifference = (time - colorPicTime) / 1000
        if (timeDifference < 120){
            reply("涩图十连还有${120 - timeDifference}s才允许被执行")
            return
        }else colorPicTime = time
        for (i in 0 until 10){
            val message = this.colorPic(group, qq)
            reply(message)
        }
    }

    @Action("点歌")
    fun song(group: Long, message: Message, qq: Long): Any?{
        if (message.toPath().size == 1) return mif.at(qq).plus("没有发现歌曲名字")
        val msgStr = message.body[0].toPath()
        val name = msgStr.substring(3)
        val qqGroupEntity = qqGroupService.findByGroup(group)
        return when (qqGroupEntity?.musicType ?: "qq") {
            "qq" -> {
                val jsonStr = toolLogic.songByQQ(name)
                mif.jsonEx(jsonStr)
            }
            "163" -> {
                val commonResult = toolLogic.songBy163(name)
                return if (commonResult.code == 200) {
                    mif.jsonEx(commonResult.t!!)
                }else commonResult.msg
            }
            else -> null
        }
    }

    @Action("菜单")
    fun menu() = "菜单它来了：https://w.url.cn/s/Adt25oJ"

    @Action("qr/{content}")
    @QMsg(at = true, atNewLine = true)
    fun creatQrCode(content: String): Message{
        val url = toolLogic.creatQr(content)
        return mif.imageByUrl(url).toMessage()
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
    fun time() = mif.image(toolLogic.queryTime())

    @Action("网抑")
    fun wy(): XmlEx{
        val num = Random.nextInt(2)
        return if (num == 1)
            mif.xmlEx(1, "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"1\" templateID=\"-1\" action=\"app\" actionData=\"com.netease.cloudmusic\" brief=\"点击启动网抑\" sourceMsgId=\"0\" url=\"http://y-8.top\" flag=\"2\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"12\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://imgurl.cloudimg.cc/2020/07/26/2a7410726090854.jpg\" w=\"0\" h=\"0\" /><title>启动网抑音乐</title></item><source name=\"今天你网抑了吗\" icon=\"\" action=\"\" appid=\"0\" /></msg>")
        else
            mif.xmlEx(1, "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"1\" templateID=\"-1\" action=\"app\" actionData=\"com.netease.cloudmusic\" brief=\"点击启动网抑\" sourceMsgId=\"0\" url=\"http://y-8.top\" flag=\"2\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"4\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://imgurl.cloudimg.cc/2020/07/26/2a7410726090854.jpg\" w=\"0\" h=\"0\" /><title>启动网抑音乐</title></item><source name=\"今天你网抑了吗\" icon=\"\" action=\"\" appid=\"0\" /></msg>")
    }

    @Action("跟我读")
    fun repeat(session: ContextSession, qq: Long): Message{
        reply(mif.at(qq).plus("您请说！！"))
        return session.waitNextMessage(30 * 1000)
    }

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
                    StringBuilder().appendln("标题：${map["title"]}")
                            .appendln("描述：${map["desc"]}")
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
            sb.appendln("${i + 1}、${map["title"]}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("loc")
    @QMsg(at = true, atNewLine = true)
    fun loc(): String{
        val list = toolLogic.hostLocPost()
        val sb = StringBuilder()
        list.forEach { sb.appendln("${it["title"]}-${it["name"]}-${it["time"]}").appendln("------------") }
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

    @Action("自闭")
    fun shut(qq: Member, group: Long): String?{
        if (yuq.groups[group]?.bot?.isAdmin() == true){
            if (qq.isAdmin()) return "以你的权限来看，我无法给予你想要的套餐呢！！"
            val time = Random.nextInt(11)
            qq.ban(time * 60)
        }
        return null
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
}
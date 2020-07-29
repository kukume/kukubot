package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.firstString
import com.icecreamqaq.yuq.message.*
import com.icecreamqaq.yuq.toMessage
import me.kuku.yuq.logic.PiXivLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.image
import java.net.URLEncoder
import javax.inject.Inject
import javax.script.ScriptEngineManager

@GroupController
@ContextController
class ToolController {
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var mf: MessageFactory
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var piXivLogic: PiXivLogic
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String

    @QMsg(at = true)
    @Action("百度/{content}")
    fun teachYouBaidu(content: String) =
        "点击以下链接即可教您使用百度搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/baidu/index.html?${URLEncoder.encode(content, "utf-8")}")}"

    @QMsg(at = true)
    @Action("谷歌/{content}")
    fun teachYouGoogle(content: String) =
        "点击以下链接即可教您使用谷歌搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/google/index.html?${URLEncoder.encode(content, "utf-8")}")}"

    @QMsg(at = true)
    @Action("舔狗日记")
    fun dogLicking() = toolLogic.dogLicking()

    @QMsg(at = true)
    @Action("百科/{params}")
    fun baiKe(params: String) = toolLogic.baiKe(params)

    @QMsg(at = true)
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
    fun shortUrl(params: String) = BotUtils.shortUrl(params)

    @Action("ip/{params}")
    fun queryIp(params: String) = toolLogic.queryIp(params)

    @Action("whois/{params}")
    fun queryWhois(params: String) = toolLogic.queryWhois(params)

    @Action("icp/{params}")
    fun queryIcp(params: String) = toolLogic.queryIcp(params)

    @Action("知乎日报")
    fun zhiHuDaily() = toolLogic.zhiHuDaily()

    @QMsg(at = true)
    @Action("测吉凶")
    fun qqGodLock(qq: Long) = toolLogic.qqGodLock(qq)

    @QMsg(at = true)
    @Action("拼音/{params}")
    fun convertPinYin(params: String) = toolLogic.convertPinYin(params)

    @QMsg(at = true)
    @Action("笑话")
    fun jokes() = toolLogic.jokes()

    @QMsg(at = true)
    @Action("垃圾/{params}")
    fun rubbish(params: String) = toolLogic.rubbish(params)

    @QMsg(at = true)
    @Action("转{str}")
    fun translate(str: String, session: ContextSession, group: Long, qq: Long): String{
        val map = mapOf("英" to "en", "中" to "zh", "日" to "jp", "韩" to "kor",
                "粤" to "yue", "法" to "gra", "俄" to "ru", "德" to "de", "文" to "wyw", "简" to "2", "繁" to "1")
        return if (map.containsKey(str)){
            yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq).plus("请输入需要翻译的内容！！")))
            val nextMessage = session.waitNextMessage(30 * 1000)
            val content = nextMessage.firstString()
            if (str == "简" || str == "繁"){
                toolLogic.convertZh(content, map.getValue(str).toInt())
            }else toolLogic.convertTranslate(content, "auto", map.getValue(str))
        }else "抱歉，没有该语言的翻译"
    }

    @Action("解析/{url}")
    fun parseVideo(url: String) = toolLogic.parseVideo(url)

    @Action("还原/{url}")
    fun restoreShortUrl(url: String) = toolLogic.restoreShortUrl(url)

    @Action("ping/{domain}")
    fun ping(domain: String) = toolLogic.ping(domain)

    @Action("\\.*\\")
    @QMsg(reply = true, at = true)
    fun js(message: Message): String?{
        val body = message.body
        val at = body[0]
        return if (at is At && at.user == this.qq.toLong()){
            var text = ""
            for (i in 1 until body.size)
                text += body[i].toPath()
            val se = ScriptEngineManager().getEngineByName("JavaScript")
            se.eval(text).toString()
        }else null
    }

    @Action("涩图")
    fun colorPic(group: Long): Message? {
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.colorPic != true) return "该功能已关闭".toMessage()
        return when (qqGroupEntity.colorPicType){
            "remote" -> mif.image(toolLogic.colorPic()).toMessage()
            "local" -> {
                val url = piXivLogic.bookMarks("13070512", "51918341_vhV0yUgHJVaJHaTH0zcREYiIOeDIokQq")
                val bytes = piXivLogic.getImage(url)
                mif.image(bytes).toMessage()
            }
            else -> mif.image(toolLogic.colorPic()).toMessage()
        }
    }

    @Action("点歌/{name}")
    fun song(name: String, group: Long): Any?{
        val qqGroupEntity = qqGroupService.findByGroup(group)
        return when (qqGroupEntity?.musicType ?: "qq") {
            "qq" -> {
                val jsonStr = toolLogic.songByQQ(name)
                mif.jsonEx(jsonStr)
            }
            "163" -> {
                val commonResult = toolLogic.songBy163(name)
                return if (commonResult.code == 200) {
                    mif.jsonEx(commonResult.t)
                }else commonResult.msg
            }
            else -> null
        }
    }

    @Action("菜单")
    fun menu() = "菜单如下：https://z6c.cn/lnecrr"

    @Action("qr/{content}")
    fun creatQrCode(content: String): Message{
        val url = toolLogic.creatQr(content)
        return mif.image(url).toMessage()
    }

    @Action("看美女")
    fun girl() = mif.image(toolLogic.girlImage())

    @QMsg(at = true)
    @Action("蓝奏/{url}")
    fun lanZou(url: String) = BotUtils.shortUrl("https://api.iheit.com/lanZou?url=${URLEncoder.encode(url, "utf-8")}")

    @Action("lol周免")
    fun lolFree() = toolLogic.lolFree()

    @Action("缩写/{content}")
    fun abbreviation(content: String) = toolLogic.abbreviation(content)

    @Action("几点了")
    @Synonym(["多久了", "时间"])
    fun time() = mif.image(toolLogic.queryTime())

    @Action("网抑")
    fun wy() = mif.xmlEx(1, "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"1\" templateID=\"-1\" action=\"app\" actionData=\"com.netease.cloudmusic\" brief=\"点击启动网抑\" sourceMsgId=\"0\" url=\"http://y-8.top\" flag=\"2\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"12\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://imgurl.cloudimg.cc/2020/07/26/2a7410726090854.jpg\" w=\"0\" h=\"0\" /><title>启动网抑音乐</title></item><source name=\"今天你网抑了吗\" icon=\"\" action=\"\" appid=\"0\" /></msg>")
}
package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Config
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.message.*
import me.kuku.yuq.service.ToolService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.image
import java.net.URLEncoder
import javax.inject.Inject
import javax.script.ScriptEngineManager

@GroupController
@ContextController
class ToolController {

    @Inject
    private lateinit var toolService: ToolService
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var mf: MessageFactory
    @Inject
    private lateinit var yuq: YuQ
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String

    @Action("百度/{content}")
    fun teachYouBaidu(content: String) =
        "点击以下链接即可教您使用百度搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/baidu/index.html?${URLEncoder.encode(content, "utf-8")}")}"

    @Action("谷歌/{content}")
    fun teachYouGoogle(content: String) =
        "点击以下链接即可教您使用谷歌搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/google/index.html?${URLEncoder.encode(content, "utf-8")}")}"

    @Action("舔狗日记")
    fun dogLicking() = toolService.dogLicking()

    @Action("百科")
    fun baiKe(@PathVar(1) params: String?) = if(params != null) toolService.baiKe(params) else "缺少参数，需要百科的内容"

    @Action("嘴臭")
    fun mouthOdor() = toolService.mouthOdor()

    @Action("毒鸡汤")
    fun poisonousChickenSoup() = toolService.poisonousChickenSoup()

    @Action("名言")
    fun saying() = toolService.saying()

    @Action("缩短/{params}")
    fun shortUrl(params: String) = BotUtils.shortUrl(params)

    @Action("ip/{params}")
    fun queryIp(params: String) = toolService.queryIp(params)

    @Action("whois/{params}")
    fun queryWhois(params: String) = toolService.queryWhois(params)

    @Action("icp/{params}")
    fun queryIcp(params: String) = toolService.queryIcp(params)

    @Action("知乎日报")
    fun zhiHuDaily() = toolService.zhiHuDaily()

    @Action("测吉凶")
    fun qqGodLock(qq: Long) = mif.at(qq).plus(toolService.qqGodLock(qq))

    @Action("拼音/{params}")
    fun convertPinYin(params: String) = toolService.convertPinYin(params)

    @Action("笑话")
    fun jokes() = toolService.jokes()

    @Action("垃圾/{params}")
    fun rubbish(params: String) = toolService.rubbish(params)

    @Action("历史上的今天")
    fun historyToday() = mif.text(toolService.historyToday())

    @Action("转{str}/{content}")
    fun translate(str: String, content: String): String{
        return when(str){
            "简" -> toolService.convertZh(content, 2)
            "繁" -> toolService.convertZh(content, 1)
            "英" -> toolService.convertTranslate(content, "auto", "en")
            "中" -> toolService.convertTranslate(content, "auto", "zh")
            "日" -> toolService.convertTranslate(content, "auto", "jp")
            "韩" -> toolService.convertTranslate(content, "auto", "kor")
            "粤" -> toolService.convertTranslate(content, "auto", "yue")
            "法" -> toolService.convertTranslate(content, "auto", "gra")
            "俄" -> toolService.convertTranslate(content, "auto", "ru")
            "德" -> toolService.convertTranslate(content, "auto", "de")
            "文" -> toolService.convertTranslate(content, "auto", "wyw")
            else -> "抱歉，没有该语言的翻译"
        }
    }

    @Action("解析/{url}")
    fun parseVideo(url: String) = toolService.parseVideo(url)

    @Action("还原/{url}")
    fun restoreShortUrl(url: String) = toolService.restoreShortUrl(url)

    @Action("天气/{local}")
    fun weather(local: String) = toolService.weather(local)

    @Action("ping/{domain}")
    fun ping(domain: String) = toolService.ping(domain)

    @Action("\\.*\\")
    @QMsg(reply = true)
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
    fun colorPic(group: Long, qq: Long): Message {
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请稍后~~~~"))
        return mif.image(toolService.colorPic()).toMessage()
    }

    @Action("点歌/{name}")
    fun song(name: String): Any{
        val jsonStr = toolService.songByQQ(name)
        return mif.jsonEx(jsonStr)
    }

    /*@Action("点歌")
    fun song(@PathVar(1) name: String?): Any{
        return if (name != null){
            val commonResult = toolService.songBy163(name)
            if (commonResult.code == 200)
                mif.jsonEx(commonResult.t)
            else commonResult.msg
        }else "缺少参数，歌曲的名字！"
    }*/

    @Action("菜单")
    fun menu() = "菜单如下：https://sohu.gg/eJRM5U"

    @Action("qr/{content}")
    fun creatQrCode(content: String): Message{
        val url = toolService.creatQr(content)
        return mif.image(url).toMessage()
    }

    @Action("看美女")
    fun girl() = mif.image(toolService.girlImage())
}
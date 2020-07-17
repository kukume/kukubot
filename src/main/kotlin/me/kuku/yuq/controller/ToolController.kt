package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Config
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.message.*
import com.icecreamqaq.yuq.toMessage
import io.ktor.http.encodeURLPath
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
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String

    @Action("百度/{content}")
    fun teachYouBaidu(content: String) =
        "点击以下链接即可教您使用百度搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/baidu/index.html?${URLEncoder.encode(content, "utf-8")}")}"

    @Action("谷歌/{content}")
    fun teachYouGoogle(content: String) =
        "点击以下链接即可教您使用谷歌搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/google/index.html?${URLEncoder.encode(content, "utf-8")}")}"

    @Action("舔狗日记")
    fun dogLicking() = toolLogic.dogLicking()

    @Action("百科/{params}")
    fun baiKe(params: String) = toolLogic.baiKe(params)

    @Action("嘴臭")
    fun mouthOdor() = toolLogic.mouthOdor()

    @Action("毒鸡汤")
    fun poisonousChickenSoup() = toolLogic.poisonousChickenSoup()

    @Action("名言")
    fun saying() = toolLogic.saying()

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

    @Action("测吉凶")
    fun qqGodLock(qq: Long) = mif.at(qq).plus(toolLogic.qqGodLock(qq))

    @Action("拼音/{params}")
    fun convertPinYin(params: String) = toolLogic.convertPinYin(params)

    @Action("笑话")
    fun jokes() = toolLogic.jokes()

    @Action("垃圾/{params}")
    fun rubbish(params: String) = toolLogic.rubbish(params)

    @Action("历史上的今天")
    fun historyToday() = mif.text(toolLogic.historyToday())

    @Action("转{str}/{content}")
    fun translate(str: String, content: String): String{
        return when(str){
            "简" -> toolLogic.convertZh(content, 2)
            "繁" -> toolLogic.convertZh(content, 1)
            "英" -> toolLogic.convertTranslate(content, "auto", "en")
            "中" -> toolLogic.convertTranslate(content, "auto", "zh")
            "日" -> toolLogic.convertTranslate(content, "auto", "jp")
            "韩" -> toolLogic.convertTranslate(content, "auto", "kor")
            "粤" -> toolLogic.convertTranslate(content, "auto", "yue")
            "法" -> toolLogic.convertTranslate(content, "auto", "gra")
            "俄" -> toolLogic.convertTranslate(content, "auto", "ru")
            "德" -> toolLogic.convertTranslate(content, "auto", "de")
            "文" -> toolLogic.convertTranslate(content, "auto", "wyw")
            else -> "抱歉，没有该语言的翻译"
        }
    }

    @Action("解析/{url}")
    fun parseVideo(url: String) = toolLogic.parseVideo(url)

    @Action("还原/{url}")
    fun restoreShortUrl(url: String) = toolLogic.restoreShortUrl(url)

    @Action("ping/{domain}")
    fun ping(domain: String) = toolLogic.ping(domain)

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
        val qqGroupEntity = qqGroupService.findByGroup(group)
        if (qqGroupEntity?.colorPic != true) return "该功能已关闭".toMessage()
        yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus("请稍后~~~~"))
        return mif.image(toolLogic.colorPic()).toMessage()
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

    @Action("蓝奏/{url}")
    fun lanZou(url: String) = BotUtils.shortUrl("https://api.iheit.com/lanZou?url=${url.encodeURLPath()}")

    @Action("lol周免")
    fun lolFree() = toolLogic.lolFree()

    @Action("缩写/{content}")
    fun abbreviation(content: String) = toolLogic.abbreviation(content)

    @After
    fun finally(actionContext: BotActionContext) = BotUtils.addAt(actionContext)
}
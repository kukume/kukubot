package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Path
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.message.*
import me.kuku.yuq.service.impl.ToolServiceImpl
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.image
import java.net.URLEncoder
import javax.inject.Inject
import javax.script.ScriptEngineManager

@GroupController
@ContextController
class ToolController {

    @Inject
    private lateinit var toolService: ToolServiceImpl
    @Inject
    private lateinit var mif: MessageItemFactory
    @Inject
    private lateinit var mf: MessageFactory
    @Inject
    private lateinit var yuq: YuQ
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String

    @Action("百度")
    fun teachYouBaidu(@PathVar(1) content: String?) =
        if (content != null)
            "点击以下链接即可教您使用百度搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/baidu/index.html?${URLEncoder.encode(content, "utf-8")}")}"
        else "缺少参数，需要百度的内容"

    @Action("谷歌")
    fun teachYouGoogle(@PathVar(1) content: String?) =
        if (content != null)
            "点击以下链接即可教您使用谷歌搜索“$content”\n${BotUtils.shortUrl("https://u.iheit.com/google/index.html?${URLEncoder.encode(content, "utf-8")}")}"
        else "缺少参数，需要谷歌的内容"

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

    @Action("缩短")
    fun shortUrl(@PathVar(1) params: String?) = if (params != null) BotUtils.shortUrl(params) else "缺少参数，需要缩短的网址"

    @Action("ip")
    fun queryIp(@PathVar(1) params: String?) = if (params != null) toolService.queryIp(params) else "缺少参数，需要查询的IP"

    @Action("whois")
    fun queryWhois(@PathVar(1) params: String?) = if (params != null) toolService.queryWhois(params) else "缺少参数，需要查询的域名"

    @Action("icp")
    fun queryIcp(@PathVar(1) params: String?) = if (params != null) toolService.queryIcp(params) else "缺少参数，需要查询的域名"

    @Action("知乎日报")
    fun zhiHuDaily() = toolService.zhiHuDaily()

    @Action("测吉凶")
    fun qqGodLock(qq: Long) = mif.at(qq).plus(toolService.qqGodLock(qq))

    @Action("拼音")
    fun convertPinYin(@PathVar(1) params: String?) = if (params != null) toolService.convertPinYin(params) else "缺少参数，需要转换的文字"

    @Action("笑话")
    fun jokes() = toolService.jokes()

    @Action("垃圾")
    fun rubbish(@PathVar(1) params: String?) = if (params != null) toolService.rubbish(params) else "缺少参数，需要查询的垃圾名称"

    @Action("历史上的今天")
    fun historyToday() = mif.text(toolService.historyToday())

    @Action("\\转.\\")
    fun translate(@PathVar(0) str: String, @PathVar(1) content: String?): String{
        return if (content != null){
            when(str[1]){
                '简' -> toolService.convertZh(content, 2)
                '繁' -> toolService.convertZh(content, 1)
                '英' -> toolService.convertTranslate(content, "auto", "en")
                '中' -> toolService.convertTranslate(content, "auto", "zh")
                '日' -> toolService.convertTranslate(content, "auto", "jp")
                '韩' -> toolService.convertTranslate(content, "auto", "kor")
                '粤' -> toolService.convertTranslate(content, "auto", "yue")
                '法' -> toolService.convertTranslate(content, "auto", "gra")
                '俄' -> toolService.convertTranslate(content, "auto", "ru")
                '德' -> toolService.convertTranslate(content, "auto", "de")
                '文' -> toolService.convertTranslate(content, "auto", "wyw")
                else -> "抱歉，没有该语言的翻译"
            }
        }else "缺少参数，需要转换的文字"
    }

    @Action("解析")
    fun parseVideo(@PathVar(1) url: String?) = if (url != null) toolService.parseVideo(url) else "缺少参数，需要解析的短视频链接"

    @Action("还原")
    fun restoreShortUrl(@PathVar(1) url: String?) = if (url != null) toolService.restoreShortUrl(url) else "缺少参数，需要转换的链接"

    @Action("天气")
    fun weather(@PathVar(1) local: String?) = if (local != null) toolService.weather(local) else "缺少参数，需要查询天气的地方"

    @Action("ping")
    fun ping(@PathVar(1) domain: String?) = if (domain != null) toolService.ping(domain) else "缺少参数，需要ping的域名"

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

    @Action("xml")
    @NextContext("nextXml")
    fun xml() = "请输入xml文本内容"

    @Action("nextXml")
    fun nextXml(message: Message): Message{
        val xmlStr = message.body[0].toPath()
        return mif.xmlEx(1, xmlStr).toMessage()
    }

    @Action("json")
    @NextContext("nextJson")
    fun json() = "请输入json文本内容"

    @Action("nextJson")
    fun nextJson(message: Message): Message{
        val jsonStr = message.body[0].toPath()
        return mif.jsonEx(jsonStr).toMessage()
    }

    @Action("点歌")
    fun song(@PathVar(1) name: String?): Any{
        return if (name != null){
            val jsonStr = toolService.songByQQ(name)
            mif.jsonEx(jsonStr)
        }else "缺少参数，歌曲的名字！"
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

    @Action("qr")
    fun creatQrCode(@PathVar(1) content: String?): Message{
        return if (content != null){
            val url = toolService.creatQr(content)
            mif.image(url).toMessage()
        }else mif.text("缺少参数[需要生产二维码的内容]").toMessage()
    }

    @Action("看美女")
    fun girl() = mif.image(toolService.girlImage())

    @Action("\\发个.包\\")
    fun sendEnvelope(/*color: String,*/ @PathVar(0) content: String): JsonEx {
        val color = content[2]
        return mif.jsonEx("{\"app\":\"com.tencent.cmshow\",\"desc\":\"\",\"view\":\"game_redpacket\",\"ver\":\"1.0.3.5\",\"prompt\":\"QQ${color}包\",\"appID\":\"\",\"sourceName\":\"\",\"actionData\":\"\",\"actionData_A\":\"\",\"sourceUrl\":\"\",\"meta\":{\"redPacket\":{\"destUrl\":\".2.15844517927 .com\",\"msg\":\"QQ${color}包\",\"posterUrl\":\"\\/qqshow\\/admindata\\/comdata\\/vipActTpl_mobile_zbltyxn\\/dddb247a4a9c6d34757c160f9e0b6669.gif\"}},\"config\":{\"forward\":1},\"text\":\"\",\"sourceAd\":\"\"}")
    }
}
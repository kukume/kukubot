package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface ToolLogic {
    fun dogLicking() : String
    fun baiKe(text: String): String
    fun mouthOdor(): String
    fun mouthSweet(): String
    fun poisonousChickenSoup(): String
    fun loveWords(): String
    fun saying(): String
    fun queryIp(ip: String): String
    fun queryWhois(domain: String): String
    fun queryIcp(domain: String): String
    fun zhiHuDaily(): String
    fun qqGodLock(qq: Long): String
    fun convertPinYin(word: String): String
    fun jokes(): String
    fun rubbish(name: String): String
    fun historyToday(): String
    fun convertZh(content: String, type: Int): String
    fun convertTranslate(content: String, from: String, to: String): String
    fun parseVideo(url: String): String
    fun restoreShortUrl(url: String): String
    fun weather(local: String, cookie: String): CommonResult<String>
    fun ping(domain: String): String
    fun colorPicByLoLiCon(apiKey: String, isR18: Boolean): CommonResult<Map<String, String>>
    fun piXivPicProxy(url: String): ByteArray
    fun r18setting(cookie: String, isOpen: Boolean): String
    fun hiToKoTo(): Map<String, String>
    fun creatQr(content: String): String
    fun girlImage(): String
    fun lolFree(): String
    fun abbreviation(content: String): String
    fun queryTime(): ByteArray
    fun queryVersion(): String
    fun music163cloud(): String
    fun searchQuestion(question: String): String
    fun bvToAv(bv: String): CommonResult<Map<String, String>>
    fun zhiHuHot(): List<Map<String, String>>
    fun hostLocPost(): List<Map<String, String>>
    fun wordSegmentation(text: String): String
    fun acgPic(): String
    fun danBooRuPic(): Map<String, String>
    fun identifyPic(url: String): String?
    fun githubQuicken(gitUrl: String): String
    fun traceRoute(domain: String): String
    fun teachYou(content: String, type: String): String?
}
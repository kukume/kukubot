package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface ToolService {
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
    fun weather(local: String): String
    fun ping(domain: String): String
    fun colorPic(): ByteArray
    fun hiToKoTo(): Map<String, String>
    fun songByQQ(name: String): String
    fun songBy163(name: String): CommonResult<String>
    fun creatQr(content: String): String
    fun girlImage(): String
}
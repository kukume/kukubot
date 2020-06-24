package me.kuku.yuq.utils

import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.MessageItemFactory
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.URLEncoder
import kotlin.random.Random

object BotUtils {

    fun shortUrl(url: String): String{
        val response = OkHttpClientUtils.get("https://sohu.gg/api/?key=pimRuFeT7vKK&url=${URLEncoder.encode(url, "utf-8")}")
        return OkHttpClientUtils.getStr(response)
    }

    fun randomStr(len: Int): String{
        val str = "abcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder()
        for (i in (0 until len))
            sb.append(str[Random.nextInt(0, str.length)])
        return sb.toString()
    }

    fun randomNum(len: Int): String{
        val sb = StringBuilder()
        for (i in (0 until len)){
            sb.append(Random.nextInt(10))
        }
        return sb.toString()
    }

    fun regex(regex: String, text: String): String? {
        val r = Regex(regex)
        val find = r.find(text)
        return find?.value
    }

    fun regex(first: String, last: String , text: String): String? {
        val regex = "(?<=$first).*?(?=$last)"
        return this.regex(regex, text)
    }

}

fun MessageItemFactory.image(byteArray: ByteArray): Image {
    val md5Str = MD5Utils.toMD5(byteArray)
    val file = File("${System.getProperty("user.home")}${File.separator}.kuku${File.separator}images${File.separator}$md5Str.jpg")
    FileUtils.writeByteArrayToFile(file, byteArray)
    return this.image(file)
}
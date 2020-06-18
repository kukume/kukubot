package me.kuku.yuq.utils

import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.MessageItemFactory
import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.random.Random

object BotUtils {

    fun shortUrl(url: String): String{
        val response = OkHttpClientUtils.post("https://create.ft12.com/go.php?m=index&a=urlCreate",
                OkHttpClientUtils.addForms("url", url, "type", "rrd", "random", randomNum(16)))
        return OkHttpClientUtils.getJson(response).getString("list")
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
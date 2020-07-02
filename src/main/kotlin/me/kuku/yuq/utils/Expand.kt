package me.kuku.yuq.utils

import com.IceCreamQAQ.Yu.util.IO
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.MessageItemFactory
import java.io.File


fun MessageItemFactory.image(byteArray: ByteArray): Image {
    val md5Str = MD5Utils.toMD5(byteArray)
//    val file = File("${System.getProperty("user.home")}${File.separator}.kuku${File.separator}images${File.separator}$md5Str.jpg")
    val file = File("tmp/$md5Str.jpg")
    IO.writeFile(file, byteArray)
    return this.image(file)
}
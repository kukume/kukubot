package me.kuku.yuq.utils

import com.IceCreamQAQ.Yu.util.IO
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.MessageItemFactory
import java.io.File


fun MessageItemFactory.image(byteArray: ByteArray): Image {
    val md5Str = MD5Utils.toMD5(byteArray)
    val file = File("tmp/$md5Str")
    IO.writeFile(file, byteArray)
    return this.imageByFile(file)
}

fun StringBuilder.removeSuffixLine() = this.removeSuffix(System.getProperty("line.separator"))

package me.kuku.yuq.utils

import com.IceCreamQAQ.Yu.util.IO
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.MessageItemFactory
import com.icecreamqaq.yuq.mf
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import java.io.File


fun MessageItemFactory.image(byteArray: ByteArray): Image {
    val md5Str = MD5Utils.toMD5(byteArray)
//    val file = File("${System.getProperty("user.home")}${File.separator}.kuku${File.separator}images${File.separator}$md5Str.jpg")
    val file = File("tmp/$md5Str")
    IO.writeFile(file, byteArray)
    return this.image(file)
}

fun String.at(qq: Long) = mif.at(qq).plus("\n$this")

fun String.groupMsgAndAt(group: Long, qq: Long) = yuq.sendMessage(mf.newGroup(group).plus(mif.at(qq)).plus(this))

fun String.groupMsg(group: Long) = yuq.sendMessage(mf.newGroup(group).plus(this))

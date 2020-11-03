package me.kuku.yuq

import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.IceCreamQAQ.Yu.util.IO
import com.icecreamqaq.yuq.mirai.YuQMiraiStart
import java.io.File


fun main(){
    val deviceName = "device.json"
    val confDeviceFile = File("conf/$deviceName")
    val rootDeviceFile = File(deviceName)
    if (confDeviceFile.exists() && !rootDeviceFile.exists()){
        IO.writeFile(rootDeviceFile, confDeviceFile.readBytes())
    }
    AppClassloader.registerTransformerList("com.IceCreamQAQ.Yu.web.WebClassTransformer")
    YuQMiraiStart.start()
}
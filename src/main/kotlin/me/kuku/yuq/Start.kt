package me.kuku.yuq

import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.icecreamqaq.yuq.mirai.YuQMiraiStart


fun main(){
    AppClassloader.registerTransformerList("com.IceCreamQAQ.Yu.web.WebClassTransformer")
    YuQMiraiStart.start()
}
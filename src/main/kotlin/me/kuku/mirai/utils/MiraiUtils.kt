package me.kuku.mirai.utils

import net.mamoe.mirai.Bot

object MiraiUtils {

    fun auth(domain: String): MiraiAuth {
        val bot = SpringUtils.getBean<Bot>()
        val botClazz = bot::class.java
        val clientMethod = botClazz.getDeclaredMethod("getClient").also { it.isAccessible = true }
        val client = clientMethod.invoke(bot)
        val clientClazz = client::class.java
        val sigInfoMethod = clientClazz.getDeclaredMethod("getWLoginSigInfo").also { it.isAccessible = true }
        val wLoginSigInfo = sigInfoMethod.invoke(client)
        val sigInfoClazz = wLoginSigInfo::class.java
        val sKeyAny = sigInfoClazz.getDeclaredMethod("getSKey").also { it.isAccessible = true }.invoke(wLoginSigInfo)
        val sKey = key(sKeyAny)
        val miraiAuth = MiraiAuth()
        miraiAuth.sKey = sKey
        val psKey =
            sigInfoClazz.getDeclaredMethod("getPsKey", String::class.java).also { it.isAccessible = true }.invoke(wLoginSigInfo, domain).toString()
        miraiAuth.psKey = psKey
        miraiAuth.qq = bot.id
        return miraiAuth
    }

    private fun key(any: Any): String {
        val clazz = any::class.java
        val bytes = clazz.getDeclaredMethod("getData").also { it.isAccessible = true }.invoke(any) as ByteArray
        return bytes.decodeToString()
    }

}

class MiraiAuth {
    var sKey: String = ""
    var qq: Long = 0
    var psKey: String = ""

}

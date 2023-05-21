package me.kuku.mirai.utils

import net.mamoe.mirai.Bot

object MiraiUtils {

    fun auth(): MiraiAuth {
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
        val map = sigInfoClazz.getDeclaredMethod("getPsKeyMap").also { it.isAccessible = true }.invoke(wLoginSigInfo) as Map<String, Any>
        for ((k, v) in map) {
            miraiAuth.psKey[k] = key(v)
        }
        miraiAuth.qq = bot.id
        return miraiAuth
    }

    private fun key(any: Any): String {
        val clazz = any::class.java
        val bytes = clazz.getDeclaredMethod("getData").also { it.isAccessible = true }.invoke(any) as ByteArray
        return String(bytes)
    }

}

class MiraiAuth {
    var sKey: String = ""
    var qq: Long = 0
    var psKey: MutableMap<String, String> = mutableMapOf()

}

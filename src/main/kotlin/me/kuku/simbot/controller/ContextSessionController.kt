package me.kuku.simbot.controller

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import love.forte.simbot.annotation.*
import love.forte.simbot.api.message.events.GroupMsg
import love.forte.simbot.api.message.events.MsgGet
import love.forte.simbot.api.message.events.PrivateMsg
import me.kuku.simbot.exception.WaitNextMessageTimeoutException
import me.kuku.simbot.utils.SpringUtils
import org.springframework.cache.CacheManager
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.stereotype.Service
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private var cacheManager: CacheManager? = null

@Service
class ContextSessionController constructor(cacheManager: CacheManager){

    private val cache = cacheManager.getCache("ContextSession")!!

    @OnGroup
    fun GroupMsg.getNextMessage(groupMsg: GroupMsg) {
        val botCode = botInfo.accountCode
        val map = (cache.get(botCode)?.get() as? MutableMap<String, Continuation<String>>) ?: return
        // 如果能够remove，则说明存在挂起，提供此值。
        val code = accountInfo.accountCode
        map.remove(code)?.resume(groupMsg.msg)
    }

    @OnPrivate
    fun PrivateMsg.getNextMessage(privateMsg: PrivateMsg){
        val botCode = botInfo.accountCode
        val map = (cache.get(botCode)?.get() as? MutableMap<String, Continuation<String>>) ?: return
        // 如果能够remove，则说明存在挂起，提供此值。
        val code = accountInfo.accountCode
        map.remove(code)?.resume(privateMsg.msg)
    }

}

class ContextSession constructor(private val msgGet: MsgGet){

    fun waitNextMessage(maxTime: Long): String{
        val code = msgGet.accountInfo.accountCode
        val botCode = msgGet.botInfo.accountCode
        return waitNextMessageCommon(code, botCode, maxTime)
    }

    fun waitNextMessage(): String{
        return waitNextMessage(30000L)
    }

}

private fun waitNextMessageCommon(code: String, botCode: String, maxTime: Long): String{
    if (cacheManager == null){
        synchronized(ContextSessionController::class.java){
            if (cacheManager == null){
                cacheManager = SpringUtils.getBean(EhCacheCacheManager::class.java)
            }
        }
    }
    val cache = cacheManager?.getCache("ContextSession")!!
    var map = cache.get(botCode)?.get() as? MutableMap<String, Continuation<String>>
    if (map == null){
        map = mutableMapOf()
        cache.put(botCode, map)
    }
    return runBlocking {
        try {
            withTimeout(maxTime){
                val msg = suspendCoroutine<String>{
                    map.merge(code, it){ _, _ ->
                        throw IllegalStateException("Account $code was still waiting.")
                    }
                }
                msg
            }
        }catch (e: Exception){
            map.remove(code)
            throw WaitNextMessageTimeoutException()
        }
    }
}

fun GroupMsg.waitNextMessage(maxTime: Long = 3000): String{
    val code = accountInfo.accountCode
    val botCode = botInfo.accountCode
    return waitNextMessageCommon(code, botCode, maxTime)
}

fun PrivateMsg.waitNextMessage(maxTime: Long = 3000): String{
    val code = accountInfo.accountCode
    val botCode = botInfo.accountCode
    return waitNextMessageCommon(code, botCode, maxTime)
}
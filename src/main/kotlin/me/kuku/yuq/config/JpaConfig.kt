package me.kuku.yuq.config

import com.IceCreamQAQ.Yu.annotation.HookBy
import com.IceCreamQAQ.Yu.hook.HookMethod
import com.IceCreamQAQ.Yu.hook.HookRunnable
import kotlinx.coroutines.runBlocking
import me.kuku.yuq.utils.SpringUtils
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate

suspend fun <T> TransactionTemplate.executeBlock(block: suspend () -> T?): T? {
    return this.execute {
        runBlocking {
            block.invoke()
        }
    }
}


@HookBy("me.kuku.yuq.config.JpaHook")
annotation class Transactional


class JpaHook: HookRunnable {

    override fun preRun(method: HookMethod): Boolean {
        val transactionStatus = SpringUtils.getBean<PlatformTransactionManager>().getTransaction(TransactionDefinition.withDefaults())
        method.saveInfo("transactionStatus", transactionStatus)
        return false
    }

    override fun postRun(method: HookMethod) {
        val transactionStatus = method.getInfo("transactionStatus") as TransactionStatus
        SpringUtils.getBean<PlatformTransactionManager>().commit(transactionStatus)
    }

    override fun onError(method: HookMethod): Boolean {
        val transactionStatus = method.getInfo("transactionStatus") as TransactionStatus
        SpringUtils.getBean<PlatformTransactionManager>().rollback(transactionStatus)
        return false
    }
}
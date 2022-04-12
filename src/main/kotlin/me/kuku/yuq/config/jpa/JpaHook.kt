package me.kuku.yuq.config.jpa

import com.IceCreamQAQ.Yu.annotation.HookBy
import com.IceCreamQAQ.Yu.hook.HookMethod
import com.IceCreamQAQ.Yu.hook.HookRunnable
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus

@HookBy("me.kuku.yuq.jpa.JpaHook")
annotation class Transactional

lateinit var transactionalManager: PlatformTransactionManager

class JpaHook: HookRunnable {

    override fun preRun(method: HookMethod): Boolean {
        val transactionStatus = transactionalManager.getTransaction(TransactionDefinition.withDefaults())
        method.saveInfo("transactionStatus", transactionStatus)
        return false
    }

    override fun postRun(method: HookMethod) {
        val transactionStatus = method.getInfo("transactionStatus") as TransactionStatus
        transactionalManager.commit(transactionStatus)
    }

    override fun onError(method: HookMethod): Boolean {
        val transactionStatus = method.getInfo("transactionStatus") as TransactionStatus
        transactionalManager.rollback(transactionStatus)
        return false
    }
}
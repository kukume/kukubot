@file:Suppress("UNCHECKED_CAST")

package me.kuku.yuq

import com.IceCreamQAQ.Yu.di.ClassContext
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.loader.AppClassloader
import com.IceCreamQAQ.Yu.module.Module
import com.icecreamqaq.yuq.artqq.YuQArtQQStarter
import me.kuku.utils.MyUtils
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.util.*
import javax.inject.Inject
import javax.persistence.EntityManagerFactory

fun main(args: Array<String>) {
    AppClassloader.registerBackList(listOf("org.springframework", "me.kuku.yuq.entity"))
    YuQArtQQStarter.start(args)
}

@Configuration
@ComponentScan(basePackages = ["me.kuku.yuq.entity"])
@EnableJpaRepositories(basePackages = ["me.kuku.yuq.entity"])
open class JpaConfig{

    @Bean
    open fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.setPackagesToScan("me.kuku.yuq.entity")
        em.jpaVendorAdapter = HibernateJpaVendorAdapter()
        val properties = Properties()
        properties.load(Thread.currentThread().contextClassLoader.getResourceAsStream("hibernate.properties"))
        em.setJpaProperties(properties)
        return em
    }

    @Bean
    open fun transactionManager(emf: EntityManagerFactory): JpaTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = emf
        return transactionManager
    }

}

private lateinit var transactionManager: JpaTransactionManager

fun transaction(block: () -> Unit) {
    val transactionDefinition = DefaultTransactionDefinition()
    val ts = transactionManager.getTransaction(transactionDefinition)
    try {
        block()
        transactionManager.commit(ts);
    }catch (e: Exception) {
        transactionManager.rollback(ts)
    }
}


class JpaModule: Module {

    @Inject
    private lateinit var context: YuContext

    override fun onLoad() {
        val applicationContext = AnnotationConfigApplicationContext(JpaConfig::class.java)
        transactionManager = applicationContext.getBean(JpaTransactionManager::class.java)
        val classes = MyUtils.getClasses("me.kuku.yuq")
        for ((_, v) in classes) {
            v.interfaces.takeIf { it.contains(JpaRepository::class.java) }
                ?.let {
                    val repository = applicationContext.getBean(v)
                    val name = v.name
                    val classContextMap = context::class.java.declaredFields.first { it.name == "classContextMap" }
                        .also { it.isAccessible = true }.get(context) as MutableMap<String, ClassContext>
                    val classContext =
                        ClassContext(name, v, false, null, repository, mutableMapOf("" to repository), null, null)
                    classContextMap[name] = classContext
                }
        }
    }
}
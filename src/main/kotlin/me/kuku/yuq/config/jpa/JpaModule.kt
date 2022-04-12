@file:Suppress("UNCHECKED_CAST")

package me.kuku.yuq.config.jpa

import com.IceCreamQAQ.Yu.di.ClassContext
import com.IceCreamQAQ.Yu.di.YuContext
import com.IceCreamQAQ.Yu.module.Module
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import me.kuku.utils.MyUtils
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import javax.inject.Inject
import javax.persistence.EntityManagerFactory

@Configuration
@EnableJpaRepositories(basePackages = ["me.kuku.yuq.entity"])
@EnableTransactionManagement
@EnableJpaAuditing
open class JpaConfig{

    @Bean
    open fun dataSource(): HikariDataSource {
        return HikariDataSource()
    }

    @Bean
    open fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter  = HibernateJpaVendorAdapter()
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.setPackagesToScan("me.kuku.yuq.entity")
        factory.jpaVendorAdapter = vendorAdapter
        val properties = Properties()
        properties.load(Thread.currentThread().contextClassLoader.getResourceAsStream("hibernate.properties"))
        factory.setJpaProperties(properties)
        factory.dataSource = dataSource()
        return factory
    }

    @Bean
    open fun transactionManager(emf: EntityManagerFactory): JpaTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = emf
        return transactionManager
    }

    @Bean
    open fun transactionTemplate(transactionManager: JpaTransactionManager): TransactionTemplate {
        return TransactionTemplate(transactionManager)
    }

}

class JpaModule: Module {

    @Inject
    private lateinit var context: YuContext

    override fun onLoad() {
        val applicationContext = AnnotationConfigApplicationContext(JpaConfig::class.java)
        context.putBean(applicationContext)
        val transactionTemplate = applicationContext.getBean(TransactionTemplate::class.java)
        context.putBean(transactionTemplate)
        val platformTransactionManager = applicationContext.getBean<PlatformTransactionManager>()
        transactionalManager = platformTransactionManager
        val classes = MyUtils.getClasses("me.kuku.yuq.entity")
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

suspend fun <T> TransactionTemplate.executeBlock(block: suspend () -> T?): T? {
    return this.execute {
        runBlocking {
            block.invoke()
        }
    }
}
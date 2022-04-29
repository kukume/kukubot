package me.kuku.yuq

import com.IceCreamQAQ.Yu.loader.AppClassloader
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class KuKuBotApplication

fun main(args: Array<String>) {
    AppClassloader.registerBackList(listOf("ch.qos",
        "org.springframework", "org.hibernate", "org.aopalliance", "com.querydsl"))
    val appClassLoader = AppClassloader(KuKuBotApplication::class.java.classLoader)
    Thread.currentThread().contextClassLoader = appClassLoader
    val clazz = appClassLoader.loadClass("org.springframework.boot.SpringApplication")
    val method = clazz.getMethod("run", Class::class.java, Array<String>::class.java)
    method.invoke(null, KuKuBotApplication::class.java, args)
}

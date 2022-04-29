package me.kuku.yuq.utils

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringUtils: ApplicationContextAware {

    companion object {

        lateinit var applicationContext: ApplicationContext

        inline fun <reified T: Any> getBean(name: String): T {
            return applicationContext.getBean(name) as T
        }

        inline fun <reified T: Any> getBean() = applicationContext.getBean(T::class.java)

    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        Companion.applicationContext = applicationContext
    }
}
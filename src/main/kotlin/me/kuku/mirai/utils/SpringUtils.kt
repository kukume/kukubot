package me.kuku.mirai.utils

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy(false)
class SpringUtils: ApplicationContextAware {

    companion object {

        lateinit var applicationContext: ApplicationContext

        inline fun <reified T: Any> getBean(name: String): T {
            return applicationContext.getBean(name) as T
        }

        inline fun <reified T: Any> getBean() = applicationContext.getBean(T::class.java)

    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        SpringUtils.applicationContext = applicationContext
    }
}


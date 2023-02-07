package me.kuku.mirai.config

import kotlinx.coroutines.runBlocking
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
import org.springframework.scheduling.config.TaskManagementConfigUtils
import org.springframework.scheduling.support.ScheduledMethodRunnable
import org.springframework.util.Assert
import org.springframework.util.ReflectionUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.UndeclaredThrowableException
import kotlin.coroutines.Continuation
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
class ScheduledSuspendConfig {

    @Bean(name = [TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME])
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun scheduledAnnotationBeanPostProcessor(): ScheduledAnnotationBeanPostProcessor {
        return ScheduledSuspendAnnotationBeanPostProcessor()
    }

}


class ScheduledSuspendAnnotationBeanPostProcessor: ScheduledAnnotationBeanPostProcessor() {

    override fun createRunnable(target: Any, method: Method): Runnable {
        val parameters = method.parameters
        Assert.isTrue(
            parameters.isEmpty() || ((parameters.size == 1) && (parameters[0].type == Continuation::class.java)),
            "Only no-arg methods may be annotated with @Scheduled");
        val invocableMethod = AopUtils.selectInvocableMethod(method, target.javaClass)
        return ScheduledSuspendMethodRunnable(target, invocableMethod)
    }

}

class ScheduledSuspendMethodRunnable(target: Any, method: Method): ScheduledMethodRunnable(target, method) {
    override fun run() {
        try {
            ReflectionUtils.makeAccessible(method)
            try {
                runBlocking {
                    method.kotlinFunction?.callSuspend(target)
                }
            } catch (ignore: InterruptedException) {}
        } catch (ex: InvocationTargetException) {
            ReflectionUtils.rethrowRuntimeException(ex.targetException)
        } catch (ex: IllegalAccessException) {
            throw UndeclaredThrowableException(ex)
        }
    }
}

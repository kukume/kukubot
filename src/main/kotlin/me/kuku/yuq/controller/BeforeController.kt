@file:Suppress("UNUSED_PARAMETER")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Catch
import com.IceCreamQAQ.Yu.annotation.Global
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Friend
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.entity.MessageAt
import com.icecreamqaq.yuq.error.WaitNextMessageTimeoutException
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.mif
import me.kuku.utils.JobManager
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.config.VerificationFailureException
import me.kuku.yuq.entity.*
import org.springframework.stereotype.Component
import java.io.PrintWriter
import java.io.StringWriter

@PrivateController
@GroupController
@Component
class BeforeController (
    private val qqService: QqService,
    private val groupService: GroupService,
    private val messageService: MessageService,
    private val privateMessageService: PrivateMessageService,
    private val exceptionLogService: ExceptionLogService
){

    @Before(weight = -1)
    @Global
    fun before(session: ContextSession, qq: Long, group: Long?) {
        val qqEntity = qqService.findByQq(qq)
        session["qqEntity"] = qqEntity!!
        if (group != null) {
            val groupEntity = groupService.findByGroup(group)
            session["groupEntity"] = groupEntity!!
        }
    }

    @After
    @Global
    fun after(context: BotActionContext, qq: Long) {
        context.reMessage?.let { message ->
            val source = context.source
            if (source is Member || source is Group) {
                if (message.at == null) {
                    message.at = MessageAt(qq, true)
                }
            }
        }
    }

    @Catch(error = WaitNextMessageTimeoutException::class)
    @Global
    fun waitError(exception: WaitNextMessageTimeoutException, qq: Long) {
        throw mif.at(qq).plus("等待您的消息超时，上下文已结束").toThrowable()
    }

    @Catch(error = VerificationFailureException::class)
    @Global
    fun ss(exception: VerificationFailureException, qq: Long) {
        throw mif.at(qq).plus(exception.message).toThrowable()
    }

    @Catch(error = Exception::class)
    @Global
    fun ss(exception: Exception, message: Message, context: BotActionContext, qq: Long, group: Long?) {
        if (exception is WaitNextMessageTimeoutException || exception is VerificationFailureException) return
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val exceptionStackTrace = sw.toString()
        val url = kotlin.runCatching {
            val jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/paste",
                mapOf("poster" to "kuku", "syntax" to "java", "content" to exceptionStackTrace)
            )
            jsonObject.getJSONObject("data").getString("url")
        }.getOrDefault("Ubuntu paste url 生成失败")
        val source = context.source
        source.sendMessage(mif.at(qq).plus("程序出现异常了，异常信息为：$url"))
        JobManager.now {
            OkHttpUtils.postJson("https://api.kukuqaq.com/botException", mapOf("message" to message.toCodeString(), "stackTrace" to exceptionStackTrace,
                "url" to url, "qq" to qq.toString()))
        }
        val messageId = message.source?.id ?: 0
        if (source is Friend || source is Member) {
            val messageEntity = privateMessageService.findByMessageIdAndQq(messageId, qq) ?: return
            val exceptionLogEntity = ExceptionLogEntity()
            exceptionLogEntity.privateMessageEntity = messageEntity
            exceptionLogEntity.stackTrace = exceptionStackTrace
            exceptionLogEntity.url = url
            exceptionLogService.save(exceptionLogEntity)
        } else if (source is Group) {
            val messageEntity =  messageService.findByMessageIdAndGroup(messageId, group!!) ?: return
            val exceptionLogEntity = ExceptionLogEntity()
            exceptionLogEntity.messageEntity = messageEntity
            exceptionLogEntity.stackTrace = exceptionStackTrace
            exceptionLogEntity.url = url
            exceptionLogService.save(exceptionLogEntity)
        }
    }

}

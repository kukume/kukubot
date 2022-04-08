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
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mif
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.*
import org.springframework.transaction.support.TransactionTemplate
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject

@PrivateController
@GroupController
class BeforeController @Inject constructor(
    private val qqService: QqService,
    private val messageService: MessageService,
    private val privateMessageService: PrivateMessageService,
    private val exceptionLogService: ExceptionLogService,
    private val transactionTemplate: TransactionTemplate
){

    @Before(weight = -1)
    @Global
    fun before(session: ContextSession, qq: Long, group: Long?) {
        transactionTemplate.execute {
            val qqEntity = qqService.findByQq(qq)
            session["qqEntity"] = qqEntity!!
            if (group != null) {
                val groupEntity = qqEntity.groups.first { it.group == group }
                session["groupEntity"] = groupEntity
            }
        }
    }

    @After
    @Global
    fun after(message: Message, context: BotActionContext, qq: Long) {
        val source = context.source
        if (source is Member || source is Group) {
            if (message.at == null) {
                message.at = MessageAt(qq, true)
            }
        }
    }

    @Catch(error = Exception::class)
    @Global
    fun ss(exception: Exception, message: Message, context: BotActionContext, qq: Long, group: Long?) {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        val exceptionStackTrace = sw.toString()
        val url = kotlin.runCatching {
            val jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/tool/paste",
                mapOf("poster" to "kuku", "syntax" to "java", "content" to exceptionStackTrace)
            )
            jsonObject.getJSONObject("data").getString("url")
        }.getOrDefault("Ubuntu paste url 生成失败")
        val source = context.source
        source.sendMessage(mif.at(qq).plus("程序出现异常了，异常信息为：$url，请反馈给开发者（不是IoException）"))
        val messageId = message.source?.id ?: 0
        if (source is Friend) {
            val messageEntity = privateMessageService.findByMessageIdAndQq(messageId, qq) ?: return
            val exceptionLogEntity = ExceptionLogEntity()
            exceptionLogEntity.privateMessageEntity = messageEntity
            exceptionLogEntity.stackTrace = exceptionStackTrace
            exceptionLogEntity.url = url
            exceptionLogService.save(exceptionLogEntity)
        } else if (source is Group || source is Member) {
            val messageEntity =  messageService.findByMessageIdAndGroup(messageId, group!!) ?: return
            val exceptionLogEntity = ExceptionLogEntity()
            exceptionLogEntity.messageEntity = messageEntity
            exceptionLogEntity.stackTrace = exceptionStackTrace
            exceptionLogEntity.url = url
            exceptionLogService.save(exceptionLogEntity)
        }
    }

}
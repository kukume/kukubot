package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Catch
import com.IceCreamQAQ.Yu.annotation.Global
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.IceCreamQAQ.Yu.entity.DoNone
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import kotlinx.coroutines.TimeoutCancellationException
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.exception.BaiduException
import me.kuku.yuq.logic.ToolLogic
import net.mamoe.mirai.contact.BotIsBeingMutedException
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import javax.inject.Inject
import javax.inject.Named

@GroupController
@PrivateController
class ActionBeforeController {
    @Inject
    private lateinit var qqService: QqService
    @Inject
    private lateinit var groupService: GroupService

    @Global
    @Before(weight = -1)
    fun nextParams(context: BotActionContext, qq: Long, group: Long?) {
        val qqEntity = qqService.findByQq(qq)
        context["qqEntity"] = qqEntity!!
        if (group != null) {
            val groupEntity = groupService.findByGroup(group)
            context["groupEntity"] = groupEntity!!
        }
    }
}

@GroupController
class GroupBeforeController {
    @Inject
    private lateinit var groupService: GroupService
    @Inject
    @field:Named("CommandCountOnTime")
    private lateinit var eh: EhcacheHelp<Int>

    @Global
    @Before
    fun before(message: Message, group: Long, qq: Long) {
        val list = message.toPath()
        if (list.isEmpty()) return
        val command = list[0]
        if (command == "指令限制" || command == "加指令限制") return
        val groupEntity = groupService.findByGroup(group) ?: return
        var maxCount = groupEntity.maxCommandCountOnTime
        if (maxCount == null) maxCount = -1
        if (maxCount < 0) return
        val key = "qq$qq$command"
        var num = eh[key] ?: 0
        if (num >= maxCount) throw DoNone()
        eh[key] = ++num
    }

    @Global
    @Before
    fun before(message: Message, group: Long) {
        val list = message.toPath()
        if (list.isEmpty()) return
        val jsonObject = groupService.findByGroup(group)?.commandLimitJson ?: return
        val command = list[0]
        if (jsonObject.containsKey(command)) {
            val maxCount = jsonObject.getInteger(command)
            if (maxCount < 0) return
            val key = "group$group$command"
            var num = eh[key] ?: 0
            if (num >= maxCount) throw DoNone()
            eh[key] = ++num
        }
    }
}

@PrivateController
@GroupController
class ExBeforeController{

    @Inject
    private lateinit var toolLogic: ToolLogic

    @Global
    @Catch(error = Exception::class)
    fun recording(exception: Exception, actionContext: BotActionContext, qq: Long) {
        if (exception is TimeoutCancellationException
            || exception is IOException || exception is BotIsBeingMutedException
        ) {
            return
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        try {
            val url: String = toolLogic.pasteUbuntu("exception", "java", sw.toString())
            actionContext.source.sendMessage(mif.at(qq).plus("程序出现异常了，异常如下：$url"))
        } catch (ignore: Exception) {
        }
    }

    @Global
    @Catch(error = IOException::class)
    fun interIO(iOException: IOException?, actionContext: BotActionContext, qq: Long) {
        actionContext.source.sendMessage(mif.at(qq).plus("出现io异常了，请重试！！"))
    }
}

@PrivateController
@GroupController
class ss(){
    @Action("test")
    fun test(){
        throw BaiduException("异常异常！")
    }

}
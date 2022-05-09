@file:Suppress("DuplicatedCode")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.*
import com.IceCreamQAQ.Yu.event.events.AppStartEvent
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import me.kuku.utils.DateTimeFormatterUtils
import me.kuku.utils.JobManager
import me.kuku.utils.OkHttpKtUtils
import me.kuku.yuq.config.VerificationFailureException
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.JobEntity
import me.kuku.yuq.entity.JobService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.utils.YuqUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.math.abs

@Component
@GroupController
@PrivateController
@EventListener
class JobController(
    private val jobService: JobService,
    @Value("\${yuq.art.master}") private val master: Long
) {

    private val jobMap = mutableMapOf<Int, Job>()

    @Before(only = ["add", "delete"])
    fun before(context: BotActionContext, groupEntity: GroupEntity?, qq: Member?) {
        if (context.source is Group) {
            if (!valid(groupEntity!!, qq!!, master)) throw mif.at(qq).plus("权限不足，无法执行").toThrowable()
        }
    }

    @Action("添加任务")
    fun add(session: ContextSession, qqEntity: QqEntity, context: BotActionContext, groupEntity: GroupEntity?, qq: Long): String {
        context.source.sendMessage(mif.at(qq).plus("""
            请发送cron，格式
            HH:mm:ss 或者 HH:mm  - 表示每天的这个时间段执行
            yyyy-MM-dd HH:mm:ss - 表示该时间执行一次，之后删除此任务
            1m - 单位 s,m,h,d,M,y 表示每间隔时间内执行一次：秒,分,时,天,月,年
        """.trimIndent()))
        val str = session.waitNextMessage().firstString()
        val interval = parse(str)
        context.source.sendMessage(mif.at(qq).plus("请发送需要发送的消息（RainCode码）"))
        val ss = session.waitNextMessage().toCodeString()
        val jobEntity = JobEntity().also {
            it.cron = str
            it.msg = ss
            if (groupEntity == null) it.qqEntity = qqEntity
            else it.groupEntity = groupEntity
        }
        jobService.save(jobEntity)
        execute(interval, jobEntity)
        return "添加定时任务成功"
    }

    @Action("查询任务")
    fun query(qqEntity: QqEntity, groupEntity: GroupEntity?): String {
        val ss = if (groupEntity == null) jobService.findByQqEntity(qqEntity)
        else jobService.findByGroupEntity(groupEntity)
        val sb = StringBuilder()
        ss.forEach {
            sb.appendLine("${it.id} -- ${it.cron} -- ${it.msg}")
        }
        return sb.removeSuffix("\n").toString()
    }

    @Action("删除任务")
    @Synonym(["开始任务", "停止任务"])
    fun delete(session: ContextSession, context: BotActionContext, qqEntity: QqEntity, groupEntity: GroupEntity?, qq: Long, @PathVar(0) type: String): String {
        context.source.sendMessage(mif.at(qq).plus("请发送任务的id"))
        val id = session.waitNextMessage().firstString().toIntOrNull() ?: return "您发送的不为数字"
        val ss = jobService.findByIdOrderById(id) ?: return "该任务不存在"
        val b = if (groupEntity == null) ss.qqEntity!!.qq == qqEntity.qq
        else ss.groupEntity!!.group == groupEntity.group
        if (!b) return "您发送的任务id不为本群或者为您的任务"
        return when (type) {
            "删除任务" -> {
                jobMap.remove(id)?.cancel()
                jobService.delete(ss)
                "删除并停止任务成功"
            }
            "开始任务" -> {
                val have = jobMap.containsKey(id)
                if (have) {
                    "该任务已开始"
                } else {
                    execute(ss)
                    "开始任务成功"
                }
            }
            "停止任务" -> {
                val have = jobMap.containsKey(id)
                if (!have) {
                    "该任务并未开始"
                } else {
                    jobMap.remove(id)?.cancel()
                    ss.begin = false
                    jobService.save(ss)
                    "停止任务成功"
                }
            }
            else -> "gg"
        }
    }


    @Event
    fun ss(e: AppStartEvent) {
        val list = jobService.findAll().filter { it.begin }
        for (jobEntity in list) {
            execute(jobEntity)
        }
    }

    private fun parse(str: String): Interval {
        var newStr = str
        if (newStr.split(":").size == 2 && newStr.length == 5) newStr = "$newStr:00"
        return try {
            val now = LocalDate.now()
            val ss = DateTimeFormatterUtils.parseToLocalTime(newStr, "HH:mm:ss")
            val nowTime = LocalTime.now()
            val sss = if (nowTime.isAfter(ss)) now.plusDays(1) else now
            val ssss = ss.atDate(sss).toInstant(ZoneOffset.of("+8")).toEpochMilli()
            Interval(abs(System.currentTimeMillis() - ssss), 1000 * 60 * 60 * 24)
        } catch (e: Exception) {
            try {
                val localDateTime = DateTimeFormatterUtils.parseToLocalDateTime(newStr, "yyyy-MM-dd HH:mm:ss")
                val ss = localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli()
                val re = System.currentTimeMillis() - ss
                if (re < 0) throw VerificationFailureException("发送的执行时间小于当前时间")
                Interval(re, -1)
            } catch (e: Exception) {
                val last = newStr.last()
                if (last in listOf('s', 'm', 'h', 'd', 'M', 'y')) {
                    val sss = newStr.removeSuffix(last.toString()).toLongOrNull() ?: throw VerificationFailureException("cron格式不正确")
                    val ss = when (last) {
                        's' -> sss * 1000
                        'm' -> sss * 1000 * 60
                        'h' -> sss * 1000 * 60 * 60
                        'd' -> sss * 1000 * 60 * 60 * 24
                        'M' -> sss * 1000 * 60 * 60 * 24 * 30
                        'y' -> sss * 1000 * 60 * 60 * 24 * 30 * 12
                        else -> sss
                    }
                    Interval(0, ss)
                } else throw VerificationFailureException("cron格式不正确")
            }
        }
    }

    private fun execute(jobEntity: JobEntity) {
        val cron = jobEntity.cron
        val interval = parse(cron)
        execute(interval, jobEntity)
    }

    private fun execute(interval: Interval, jobEntity: JobEntity) {
        val job = JobManager.now {
            delay(interval.first)
            sendMessage(jobEntity)
            if (interval.every < 0) {
                withContext(Dispatchers.IO) {
                    jobService.delete(jobEntity)
                }
                return@now
            }
            jobEntity.begin = true
            jobService.save(jobEntity)
            while (true) {
                delay(interval.every)
                sendMessage(jobEntity)
            }
        }
        jobMap[jobEntity.id!!] = job
    }

    private suspend fun sendMessage(jobEntity: JobEntity) {
        jobEntity.qqEntity?.let {
            YuqUtils.sendMessage(it, msg(jobEntity.msg))
        }
        jobEntity.groupEntity?.let {
            YuqUtils.sendMessage(it, msg(jobEntity.msg))
        }
    }

    private suspend fun msg(str: String): Message {
        return if (str.startsWith("http")) OkHttpKtUtils.getStr(str).toMessageByRainCode()
        else str.toMessageByRainCode()
    }

}

data class Interval(val first: Long, val every: Long)
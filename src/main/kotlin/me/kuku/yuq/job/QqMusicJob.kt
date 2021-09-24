package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.IceCreamQAQ.Yu.job.JobManager
import me.kuku.pojo.Result
import me.kuku.yuq.entity.QqMusicEntity
import me.kuku.yuq.entity.QqMusicService
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.logic.QqMusicLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject
import kotlin.Exception

@JobCenter
class QqMusicJob {

    @Inject
    private lateinit var qqMusicService: QqMusicService
    @Inject
    private lateinit var qqMusicLogic: QqMusicLogic
    @Inject
    private lateinit var toolLogic: ToolLogic
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String
    @Inject
    private lateinit var qqService: QqService
    @Inject
    private lateinit var jobManager: JobManager

    @Cron("At::d::05:15:41")
    fun musicSign() {
        val list: List<QqMusicEntity> = qqMusicService.findAll()
        for (qqMusicEntity in list) {
            try {
                val result: Result<Void> = qqMusicLogic.sign(qqMusicEntity)
                val qqEntity = qqMusicEntity.qqEntity
                if (result.isFailure) {
                    BotUtils.sendMessage(qqEntity, "您的QQ音乐的cookie已失效，如需自动签到，请重新绑定！")
                    continue
                }
                qqMusicLogic.musicianSign(qqMusicEntity)
                qqMusicLogic.daySign(qqMusicEntity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Cron("1h")
    fun qqMusicComment() {
        val list = qqMusicService.findByAutoComment(true)
        for (qqMusicEntity in list) {
            qqMusicLogic.replyComment(qqMusicEntity, toolLogic.hiToKoTo()["text"] ?: "好听好听！")
            val replyResult = qqMusicLogic.randomReplyComment(qqMusicEntity, toolLogic.hiToKoTo()["text"] ?: "好听好听！")
            if (replyResult.isFailure) {
                BotUtils.sendMessage(qqMusicEntity.qqEntity, "您的qq音乐随机回复评论失败，错误信息为：" + replyResult.message)
                break
            }
        }
    }

    @Cron("1h")
    fun musicianTask() {
        val list = qqMusicService.findByAutoPublishView(true)
        for (qqMusicEntity in list) {
            try {
                val result = qqMusicLogic.publishNews(qqMusicEntity, toolLogic.hiToKoTo()["text"] ?: "每小时发个动态？")
                if (result.isFailure) {
                    val qqEntity = qqMusicEntity.qqEntity
                    BotUtils.sendMessage(qqEntity, "您的QQ音乐发布新动态失败，失败原因为" + result.message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Cron("1h")
    fun musicShare(){
        val list = qqMusicService.findAll()
        for (qqMusicEntity in list) {
            try {
                qqMusicLogic.shareMusic(qqMusicEntity)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    @Cron("1h")
    fun musicianUpdate() {
        val list = qqMusicService.findAll()
        for (qqMusicEntity in list) {
            val result = qqMusicLogic.sign(qqMusicEntity)
            if (result.isFailure) {
                val qqEntity = qqMusicEntity.qqEntity!!
                if (qqEntity.password?.isNotEmpty() == true) {
                    val loginResult = qqMusicLogic.loginByPassword(qqEntity.qq, qqEntity.password!!)
                    if (loginResult.isFailure) {
                        BotUtils.sendMessage(qqEntity, "您的QQ音乐的cookie已失效，如需自动签到，请重新绑定！")
                    } else {
                        val (_, _, cookie, qqMusicKey) = loginResult.data
                        qqMusicEntity.cookie = cookie
                        qqMusicEntity.qqMusicKey = qqMusicKey
                        qqMusicService.save(qqMusicEntity)
                        qqMusicLogic.sign(qqMusicEntity)
                    }
                } else {
                    BotUtils.sendMessage(qqEntity, "您的QQ音乐的cookie已失效，如需自动签到，请重新绑定！")
                    qqMusicService.delete(qqMusicEntity)
                }
            }
        }
    }

    @Cron("At::d::00:00:00")
    fun musicianConvert() {
        val qqEntity = qqService.findByQq(master.toLong()) ?: return
        val qqMusicEntity = qqMusicService.findByQqEntity(qqEntity)?: return
        for (i in 0..10) {
            jobManager.registerTimer({
                try {
                    val res = qqMusicLogic.convertGreenDiamond(qqMusicEntity)
                    val message = res.message
                    BotUtils.sendMessage(qqMusicEntity.qqEntity, "qq音乐人自动领取绿钻通知：\n$message")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 0)
        }
    }

}
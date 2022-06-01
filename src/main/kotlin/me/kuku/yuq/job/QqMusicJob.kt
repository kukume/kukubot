package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.entity.QqMusicService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.logic.QqMusicLogic
import me.kuku.yuq.utils.hitokoto
import org.springframework.stereotype.Component

@JobCenter
@Component
class QqMusicJob(
    private val qqMusicService: QqMusicService,
    private val qqMusicLogic: QqMusicLogic
) {

    @Cron("06:15")
    suspend fun musicSign() {
        val list = qqMusicService.findAll()
        for (qqMusicEntity in list) {
            kotlin.runCatching {
                qqMusicLogic.sign(qqMusicEntity)
                qqMusicLogic.musicianSign(qqMusicEntity)
                qqMusicLogic.daySign(qqMusicEntity)
                qqMusicLogic.shareMusic(qqMusicEntity)
            }
        }
    }


    @Cron("1h")
    suspend fun qqMusicComment() {
        val list = qqMusicService.findAll().filter { it.config.comment == Status.ON }
        for (qqMusicEntity in list) {
            kotlin.runCatching {
                qqMusicLogic.replyComment(qqMusicEntity, hitokoto())
                qqMusicLogic.randomReplyComment(qqMusicEntity, hitokoto())
            }
        }
    }

    @Cron("1h")
    suspend fun musicianTask() {
        val list = qqMusicService.findAll().filter { it.config.view == Status.ON }
        for (qqMusicEntity in list) {
            kotlin.runCatching {
                qqMusicLogic.publishNews(qqMusicEntity, hitokoto())
            }
        }
    }

    @Cron("00:01")
    suspend fun updateCookie() {
        val list = qqMusicService.findAll().filter { it.config.password.isNotEmpty() }
        for (qqMusicEntity in list) {
            val loginResult = qqMusicLogic.loginByPassword(qqMusicEntity.qqEntity!!.qq, qqMusicEntity.config.password)
            if (loginResult.success()) {
                val newEntity = loginResult.data()
                qqMusicEntity.cookie = newEntity.cookie
                qqMusicService.save(qqMusicEntity)
            }
        }
    }
}
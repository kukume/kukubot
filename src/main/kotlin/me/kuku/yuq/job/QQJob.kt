@file:Suppress("unused")

package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.logic.QQZoneLogic
import me.kuku.yuq.service.QQJobService
import me.kuku.yuq.service.QQLoginService
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import javax.inject.Inject

@JobCenter
class QQJob {

    @Inject
    private lateinit var qqLoginService: QQLoginService
    @Inject
    private lateinit var qqLogic: QQLogic

    @Cron("30m")
    fun checkAndUpdate(){
        val list = qqLoginService.findByActivity()
        list.forEach {
            val qqEntity = it
            val result = qqLogic.qqSign(qqEntity)
            if ("失败" in result){
                if (qqEntity.password == "") {
                    qqEntity.status = false
                    qqLoginService.save(qqEntity)
                    yuq.groups[qqEntity.group]?.get(qqEntity.qq)?.sendMessage("您的QQ登录已失效！！".toMessage())
                }else{
                    val commonResult = QQPasswordLoginUtils.login(qq = qqEntity.qq.toString(), password = qqEntity.password)
                    if (commonResult.code == 200){
                        QQUtils.saveOrUpdate(qqLoginService, commonResult.t!!, qqEntity.qq, qqEntity.password)
                    } else if (!arrayOf(400, 1, -1, 7).contains(commonResult.code)) {
                        qqEntity.status = false
                        qqLoginService.save(qqEntity)
                        val msg = "您的QQ自动更新失败，${commonResult.msg}"
                        if (qqEntity.group == 0L)
                            yuq.friends[qqEntity.qq]?.sendMessage(msg.toMessage())
                        else
                            yuq.groups[qqEntity.group]?.get(qqEntity.qq)?.sendMessage(msg.toMessage())
                    }
                }
            }
        }
    }

    @Cron("At::d::06:00")
    fun sVipMorn() {
        val list = qqLoginService.findByActivity()
        list.forEach {
            try {
                qqLogic.sVipMornClock(it)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}

@JobCenter
class QQSwitchJob {
    @Inject
    private lateinit var qqJobService: QQJobService
    @Inject
    private lateinit var qqLoginService: QQLoginService
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var qqZoneLogic: QQZoneLogic

    @Cron("4h")
    fun qqSign(){
        val list = qqJobService.findByType("autoSign")
        list.forEach {
            try {
                val qqEntity = qqLoginService.findByQQ(it.qq) ?: return@forEach
                val str = qqLogic.qqSign(qqEntity)
                if (!str.contains("更新QQ")){
                    qqLogic.anotherSign(qqEntity)
                    qqLogic.vipSign(qqEntity)
                    qqLogic.phoneGameSign(qqEntity)
                    qqLogic.yellowSign(qqEntity)
                    qqLogic.qqVideoSign1(qqEntity)
                    qqLogic.qqVideoSign2(qqEntity)
                    qqLogic.bigVipSign(qqEntity)
                    qqLogic.qqMusicSign(qqEntity)
                    qqLogic.gameSign(qqEntity)
                    qqLogic.qPetSign(qqEntity)
                    qqLogic.tribeSign(qqEntity)
                    qqLogic.motionSign(qqEntity)
                    qqLogic.blueSign(qqEntity)
                    qqLogic.sVipMornSign(qqEntity)
                    qqLogic.weiYunSign(qqEntity)
                    qqLogic.weiShiSign(qqEntity)
                    qqLogic.growthLike(qqEntity)
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    @Cron("1m")
    fun mz(){
        val list = qqJobService.findByType("mz")
        list.forEach {
            val qqJobEntity = it
            if (qqJobEntity.dataJsonObject.getBoolean("status")){
                val qqEntity = qqLoginService.findByQQ(qqJobEntity.qq)?: return@forEach
                if (qqEntity.status){
                    qqZoneLogic.friendTalk(qqEntity)?.forEach { map ->
                        if (map["like"] == null || map["like"] != "1") {
                            qqZoneLogic.likeTalk(qqEntity, map)
                        }
                    }
                }
            }
        }
    }

    @Cron("30s")
    fun bubble(){
        val list = qqJobService.findByType("bubble")
        list.forEach {
            if (it.dataJsonObject.getBoolean("status")){
                val qqEntity = qqLoginService.findByQQ(it.qq)?: return@forEach
                if (qqEntity.status){
                    qqLogic.diyBubble(qqEntity, it.dataJsonObject.getString("text"), null)
                }
            }
        }
    }

}
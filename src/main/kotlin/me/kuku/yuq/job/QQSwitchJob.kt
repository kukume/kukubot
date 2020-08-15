package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.logic.QQZoneLogic
import me.kuku.yuq.service.QQJobService
import me.kuku.yuq.service.QQService
import javax.inject.Inject

@JobCenter
class QQSwitchJob {
    @Inject
    private lateinit var qqJobService: QQJobService
    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var qqLogic: QQLogic
    @Inject
    private lateinit var qqZoneLogic: QQZoneLogic

    @Cron("4h")
    fun qqSign(){
        val list = qqJobService.findByType("autoSign")
        list.forEach {
            try {
                val qqEntity = qqService.findByQQ(it.qq) ?: return@forEach
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
            if (qqJobEntity.getJsonObject().getBoolean("status")){
                val qqEntity = qqService.findByQQ(qqJobEntity.qq)?: return@forEach
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
            if (it.getJsonObject().getBoolean("status")){
                val qqEntity = qqService.findByQQ(it.qq)?: return@forEach
                if (qqEntity.status){
                    qqLogic.diyBubble(qqEntity, it.getJsonObject().getString("text"), null)
                }
            }
        }
    }

}
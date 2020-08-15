package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.logic.QQLogic
import me.kuku.yuq.service.QQService
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import javax.inject.Inject

@JobCenter
class QQJob {

    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var qqLogic: QQLogic

    @Cron("30m")
    fun checkAndUpdate(){
        val list = qqService.findByActivity()
        list.forEach {
            val qqEntity = it
            val result = qqLogic.qqSign(qqEntity)
            if ("失败" in result){
                if (qqEntity.password == "") {
                    qqEntity.status = false
                    qqService.save(qqEntity)
                    yuq.groups[qqEntity.qqGroup]?.get(qqEntity.qq)?.sendMessage("您的QQ登录已失效！！".toMessage())
                }else{
                    val commonResult = QQPasswordLoginUtils.login(qq = qqEntity.qq.toString(), password = qqEntity.password)
                    if (commonResult.code == 200){
                        QQUtils.saveOrUpdate(qqService, commonResult.t, qqEntity.qq, qqEntity.password)
                    } else if (!arrayOf(400, 1, -1, 7).contains(commonResult.code)) {
                        qqEntity.status = false
                        qqService.save(qqEntity)
                        val msg = "您的QQ自动更新失败，${commonResult.msg}"
                        if (qqEntity.qqGroup == 0L)
                            yuq.friends[qqEntity.qq]?.sendMessage(msg.toMessage())
                        else
                            yuq.groups[qqEntity.qqGroup]?.get(qqEntity.qq)?.sendMessage(msg.toMessage())
                    }
                }
            }
        }
    }

    @Cron("At::d::07")
    fun sVipMorn() {
        val list = qqService.findByActivity()
        list.forEach {
            try {
                qqLogic.sVipMornClock(it)
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}
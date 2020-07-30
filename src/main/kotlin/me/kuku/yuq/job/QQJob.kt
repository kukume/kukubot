package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.message.MessageFactory
import com.icecreamqaq.yuq.message.MessageItemFactory
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
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var mf: MessageFactory
    @Inject
    private lateinit var mif: MessageItemFactory

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
                    yuq.sendMessage(mf.newTemp(qqEntity.qqGroup, qqEntity.qq).plus(mif.at(qqEntity.qq)).plus("您的QQ已失效。"))
                }else{
                    val commonResult = QQPasswordLoginUtils.login(qq = qqEntity.qq.toString(), password = qqEntity.password)
                    if (commonResult.code == 200){
                        QQUtils.saveOrUpdate(qqService, commonResult.t, qqEntity.qq, qqEntity.password)
                    } else if (!arrayOf(400, 1, -1, 7).contains(commonResult.code)) {
                        qqEntity.status = false
                        qqService.save(qqEntity)
                        val msg = "您的QQ自动更新失败，${commonResult.msg}"
                        if (qqEntity.qqGroup == 0L)
                            yuq.sendMessage(mf.newPrivate(qqEntity.qq).plus(msg))
                        else
                            yuq.sendMessage(mf.newTemp(qqEntity.qqGroup, qqEntity.qq).plus(msg))
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
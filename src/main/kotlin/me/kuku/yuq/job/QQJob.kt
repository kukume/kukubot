package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.icecreamqaq.yuq.YuQ
import com.icecreamqaq.yuq.message.MessageFactory
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.service.impl.QQServiceImpl
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import javax.inject.Inject

@JobCenter
class QQJob {

    @Inject
    private lateinit var qqDao: QQDao
    @Inject
    private lateinit var qqService: QQServiceImpl
    @Inject
    private lateinit var yuq: YuQ
    @Inject
    private lateinit var mf: MessageFactory
    @Inject
    private lateinit var mif: MessageItemFactory

    @Cron("1h")
    fun checkAndUpdate(){
        val list = qqDao.findAll()
        list?.forEach {
            val qqEntity = it as QQEntity
            if (qqEntity.status){
                val result = qqService.qqSign(qqEntity)
                if ("失败" in result) qqEntity.status = false
                if (qqEntity.password == "") {
                    qqDao.singleSave(qqEntity)
                    yuq.sendMessage(mf.newTemp(qqEntity.qqGroup, qqEntity.qq).plus(mif.at(qqEntity.qq)).plus("您的QQ已失效。"))
                }
            }
            if (!qqEntity.status && qqEntity.password != ""){
                val commonResult = QQPasswordLoginUtils.login(qq = qqEntity.qq.toString(), password = qqEntity.password)
                if (commonResult.code == 200){
                    QQUtils.saveOrUpdate(qqDao, commonResult.t, qqEntity.qq, qqEntity.password)
                } else yuq.sendMessage(mf.newPrivate(qqEntity.qq).plus("您的QQ自动更新失败，${commonResult.msg}"))
            }
        }
    }

    @Cron("6h")
    fun qqSign(){
        val list = qqDao.findAll()
        list?.forEach {
            val qqEntity = it as QQEntity
            if (qqEntity.status) {
                qqService.qqSign(qqEntity)
                qqService.anotherSign(qqEntity)
                qqService.groupLottery(qqEntity, 1132123L)
                qqService.vipSign(qqEntity)
                qqService.phoneGameSign(qqEntity)
                qqService.yellowSign(qqEntity)
                qqService.qqVideoSign1(qqEntity)
                qqService.qqVideoSign2(qqEntity)
                qqService.bigVipSign(qqEntity)
                qqService.qqMusicSign(qqEntity)
                qqService.gameSign(qqEntity)
                qqService.qPetSign(qqEntity)
                qqService.tribeSign(qqEntity)
                qqService.motionSign(qqEntity)
                qqService.blueSign(qqEntity)
                qqService.sVipMornSign(qqEntity)
                qqService.weiYunSign(qqEntity)
            } else{
                qqEntity.status = false
                qqDao.singleSave(qqEntity)
            }
        }
    }

    @Cron("At::h::7")
    fun sVipMorn() {
        val list = qqDao.findAll()
        list?.forEach {
            val qqEntity = it as QQEntity
            if (qqEntity.status) qqService.sVipMornClock(qqEntity)
            else {
                qqEntity.status = false
                qqDao.singleSave(qqEntity)
            }
        }
    }

}
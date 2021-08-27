@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.BaiduEntity
import me.kuku.yuq.entity.BaiduService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.BaiduLogic
import javax.inject.Inject

@GroupController
class BaiduController {

    @Inject
    private lateinit var baiduLogic: BaiduLogic
    @Inject
    private lateinit var baiduService: BaiduService

    @Before(except = ["getQrcode"])
    fun before(qqEntity: QqEntity): BaiduEntity{
        return baiduService.findByQqEntity(qqEntity)
            ?: throw mif.at(qqEntity.qq).plus("您没有绑定百度账号，请发送<百度qq二维码>进行绑定！").toThrowable()

    }

    @Action("百度qq二维码")
    fun getQrcode(group: Group, qq: Long, qqEntity: QqEntity){
        val qrcode = baiduLogic.getQrcode()
        group.sendMessage(mif.at(qq).plus(mif.imageByByteArray(qrcode.bytes))
            .plus(mif.text("请使用qq扫码登录百度！请确保您的百度账号已绑定qq！")))
        GlobalScope.launch {
            val msg: Message
            while (true){
                delay(3000)
                val result = baiduLogic.checkQrcode(qrcode)
                if (result.code == 200){
                    val newBaiduEntity = result.data
                    val baiduEntity = baiduService.findByQqEntity(qqEntity) ?: BaiduEntity(qqEntity = qqEntity)
                    baiduEntity.bdUss = newBaiduEntity.bdUss
                    baiduEntity.sToken = newBaiduEntity.sToken
                    baiduService.save(baiduEntity)
                    msg = mif.at(qq).plus("绑定百度账号成功！")
                    break
                }else if (result.code == 500){
                    msg = mif.at(qq).plus(result.message)
                    break
                }
            }
            group.sendMessage(msg)
        }
    }

    @Action("游帮帮签到")
    @QMsg(at = true)
    fun ybbSign(baiduEntity: BaiduEntity): String = baiduLogic.ybbSign(baiduEntity).message

    @Action("贴吧签到")
    @QMsg(at = true)
    fun tieBaSign(baiduEntity: BaiduEntity): String = baiduLogic.tieBaSign(baiduEntity).message
}

@PrivateController
class BaiduPrivateController{

    @Inject
    private lateinit var baiduService: BaiduService

    @Action("百度 {cookie}")
    fun bindCookie(cookie: String, qqEntity: QqEntity): String{
        val bdUss = OkHttpUtils.getCookie(cookie, "BDUSS")
            ?: return "没有从cookie中获取到BDUSS"
        val sToken = OkHttpUtils.getCookie(cookie, "STOKEN")
            ?: return "没有从cookie中获取到STOKEN"
        val baiduEntity = baiduService.findByQqEntity(qqEntity)
            ?: BaiduEntity(qqEntity = qqEntity).apply {
                this.bdUss = bdUss
                this.sToken = sToken
            }
        baiduService.save(baiduEntity)
        return "绑定百度信息成功！"
    }

}
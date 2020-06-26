package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.icecreamqaq.yuq.annotation.*
import com.icecreamqaq.yuq.message.At
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.service.impl.QQGroupServiceImpl
import me.kuku.yuq.service.impl.QQServiceImpl
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import me.kuku.yuq.utils.image
import javax.inject.Inject
import kotlin.random.Random

@GroupController
@ContextController
class BotController {
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String
    @Config("YuQ.Mirai.user.pwd")
    private lateinit var password: String
    @Inject
    private lateinit var qqService: QQServiceImpl
    @Inject
    private lateinit var qqGroupService: QQGroupServiceImpl
    @Inject
    private lateinit var mif: MessageItemFactory

    var qqEntity: QQEntity? = null

    @Before
    fun before(){
        if (qqEntity == null || !qqEntity!!.status) {
            val commonResult = QQPasswordLoginUtils.login(qq = this.qq, password = this.password)
            qqEntity = QQUtils.convertQQEntity(commonResult.t)
            qqEntity!!.qq = this.qq.toLong()
            qqEntity!!.password = this.password
        }
    }

    @Action("赞我")
    fun like(qq : Long): String {
        val str = qqService.like(qqEntity!!, qq)
        if ("失败" in str) qqEntity?.status = false
        return str
    }

    @Action("公告")
    @NextContext("nextPublishNotice")
    fun publishNotice() = "请输入需要发送的公告内容！"

    @Action("nextPublishNotice")
    fun nextPublishNotice(message: Message, group: Long): String{
        val content = message.body[0].toPath()
        val str =  qqService.publishNotice(qqEntity!!, group, content)
        if ("失败" in str) qqEntity?.status = false
        return str
    }

    @Action("群链接")
    fun groupLink(group: Long): String{
        val str =  qqService.getGroupLink(qqEntity!!, group);
        if ("失败" in str) qqEntity?.status = false
        return str
    }

    @Action("群活跃")
    fun groupActive(group: Long): String{
        val str =  qqService.groupActive(qqEntity!!, group, 0)
        if ("失败" in str) qqEntity?.status = false
        return str
    }

    @Action("拉")
    fun addMember(@PathVar(1) addQQ: String?, group: Long): String{
        return if (addQQ != null) qqGroupService.addGroupMember(qqEntity!!, addQQ.toLong(), group)
        else "缺少参数：拉人的QQ号"
    }

    @Action("踢")
    fun deleteMember(message: Message, group: Long): String{
        val at = message.body[1]
        return if (at is At){
            qqGroupService.deleteGroupMember(qqEntity!!, at.user, group, false)
        }else "缺少参数，踢人的QQ！"
    }

    @Action("龙王")
    fun dragonKing(group: Long): Message{
        val commonResult = qqGroupService.groupDragonKing(qqEntity!!, group)
        return if (commonResult.code == 200){
            val urlArr = arrayOf(
                    "https://u.iheit.com/kuku/61f600415023300.jpg",
                    "https://u.iheit.com/kuku/449ab0415103619.jpg",
                    "https://u.iheit.com/kuku/51fe90415023311.jpg",
                    "https://u.iheit.com/kuku/1d12a0415023726.jpg",
                    "https://u.iheit.com/kuku/b04b30415023728.jpg",
                    "https://u.iheit.com/kuku/d21200415023730.jpg",
                    "https://u.iheit.com/kuku/55f0e0415023731.jpg",
                    "https://u.iheit.com/kuku/634cc0415023733.jpg",
                    "https://u.iheit.com/kuku/c044b0415023734.jpg",
                    "https://u.iheit.com/kuku/ce2270415023735.jpg",
                    "https://u.iheit.com/kuku/6e4b20415023737.jpg",
                    "https://u.iheit.com/kuku/5f7d70415023738.jpg",
                    "https://u.iheit.com/kuku/98d640415023739.jpg",
                    "https://u.iheit.com/kuku/26a1a0415023741.jpg",
                    "https://u.iheit.com/kuku/e84c90415023744.jpg",
                    "https://u.iheit.com/kuku/ddc810415023745.jpg",
                    "https://u.iheit.com/kuku/23ee20415023747.jpg",
                    "https://u.iheit.com/kuku/8c4a80415023748.jpg",
                    "https://u.iheit.com/kuku/bdb970415023750.jpg"
            )
            val url = urlArr[Random.nextInt(urlArr.size)]
            val response = OkHttpClientUtils.get(url)
            val bytes = OkHttpClientUtils.getBytes(response)
            val map = commonResult.t
            mif.at(map.getValue("qq")).plus(mif.image(bytes)).plus("龙王（已蝉联${map.getValue("day")}天）快喷水！")
        }else mif.text(commonResult.msg).toMessage()
    }

    @Action("群文件")
    fun groupFile(@PathVar(1) fileName: String?, group: Long) = qqService.groupFileUrl(qqEntity!!, group, fileName)
}
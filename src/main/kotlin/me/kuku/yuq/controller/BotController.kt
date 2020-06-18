package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.icecreamqaq.yuq.annotation.ContextController
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.NextContext
import com.icecreamqaq.yuq.message.Message
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.service.impl.QQServiceImpl
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import javax.inject.Inject

@GroupController
@ContextController
class BotController {
    @Config("YuQ.Mirai.user.qq")
    private lateinit var qq: String
    @Config("YuQ.Mirai.user.pwd")
    private lateinit var password: String
    @Inject
    private lateinit var qqService: QQServiceImpl

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
}
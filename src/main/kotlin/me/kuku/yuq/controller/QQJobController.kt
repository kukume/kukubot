package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.entity.QQJobEntity
import me.kuku.yuq.service.QQJobService
import me.kuku.yuq.service.QQService
import javax.inject.Inject

@GroupController
class QQJobController {
    @Inject
    private lateinit var qqJobService: QQJobService
    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var mif: MessageItemFactory

    @Before
    fun check(qq: Long, context: BotActionContext){
        val qqEntity = qqService.findByQQ(qq)
        if (qqEntity == null)
            throw mif.text("没有绑定QQ！！").toMessage()
        else{
            context.session["qqEntity"] = qqEntity
        }
    }

    @QMsg(at = true)
    @Action("秒赞 {status}")
    fun mzOpen(qq: Long, status: Boolean): String{
        var qqJobEntity = qqJobService.findByQQAndType(qq, "mz")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            jsonObject["status"] = false
            qqJobEntity = QQJobEntity(null, qq, "mz", jsonObject.toString())
        }
        val jsonObject = qqJobEntity.getJsonObject()
        jsonObject["status"] = status
        qqJobEntity.data = jsonObject.toString()
        qqJobService.save(qqJobEntity)
        return "秒赞已${if (status) "开启" else "关闭"}"
    }

    @QMsg(at = true)
    @Action("百变气泡/{text}")
    fun varietyBubble(qq: Long, text: String): String{
        var qqJobEntity = qqJobService.findByQQAndType(qq, "bubble")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            jsonObject["status"] = false
            jsonObject["text"] = ""
            qqJobEntity = QQJobEntity(null, qq, "bubble", jsonObject.toString())
        }
        val jsonObject = qqJobEntity.getJsonObject()
        val msg = when (text){
            "开" -> {
                jsonObject["status"] = true
                "百变气泡已开启！！"
            }
            "关" -> {
                jsonObject["status"] = false
                "百变气泡已关闭！！"
            }
            else -> {
                jsonObject["status"] = true
                jsonObject["text"] = text
                "百变气泡已开启！！气泡diy文字为：$text"
            }
        }
        qqJobEntity.data = jsonObject.toString()
        qqJobService.save(qqJobEntity)
        return msg
    }

    @QMsg(at = true)
    @Action("自动签到 {status}")
    fun autoSign(qq: Long, status: Boolean): String{
        var qqJobEntity = qqJobService.findByQQAndType(qq, "autoSign")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            jsonObject["status"] = false
            qqJobEntity = QQJobEntity(null, qq, "autoSign", jsonObject.toString())
        }
        val jsonObject = qqJobEntity.getJsonObject()
        jsonObject["status"] = status
        qqJobEntity.data = jsonObject.toString()
        qqJobService.save(qqJobEntity)
        return "qq启动签到${if (status) "开启" else "关闭"}成功"
    }
}
package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.MessageItemFactory
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.QQJobEntity
import me.kuku.yuq.logic.QQZoneLogic
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
    private lateinit var qqZoneLogic: QQZoneLogic
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

    @Action("群签到 {status}")
    fun groupSignOpen(status: Boolean, qq: Long, qqEntity: QQEntity): String{
        var qqJobEntity = qqJobService.findByQQAndType(qq, "groupSign")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            val commonResult = qqZoneLogic.queryGroup(qqEntity)
            if (commonResult.code != 200) return "获取群列表失败，请更新QQ！！！"
            val list = commonResult.t
            val jsonArray = JSONArray()
            list.forEach { jsonArray.add(it.getValue("group")) }
            jsonObject["status"] = false
            jsonObject["num"] = 0
            jsonObject["exclude"] = JSONArray()
            jsonObject["group"] = jsonArray
            qqJobEntity = QQJobEntity(null, qq, "groupSign", jsonObject.toString())
        }
        val jsonObject = qqJobEntity.getJsonObject()
        jsonObject["status"] = status
        qqJobEntity.data = jsonObject.toString()
        qqJobService.save(qqJobEntity)
        return "群签到定时任务已${if (status) "开启" else "关闭"}"
    }

    @Action("\\删?群排除\\")
    fun groupSignExclude(message: Message, qq: Long, @PathVar(0) text: String): String{
        val list = message.toPath().toMutableList()
        if (list.size == 1) return "缺少参数，群号"
        list.removeAt(0)
        val qqJobEntity = qqJobService.findByQQAndType(qq, "groupSign")
        return if (qqJobEntity != null){
            val jsonObject = qqJobEntity.getJsonObject()
            val jsonArray = jsonObject.getJSONArray("exclude")
            val msg: String
            if (text[0] == '群') {
                msg = "群签到定时任务添加排除名单成功！！"
                jsonArray.addAll(list)
            }else{
                msg = "群签到定时任务删除排除名单成功！！"
                list.forEach { jsonArray.remove(it) }
            }
            jsonObject["exclude"] = jsonArray
            qqJobEntity.data = jsonObject.toString()
            qqJobService.save(qqJobEntity)
            msg
        }else "修改失败"
    }

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
}
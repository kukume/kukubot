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
import me.kuku.yuq.service.DaoService
import me.kuku.yuq.service.QQZoneService
import javax.inject.Inject

@GroupController
class QQJobController {
    @Inject
    private lateinit var daoService: DaoService
    @Inject
    private lateinit var qqZoneService: QQZoneService
    @Inject
    private lateinit var mif: MessageItemFactory

    @Before
    fun check(qq: Long, context: BotActionContext){
        val qqEntity = daoService.findQQByQQ(qq)
        if (qqEntity == null)
            throw mif.text("没有绑定QQ！！").toMessage()
        else{
            context.session["qqEntity"] = qqEntity
        }
    }

    @Action("\\群签到(开|关)\\")
    fun groupSignOpen(@PathVar(0) text: String, qq: Long, qqEntity: QQEntity): String{
        var qqJobEntity = daoService.findQQJobByQQAndType(qq, "groupSign")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            val commonResult = qqZoneService.queryGroup(qqEntity)
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
        jsonObject["status"] = text[3] == '开'
        qqJobEntity.data = jsonObject.toString()
        daoService.saveOrUpdateQQJob(qqJobEntity)
        return "群签到定时任务已${if (jsonObject.getBoolean("status")) "开启" else "关闭"}"
    }

    @Action("\\删?群排除\\")
    fun groupSignExclude(message: Message, qq: Long, @PathVar(0) text: String): String{
        val list = message.toPath().toMutableList()
        if (list.size == 1) return "缺少参数，群号"
        list.removeAt(0)
        val qqJobEntity = daoService.findQQJobByQQAndType(qq, "groupSign")
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
            daoService.saveOrUpdateQQJob(qqJobEntity)
            msg
        }else "修改失败"
    }

    @Action("\\秒赞(开|关)\\")
    fun mzOpen(qq: Long, @PathVar(0) text: String): String{
        var qqJobEntity = daoService.findQQJobByQQAndType(qq, "mz")
        if (qqJobEntity == null){
            val jsonObject = JSONObject()
            jsonObject["status"] = false
            qqJobEntity = QQJobEntity(null, qq, "mz", jsonObject.toString())
        }
        val jsonObject = qqJobEntity.getJsonObject()
        jsonObject["status"] = text[2] == '开'
        qqJobEntity.data = jsonObject.toString()
        daoService.saveOrUpdateQQJob(qqJobEntity)
        return "秒赞已${if (jsonObject.getBoolean("status")) "开启" else "关闭"}"
    }
}
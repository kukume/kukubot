@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.service.BiliBiliService
import me.kuku.yuq.utils.removeSuffixLine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

@GroupController
class BiliBiliLoginController: QQController() {
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic
    @Inject
    private lateinit var biliBiliService: BiliBiliService
    @Inject
    private lateinit var toolLogic: ToolLogic

    @Action("bllogin qr")
    @QMsg(at = true)
    fun biliBiliLoginByQr(group: Group, qq: Long): Message{
        val url = biliBiliLogic.loginByQr1()
        val qrUrl = toolLogic.creatQr(url)
        thread {
            while (true){
                TimeUnit.SECONDS.sleep(3)
                val commonResult = biliBiliLogic.loginByQr2(url)
                when (commonResult.code){
                    500 -> {
                        group.sendMessage(mif.at(qq).plus(commonResult.msg))
                        return@thread
                    }
                    200 -> {
                        val biliBiliEntity = biliBiliService.findByQQ(qq) ?: BiliBiliEntity(null, qq, group.id)
                        val newBiliBiliEntity = commonResult.t!!
                        biliBiliEntity.cookie = newBiliBiliEntity.cookie
                        biliBiliEntity.token = newBiliBiliEntity.token
                        biliBiliEntity.userId = newBiliBiliEntity.userId
                        biliBiliService.save(biliBiliEntity)
                        group.sendMessage(mif.at(qq).plus("绑定或者更新哔哩哔哩成功！！"))
                        return@thread
                    }
                }
            }
        }
        return mif.text("请使用哔哩哔哩APP扫码登录：").plus(mif.imageByInputStream(qrUrl.inputStream()))
    }

}

@GroupController
class BiliBiliController: QQController() {
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic
    @Inject
    private lateinit var biliBiliService: BiliBiliService

    @Before
    fun before(qq: Long, actionContext: BotActionContext){
        val biliBiliEntity = biliBiliService.findByQQ(qq)
        if (biliBiliEntity == null || biliBiliEntity.cookie == "")
            throw mif.at(qq).plus("您还没有绑定哔哩哔哩账号，无法继续！！！，如需绑定请发送bilibililoginbyqr").toThrowable()
        actionContext["biliBiliEntity"] = biliBiliEntity
    }

    @Action("哔哩哔哩/add/{type}/{username}")
    fun biliBiliAdd(biliBiliEntity: BiliBiliEntity, type: String, username: String, session: ContextSession, qq: Long){
        val commonResult = biliBiliLogic.getIdByName(username)
        val list = commonResult.t
        if (list == null){
            reply(mif.at(qq).plus("该用户不存在！！"))
            return
        }
        val biliBiliPojo = list[0]
        val name = biliBiliPojo.name
        val id = biliBiliPojo.userId.toLong()
        val jsonObject = JSONObject()
        jsonObject["id"] = id
        jsonObject["name"] = name
        when (type){
            "开播提醒" -> {
                biliBiliEntity.liveList = biliBiliEntity.liveJsonArray.fluentAdd(jsonObject).toString()
                reply(mif.at(qq).plus("添加${name}的开播提醒成功！！"))
            }
            "赞" -> {
                biliBiliEntity.likeList = biliBiliEntity.likeJsonArray.fluentAdd(jsonObject).toString()
                reply(mif.at(qq).plus("添加${name}的自动赞成功！！"))
            }
            "评论" -> {
                reply(mif.at(qq).plus("请输入需要评论的内容"))
                val message = session.waitNextMessage()
                val content = message.firstString()
                jsonObject["content"] = content
                biliBiliEntity.commentList =
                        biliBiliEntity.commentJsonArray.fluentAdd(content).toString()
                reply(mif.at(qq).plus("添加${name}的自动评论成功！！"))
            }
            "转发" -> {
                reply(mif.at(qq).plus("请输入需要转发的内容"))
                val message = session.waitNextMessage()
                val content = message.firstString()
                jsonObject["content"] = content
                biliBiliEntity.forwardList =
                        biliBiliEntity.forwardJsonArray.fluentAdd(jsonObject).toString()
                reply(mif.at(qq).plus("添加${name}的自动转发成功！！"))
            }
            "投硬币" -> {
                biliBiliEntity.tossCoinList = biliBiliEntity.tossCoinJsonArray.fluentAdd(jsonObject).toString()
                reply(mif.at(qq).plus("添加${name}的自动投硬币成功！！"))
            }
            "收藏" -> {
                reply(mif.at(qq).plus("请输入需要自动收藏的收藏夹的名称"))
                val message = session.waitNextMessage()
                val content = message.firstString()
                jsonObject["content"] = content
                biliBiliEntity.forwardList =
                        biliBiliEntity.forwardJsonArray.fluentAdd(jsonObject).toString()
                reply(mif.at(qq).plus("添加${name}的自动收藏成功！！"))
            }
            else -> return
        }
        biliBiliService.save(biliBiliEntity)
        return
    }

    private fun del(jsonArray: JSONArray, username: String): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in jsonArray.indices){
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getString("name") == username) list.add(jsonObject)
        }
        return list
    }

    @Action("哔哩哔哩/del/{type}/{username}")
    fun biliBiliDel(biliBiliEntity: BiliBiliEntity, type: String, username: String): String?{
        when (type){
            "开播提醒" -> {
                val jsonArray = biliBiliEntity.liveJsonArray
                biliBiliEntity.liveList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            "赞" -> {
                val jsonArray = biliBiliEntity.likeJsonArray
                biliBiliEntity.likeList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            "评论" -> {
                val jsonArray = biliBiliEntity.commentJsonArray
                biliBiliEntity.commentList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            "转发" -> {
                val jsonArray = biliBiliEntity.forwardJsonArray
                biliBiliEntity.forwardList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            "投硬币" -> {
                val jsonArray = biliBiliEntity.tossCoinJsonArray
                biliBiliEntity.tossCoinList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            "收藏" -> {
                val jsonArray = biliBiliEntity.favoritesJsonArray
                biliBiliEntity.favoritesList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            else -> return null
        }
        return "删除成功！！"
    }

    @Action("哔哩哔哩/list/{type}")
    @QMsg(at = true, atNewLine = true)
    fun biliBiliList(biliBiliEntity: BiliBiliEntity, type: String): String?{
        val sb = StringBuilder()
        val jsonArray: JSONArray
        when (type){
            "开播提醒" -> {
                sb.appendLine("您的开播提醒列表如下：")
                jsonArray = biliBiliEntity.liveJsonArray
            }
            "赞" -> {
                sb.appendLine("您的自动赞列表如下：")
                jsonArray = biliBiliEntity.likeJsonArray
            }
            "评论" -> {
                sb.appendLine("您的自动评论列表如下：")
                jsonArray = biliBiliEntity.commentJsonArray
            }
            "转发" -> {
                sb.appendLine("您的自动转发列表如下：")
                jsonArray = biliBiliEntity.forwardJsonArray
            }
            "投硬币" -> {
                sb.appendLine("您的自动投硬币列表如下：")
                jsonArray = biliBiliEntity.tossCoinJsonArray
            }
            "收藏" -> {
                sb.appendLine("您的自动收藏列表如下：")
                jsonArray = biliBiliEntity.favoritesJsonArray
            }
            else -> return null
        }
        for (i in jsonArray.indices){
            val jsonObject = jsonArray.getJSONObject(i)
            sb.appendLine("${jsonObject.getString("id")}-${jsonObject.getString("name")}-${jsonObject.getString("content")}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("哔哩哔哩关注监控 {status}")
    @QMsg(at = true)
    fun biliBiliMonitor(biliBiliEntity: BiliBiliEntity, status: Boolean): String{
        biliBiliEntity.monitor = status
        biliBiliService.save(biliBiliEntity)
        return "哔哩哔哩我的关注监控已${if (status) "开启" else "关闭"}"
    }

    @Action("哔哩哔哩任务 {status}")
    @QMsg(at = true)
    fun biliBiliTask(biliBiliEntity: BiliBiliEntity, status: Boolean): String{
        biliBiliEntity.task = status
        biliBiliService.save(biliBiliEntity)
        return "哔哩哔哩定时任务已${if (status) "开启" else "关闭"}"
    }

    @Action("哔哩哔哩赞 {username}")
    fun biliBiliAllLike(biliBiliEntity: BiliBiliEntity, username: String, group: Group, qq: Long): String{
        val idCommonResult = biliBiliLogic.getIdByName(username)
        val biliBiliPojo = idCommonResult.t?.get(0) ?: return "没有找到该用户！！"
        val list = biliBiliLogic.getAllDynamicById(biliBiliPojo.userId)
        thread {
            list.forEach {
                TimeUnit.SECONDS.sleep(5)
                biliBiliLogic.like(biliBiliEntity, it.id, true)
            }
            group.sendMessage(mif.at(qq).plus("赞该用户动态成功！！"))
        }
        return "哔哩哔哩赞正在运行中！！"
    }
}
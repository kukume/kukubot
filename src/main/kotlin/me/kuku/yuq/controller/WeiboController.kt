@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.service.WeiboService
import me.kuku.yuq.utils.removeSuffixLine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

@GroupController
class WeiboNotController: QQController(){
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var weiboService: WeiboService

    @Action("wblogin")
    fun wbLoginByQr(qq: Long, group: Group){
        val map = weiboLogic.loginByQr1()
        reply(mif.at(qq).plus("请使用微博APP扫码登录").plus(mif.imageByUrl("https:" + map.getValue("url"))))
        thread {
            val id = map.getValue("id")
            while (true){
                TimeUnit.SECONDS.sleep(3)
                val commonResult = weiboLogic.loginByQr2(id)
                if (commonResult.code == 200){
                    val newWeiboEntity = commonResult.t!!
                    val weiboEntity = weiboService.findByQQ(qq) ?: WeiboEntity(null, qq, group.id)
                    weiboEntity.pcCookie = newWeiboEntity.pcCookie
                    weiboEntity.group = group.id
                    weiboEntity.mobileCookie = newWeiboEntity.mobileCookie
                    weiboService.save(weiboEntity)
                    group.sendMessage(mif.at(qq).plus("绑定微博信息成功！！"))
                    return@thread
                }else if (commonResult.code == 500){
                    group.sendMessage(mif.at(qq).plus(commonResult.msg))
                    return@thread
                }
            }
        }
    }

    @Action("热搜")
    @QMsg(at = true, atNewLine = true)
    fun hotSearch(): String{
        val list = weiboLogic.hotSearch()
        val sb = StringBuilder()
        for (str in list){
            sb.appendLine(str)
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("wbinfo {username}")
    @QMsg(at = true, atNewLine = true)
    fun weiboInfo(username: String): String{
        val idResult = weiboLogic.getIdByName(username)
        val idList = idResult.t ?: return idResult.msg
        return weiboLogic.getUserInfo(idList[0].userId)
    }
}

@GroupController
class WeiboController: QQController() {
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var weiboService: WeiboService
    @Inject
    private lateinit var toolLogic: ToolLogic

    @Before
    fun before(qq: Long): WeiboEntity{
        return weiboService.findByQQ(qq) ?:
            throw mif.at(qq).plus("您还未绑定微博，请先私聊机器人发送（wb 账号 密码）进行绑定").toThrowable()
    }

    @Action("微博关注监控 {status}")
    @QMsg(at = true)
    fun weiboMyMonitor(status: Boolean, weiboEntity: WeiboEntity): String{
        weiboEntity.monitor = status
        weiboService.save(weiboEntity)
        return if (status) "我的关注微博监控开启成功！！" else "我的关注微博监控关闭成功！！"
    }

    @Action("微博/add/{type}/{username}")
    @QMsg(at = true)
    fun weiboAdd(weiboEntity: WeiboEntity, type: String, username: String, session: ContextSession, qq: Long): String?{
        val commonResult = weiboLogic.getIdByName(username)
        val weiboPojo = commonResult.t?.get(0) ?: return commonResult.msg
        val id = weiboPojo.userId
        val name = weiboPojo.name
        val jsonObject = JSONObject()
        jsonObject["id"] = id
        jsonObject["name"] = name
        when (type){
            "赞" ->
                weiboEntity.likeList = weiboEntity.likeJsonArray.fluentAdd(jsonObject).toString()
            "评论" -> {
                reply(mif.at(qq).plus("请输入需要评论的内容！！"))
                val content = session.waitNextMessage().firstString()
                jsonObject["content"] = content
                weiboEntity.commentList = weiboEntity.commentJsonArray.fluentAdd(jsonObject).toString()
            }
            "转发" -> {
                reply(mif.at(qq).plus("请输入需要转发的内容！！"))
                val content = session.waitNextMessage().firstString()
                jsonObject["content"] = content
                weiboEntity.forwardList = weiboEntity.forwardJsonArray.fluentAdd(jsonObject).toString()
            }
            else -> return null
        }
        weiboService.save(weiboEntity)
        return "添加微博用户<${weiboPojo.name}>的自动${type}成功！！"
    }

    private fun del(jsonArray: JSONArray, username: String): List<JSONObject> {
        val list = mutableListOf<JSONObject>()
        for (i in jsonArray.indices){
            val jsonObject = jsonArray.getJSONObject(i)
            if (jsonObject.getString("name") == username) list.add(jsonObject)
        }
        return list
    }

    @Action("微博/del/{type}/{username}")
    @QMsg(at = true)
    fun weiboDel(weiboEntity: WeiboEntity, type: String, username: String): String?{
        when (type){
            "赞" -> {
                val jsonArray = weiboEntity.likeJsonArray
                weiboEntity.likeList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            "评论" -> {
                val jsonArray = weiboEntity.commentJsonArray
                weiboEntity.commentList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            "转发" -> {
                val jsonArray = weiboEntity.forwardJsonArray
                weiboEntity.forwardList = jsonArray.fluentRemoveAll(del(jsonArray, username)).toString()
            }
            else -> return null
        }
        weiboService.save(weiboEntity)
        return "删除成功！！"
    }

    @Action("微博/list/{type}")
    @QMsg(at = true, atNewLine = true)
    fun weiboList(weiboEntity: WeiboEntity, type: String): String?{
        val sb = StringBuilder()
        val jsonArray = when (type){
            "赞" -> {
                sb.appendLine("您的微博自动赞列表如下：")
                weiboEntity.likeJsonArray
            }
            "评论" -> {
                sb.appendLine("您的微博自动评论列表如下：")
                weiboEntity.commentJsonArray
            }
            "转发" -> {
                sb.appendLine("您的微博自动转发列表如下：")
                weiboEntity.forwardJsonArray
            }
            else -> return null
        }
        for (obj in jsonArray){
            val jsonObject = obj as JSONObject
            sb.appendLine("${jsonObject.getString("id")}-${jsonObject.getString("name")}-${jsonObject.getString("content")}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("微博超话签到")
    @QMsg(at = true)
    fun weiboSuperTalkSign(weiboEntity: WeiboEntity): Any {
        val commonResult = weiboLogic.weiboSuperTalkSign(weiboEntity)
        return when (commonResult.code){
            200,500 -> commonResult.msg
            else -> {
                val url = commonResult.t
                val qrUrl = toolLogic.creatQr(url!!)
                mif.text("请打开微博客户端扫描该二维码并验证该验证码，然后重新发送该指令：").plus(mif.imageByInputStream(qrUrl.inputStream()))
            }
        }
    }
}
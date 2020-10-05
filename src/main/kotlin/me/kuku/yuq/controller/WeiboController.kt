package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.WeiboPojo
import me.kuku.yuq.service.BiliBiliService
import me.kuku.yuq.service.WeiboService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.removeSuffixLine
import java.lang.ClassCastException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

@GroupController
class WeiboController: QQController() {
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var weiboService: WeiboService
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic
    @Inject
    private lateinit var biliBiliService: BiliBiliService

    private val hotMap: MutableMap<Long, List<String>> = mutableMapOf()

    @Before
    fun before(qq: Long, message: Message): WeiboEntity?{
        val str = message.toPath()[0]
        val whiteList = arrayOf("热搜", "hot", "wb", "微博监控", "wbinfo", "wblogin")
        return if (!whiteList.contains(str)){
            return weiboService.findByQQ(qq) ?: throw mif.at(qq).plus("您还未绑定微博，请先私聊机器人发送（wb 账号 密码）进行绑定")
        }else null
    }

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
                    weiboEntity.group_ = group.id
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
    fun hotSearch(group: Long): String{
        val list = weiboLogic.hotSearch()
        hotMap[group] = list
        val sb = StringBuilder()
        for (str in list){
            sb.appendln(str)
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("hot {num}")
    @QMsg(at = true, atNewLine = true)
    fun hot(num: Int, group: Long): String{
        val list = if (hotMap.containsKey(group)) hotMap[group]!!
        else {
            val list = weiboLogic.hotSearch()
            hotMap[group] = list
            list
        }
        var name: String? = null
        for (str in list){
            if (str.startsWith(num.toString())){
                name = str.split("、")[1]
                break
            }
        }
        if (name == null) return "没有找到该热搜！！"
        val commonResult = weiboLogic.weiboTopic(name)
        val weiboPojo = commonResult.t?.get(0) ?: return "没有找到该话题！！"
        return weiboLogic.convertStr(weiboPojo)
    }

    @Action("wb {username}")
    @QMsg(at = true, atNewLine = true)
    fun searchWeibo(username: String, @PathVar(2) numStr: String?): String{
        val commonResult = this.queryWeibo(username, numStr)
        val weiboPojo = commonResult.t ?: return commonResult.msg
        return weiboLogic.convertStr(weiboPojo)
    }

    @Action("wbmymonitor {status}")
    @Synonym(["微博关注监控 {status}"])
    @QMsg(at = true)
    fun weiboMyMonitor(status: Boolean, weiboEntity: WeiboEntity): String{
        weiboEntity.monitor = status
        weiboService.save(weiboEntity)
        return if (status) "我的关注微博监控开启成功！！" else "我的关注微博监控关闭成功！！"
    }

    @Action("wbmy")
    @QMsg(at = true, atNewLine = true)
    fun myFriendWeibo(@PathVar(value = 1, type = PathVar.Type.Integer) num: Int?, weiboEntity: WeiboEntity): String{
        val commonResult = weiboLogic.getFriendWeibo(weiboEntity)
        return if (commonResult.code == 200) {
            val list = commonResult.t!!
            val weiboPojo = this.getWeiboPojo(list, num)
            weiboLogic.convertStr(weiboPojo)
        }else commonResult.msg
    }

    @Action("mywb")
    @QMsg(at = true, atNewLine = true)
    fun myWeibo(@PathVar(value = 1, type = PathVar.Type.Integer) num: Int?, weiboEntity: WeiboEntity): String{
        val commonResult = weiboLogic.getMyWeibo(weiboEntity)
        val list = commonResult.t ?: return commonResult.msg
        val weiboPojo = this.getWeiboPojo(list, num)
        return weiboLogic.convertStr(weiboPojo)
    }

    @Action("wbinfo {username}")
    @QMsg(at = true, atNewLine = true)
    fun weiboInfo(username: String): String{
        val idResult = weiboLogic.getIdByName(username)
        val idList = idResult.t ?: return idResult.msg
        return weiboLogic.getUserInfo(idList[0].userId)
    }

    @Action("wbtopic {keyword}")
    @Synonym(["微博话题 {keyword}"])
    @QMsg(at = true, atNewLine = true)
    fun weiboTopic(keyword: String, @PathVar(value = 2, type = PathVar.Type.Integer) num: Int?): String{
        val commonResult = weiboLogic.weiboTopic(keyword)
        if (commonResult.code != 200) return commonResult.msg
        val list = commonResult.t!!
        val weiboPojo = this.getWeiboPojo(list, num)
        return weiboLogic.convertStr(weiboPojo)
    }

    @Action("微博评论 {username} {content}")
    @QMsg(at = true)
    fun comment(@PathVar(3) numStr: String?, username: String, content: String, weiboEntity: WeiboEntity): String{
        val commonResult = this.queryWeibo(username, numStr)
        val weiboPojo = commonResult.t ?: return commonResult.msg
        return weiboLogic.comment(weiboEntity, weiboPojo.id, content)
    }

    @Action("微博转发 {username} {content}")
    @QMsg(at = true)
    fun forward(username: String, content: String, weiboEntity: WeiboEntity, @PathVar(3) numStr: String?, message: Message): String{
        val commonResult = this.queryWeibo(username, numStr)
        val weiboPojo = commonResult.t ?: return commonResult.msg
        val bodyList = message.body
        var url: String? = null
        if (bodyList.size > 1){
            url = try {
                val image = bodyList[1] as Image
                image.url
            }catch (e: ClassCastException){
                null
            }
        }
        return weiboLogic.forward(weiboEntity, weiboPojo.id, content, url)
    }

    @Action("微博发布 {content}")
    @QMsg(at = true)
    fun publishWeibo(weiboEntity: WeiboEntity, content: String, message: Message): String{
        val url = mutableListOf<String>()
        val bodyList = message.body
        bodyList.forEach {
            if (it is Image){
                url.add(it.url)
            }
        }
        return weiboLogic.publishWeibo(weiboEntity, content, url)
    }

    @Action("微博自动赞 {username}")
    @QMsg(at = true)
    fun weiboAutoLike(weiboEntity: WeiboEntity, username: String): String{
        val commonResult = this.searchToJsonObject(username)
        val jsonObject = commonResult.t ?: return commonResult.msg
        val likeJsonArray = weiboEntity.getLikeJsonArray()
        likeJsonArray.add(jsonObject)
        weiboEntity.likeList = likeJsonArray.toString()
        weiboService.save(weiboEntity)
        return "添加微博用户[${jsonObject["name"]}]的自动赞成功"
    }

    @Action("查微博自动赞")
    @QMsg(at = true)
    fun queryAutoLike(weiboEntity: WeiboEntity): String{
        val sb = StringBuilder().appendln("您的微博自动赞列表如下：")
        val likeJsonArray = weiboEntity.getLikeJsonArray()
        for (i in likeJsonArray.indices){
            val jsonObject = likeJsonArray.getJSONObject(i)
            sb.appendln("${jsonObject.getString("name")}-${jsonObject.getString("id")}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("删微博自动赞 {username}")
    @QMsg(at = true)
    fun delAutoLike(weiboEntity: WeiboEntity, username: String): String{
        val likeJsonArray = weiboEntity.getLikeJsonArray()
        BotUtils.delAuto(likeJsonArray, username)
        weiboEntity.likeList = likeJsonArray.toString()
        weiboService.save(weiboEntity)
        return "删除该用户的微博自动赞成功！！"
    }

    @Action("微博自动评论 {username} {content}")
    @QMsg(at = true)
    fun weiboAutoComment(weiboEntity: WeiboEntity, username: String, content: String): String{
        val commonResult = this.searchToJsonObject(username)
        val jsonObject = commonResult.t ?: return commonResult.msg
        jsonObject["content"] = content
        val commentJsonArray = weiboEntity.getCommentJsonArray()
        commentJsonArray.add(jsonObject)
        weiboEntity.commentList = commentJsonArray.toString()
        weiboService.save(weiboEntity)
        return "添加微博用户[${jsonObject["name"]}]自动评论成功！！"
    }

    @Action("查微博自动评论")
    @QMsg(at = true)
    fun queryAutoComment(weiboEntity: WeiboEntity): String{
        val sb = StringBuilder().appendln("您的微博自动评论列表如下：")
        val commentJsonArray = weiboEntity.getCommentJsonArray()
        for (i in commentJsonArray.indices){
            val jsonObject = commentJsonArray.getJSONObject(i)
            sb.appendln("${jsonObject.getString("name")}-${jsonObject.getString("id")}-${jsonObject.getString("content")}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("删微博自动评论 {username}")
    @QMsg(at = true)
    fun delAutoComment(weiboEntity: WeiboEntity, username: String): String{
        val commentJsonArray = weiboEntity.getCommentJsonArray()
        BotUtils.delAuto(commentJsonArray, username)
        weiboEntity.commentList = commentJsonArray.toString()
        weiboService.save(weiboEntity)
        return "删除该用户的微博自动评论成功！！"
    }

    @Action("微博自动转发 {username} {content}")
    @QMsg(at = true)
    fun weiboAutoForward(weiboEntity: WeiboEntity, username: String, content: String): String{
        val commonResult = this.searchToJsonObject(username)
        val jsonObject = commonResult.t ?: return commonResult.msg
        jsonObject["content"] = content
        val forwardJsonArray = weiboEntity.getForwardJsonArray()
        forwardJsonArray.add(jsonObject)
        weiboEntity.forwardList = forwardJsonArray.toString()
        weiboService.save(weiboEntity)
        return "添加微博用户的[${jsonObject["name"]}]自动转发成功！！"
    }

    @Action("查微博自动转发")
    @QMsg(at = true)
    fun queryAutoForward(weiboEntity: WeiboEntity): String{
        val sb = StringBuilder().appendln("您的微博自动转发列表如下：")
        val forwardJsonArray = weiboEntity.getForwardJsonArray()
        for (i in forwardJsonArray){
            val jsonObject =  i as JSONObject
            sb.appendln("${jsonObject.getString("name")}-${jsonObject.getString("id")}-${jsonObject.getString("content")}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("删微博自动转发 {username}")
    @QMsg(at = true)
    fun delAutoForward(weiboEntity: WeiboEntity, username: String): String{
        val forwardJsonArray = weiboEntity.getForwardJsonArray()
        BotUtils.delAuto(forwardJsonArray, username)
        weiboEntity.forwardList = forwardJsonArray.toString()
        weiboService.save(weiboEntity)
        return "删除该用户的微博自动转发成功！！"
    }

    @Action("微博超话签到")
    @QMsg(at = true)
    fun weiboSuperTalkSign(weiboEntity: WeiboEntity) =
        weiboLogic.weiboSuperTalkSign(weiboEntity)

    @QMsg(at = true)
    @Action("bilibililoginbyweibo")
    fun biliBiliLogin(weiboEntity: WeiboEntity, qq: Long, group: Long): String{
        val commonResult = biliBiliLogic.loginByWeibo(weiboEntity)
        val resultBiliBiliEntity = commonResult.t ?: return commonResult.msg
        val biliBiliEntity = biliBiliService.findByQQ(qq) ?: BiliBiliEntity(null, qq)
        biliBiliEntity.cookie = resultBiliBiliEntity.cookie
        biliBiliEntity.group_ = group
        biliBiliEntity.userId = resultBiliBiliEntity.userId
        biliBiliEntity.token = resultBiliBiliEntity.token
        biliBiliService.save(biliBiliEntity)
        return "绑定或者更新哔哩哔哩成功！！"
    }

    private fun getWeiboPojo(list: List<WeiboPojo>, num: Int?): WeiboPojo {
        return when {
            num == null -> list[0]
            num >= list.size -> list[0]
            num <= 0 -> list[0]
            else -> list[num - 1]
        }
    }

    private fun searchToJsonObject(username: String): CommonResult<JSONObject>{
        val commonResult = weiboLogic.getIdByName(username)
        val weiboPojo = commonResult.t?.get(0) ?: return CommonResult(500, commonResult.msg)
        val jsonObject = JSONObject()
        jsonObject["id"] = weiboPojo.userId
        jsonObject["name"] = weiboPojo.name
        return CommonResult(200, "", jsonObject)
    }

    private fun queryWeibo(username: String, numStr: String?): CommonResult<WeiboPojo> {
        val idResult = weiboLogic.getIdByName(username)
        val idList = idResult.t ?: return CommonResult(500, idResult.msg)
        val queryWeiboPojo = idList[0]
        val weiboResult = weiboLogic.getWeiboById(queryWeiboPojo.userId)
        val weiboList = weiboResult.t ?: return CommonResult(500, weiboResult.msg)
        if (weiboList.isEmpty()) return CommonResult(500, "没有查询到微博，可能还没有发布过微博或者连接异常！！")
        val num = try {
            numStr?.toInt()
        }catch (e: Exception){
            return CommonResult(500, "第二个参数应为整型！！")
        }
        val weiboPojo = this.getWeiboPojo(weiboList, num)
        return CommonResult(200, "", weiboPojo)
    }

}
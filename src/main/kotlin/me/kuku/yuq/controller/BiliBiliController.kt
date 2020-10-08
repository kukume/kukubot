package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.controller.QQController
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.toMessage
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.yuq.logic.BiliBiliLogic
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.pojo.BiliBiliPojo
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.service.BiliBiliService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.removeSuffixLine
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.thread

@GroupController
class BiliBiliController: QQController() {
    @Inject
    private lateinit var biliBiliLogic: BiliBiliLogic
    @Inject
    private lateinit var biliBiliService: BiliBiliService
    @Inject
    private lateinit var toolLogic: ToolLogic

    @Before
    fun before(qq: Long, message: Message, actionContext: BotActionContext){
        val whiteList = arrayOf("bilibili", "哔哩哔哩", "bilibililoginbyqr")
        val list = message.toPath()
        if (list.isNotEmpty() && whiteList.contains(list[0])){
            return
        }
        val biliBiliEntity = biliBiliService.findByQQ(qq)
        if (biliBiliEntity == null || biliBiliEntity.cookie == "") throw mif.at(qq).plus("您还没有绑定哔哩哔哩账号，无法继续！！！，如需绑定请发送[bilibililoginbyqq]或[bilibililoginbyweibo]或[]bilibililoginbyqr")
        actionContext["biliBiliEntity"] = biliBiliEntity
    }

    @Action("bilibililoginbyqr")
    @QMsg(at = true)
    fun biliBiliLoginByQr(group: Group, qq: Long): Message{
        val url = biliBiliLogic.loginByQr1()
        val qrUrl = toolLogic.creatQr(url)
        var count = 0
        thread {
            while (true){
                if (++count >= 20){
                    group.sendMessage(mif.at(qq).plus("您不能使用使用这个二维码登录哔哩哔哩了！！"))
                    return@thread
                }
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
        return mif.text("请使用哔哩哔哩APP扫码登录：").plus(mif.imageByUrl(qrUrl))
    }

    @Action("bilibili {username}")
    @Synonym(["哔哩哔哩 {username}"])
    @QMsg(at = true, atNewLine = true)
    fun searchDynamic(username: String, @PathVar(value = 2, type = PathVar.Type.Integer) num: Int?, qq: Long): Message{
        val commonResult = this.queryDynamic(username, num)
        val biliBiliPojo = commonResult.t ?: return mif.at(qq).plus(commonResult.msg)
        return biliBiliLogic.convertStr(biliBiliPojo).toMessage()
    }

    @Action("bilibilimy")
    @QMsg(at = true, atNewLine = true)
    fun searchMyFriendDynamic(biliBiliEntity: BiliBiliEntity, @PathVar(value = 1, type = PathVar.Type.Integer) num :Int?, qq: Long): Message{
        val commonResult = biliBiliLogic.getFriendDynamic(biliBiliEntity)
        val list = commonResult.t ?: return mif.at(qq).plus(commonResult.msg)
        if (list.isEmpty()) return mif.at(qq).plus("您的好友没有任何动态呢！！")
        val newNum = this.parseNum(list, num)
        return biliBiliLogic.convertStr(list[newNum - 1]).toMessage()
    }

    @Action("哔哩哔哩关注监控 {status}")
    @QMsg(at = true)
    fun biliBiliMonitor(biliBiliEntity: BiliBiliEntity, status: Boolean): String{
        biliBiliEntity.monitor = status
        biliBiliService.save(biliBiliEntity)
        return "哔哩哔哩我的关注监控已${if (status) "开启" else "关闭"}"
    }

    @Action("哔哩哔哩开播提醒 {username}")
    @QMsg(at = true)
    fun biliBiliLive(username: String, biliBiliEntity: BiliBiliEntity): String{
        val commonResult = this.searchToJsonObject(username)
        val jsonObject = commonResult.t ?: return commonResult.msg
        val liveJsonArray = biliBiliEntity.getLiveJsonArray()
        liveJsonArray.add(jsonObject)
        biliBiliEntity.liveList = liveJsonArray.toString()
        biliBiliService.save(biliBiliEntity)
        return "添加用户[${jsonObject["name"]}]的开播提醒成功！！"
    }

    @Action("查哔哩哔哩开播提醒")
    @QMsg(at = true, atNewLine = true)
    fun queryBiliBiliLive(biliBiliEntity: BiliBiliEntity): String{
        val liveJsonArray = biliBiliEntity.getLiveJsonArray()
        val sb = StringBuilder().appendln("您的开播提醒的用户如下：")
        liveJsonArray.forEach {
            val jsonObject = it as JSONObject
            sb.appendln("${jsonObject["name"]}-${jsonObject["id"]}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("删哔哩哔哩开播提醒 {username}")
    @QMsg(at = true)
    fun delBiliBiliLive(biliBiliEntity: BiliBiliEntity, username: String): String{
        val liveJsonArray = biliBiliEntity.getLiveJsonArray()
        BotUtils.delMonitorList(liveJsonArray, username)
        biliBiliEntity.liveList = liveJsonArray.toString()
        biliBiliService.save(biliBiliEntity)
        return "删除用户[$username]的开播提醒成功！！"
    }

    @Action("哔哩哔哩直播签到")
    @QMsg(at = true)
    fun biliBiliLiveSign(biliBiliEntity: BiliBiliEntity) = biliBiliLogic.liveSign(biliBiliEntity)

    @Action("哔哩哔哩自动{op} {username}")
    @QMsg(at = true)
    fun autoComment(biliBiliEntity: BiliBiliEntity, op: String, username: String, @PathVar(2) content: String?): String?{
        if (op == "三连" && content == null) return "添加哔哩哔哩自动三连需要收藏到的收藏夹名称！！"
        if (!arrayOf("自动赞", "自动投硬币").contains(op) && content == null) return "缺少${op}的内容！！"
        val commonResult = this.searchToJsonObject(username, if (content == null) null else mapOf("content" to content))
        val jsonObject = commonResult.t ?: return commonResult.msg
        when (op) {
            "赞" -> {
                val likeJsonArray = biliBiliEntity.getLikeJsonArray()
                likeJsonArray.add(jsonObject)
                biliBiliEntity.likeList = likeJsonArray.toString()
            }
            "评论" -> {
                val commentJsonArray = biliBiliEntity.getCommentJsonArray()
                commentJsonArray.add(jsonObject)
                biliBiliEntity.commentList = commentJsonArray.toString()
            }
            "转发" -> {
                val forwardJsonArray = biliBiliEntity.getForwardJsonArray()
                forwardJsonArray.add(jsonObject)
                biliBiliEntity.forwardList = forwardJsonArray.toString()
            }
            "投硬币" -> {
                val tossCoinJsonArray = biliBiliEntity.getTossCoinJsonArray()
                tossCoinJsonArray.add(jsonObject)
                biliBiliEntity.tossCoinList = tossCoinJsonArray.toString()
            }
            "收藏" -> {
                val favoritesJsonArray = biliBiliEntity.getFavoritesJsonArray()
                favoritesJsonArray.add(jsonObject)
                biliBiliEntity.favoritesList = favoritesJsonArray.toString()
            }
            "三连" -> {
                val likeJsonArray = biliBiliEntity.getLikeJsonArray()
                likeJsonArray.add(jsonObject)
                biliBiliEntity.likeList = likeJsonArray.toString()
                val tossCoinJsonArray = biliBiliEntity.getTossCoinJsonArray()
                tossCoinJsonArray.add(jsonObject)
                biliBiliEntity.tossCoinList = tossCoinJsonArray.toString()
                val favoritesJsonArray = biliBiliEntity.getFavoritesJsonArray()
                favoritesJsonArray.add(jsonObject)
                biliBiliEntity.favoritesList = favoritesJsonArray.toString()
            }
            else -> return null
        }
        biliBiliService.save(biliBiliEntity)
        return "添加哔哩哔哩用户[${jsonObject["name"]}]的${op}成功！！"
    }

    @Action("删哔哩哔哩{op} {username}")
    @QMsg(at = true)
    fun delAuto(biliBiliEntity: BiliBiliEntity, op: String, username: String): String?{
        when (op) {
            "自动赞" -> {
                val likeJsonArray = biliBiliEntity.getLikeJsonArray()
                BotUtils.delAuto(likeJsonArray, username)
                biliBiliEntity.likeList = likeJsonArray.toString()
            }
            "自动评论" -> {
                val commentJsonArray = biliBiliEntity.getCommentJsonArray()
                BotUtils.delAuto(commentJsonArray, username)
                biliBiliEntity.commentList = commentJsonArray.toString()
            }
            "自动转发" -> {
                val forwardJsonArray = biliBiliEntity.getForwardJsonArray()
                BotUtils.delAuto(forwardJsonArray, username)
                biliBiliEntity.forwardList = forwardJsonArray.toString()
            }
            "自动投硬币" -> {
                val tossCoinJsonArray = biliBiliEntity.getTossCoinJsonArray()
                BotUtils.delAuto(tossCoinJsonArray, username)
                biliBiliEntity.tossCoinList = tossCoinJsonArray.toString()
            }
            "自动收藏" -> {
                val favoritesJsonArray = biliBiliEntity.getFavoritesJsonArray()
                BotUtils.delAuto(favoritesJsonArray, username)
                biliBiliEntity.favoritesList = favoritesJsonArray.toString()
            }
            "自动三连" -> {
                val likeJsonArray = biliBiliEntity.getLikeJsonArray()
                BotUtils.delAuto(likeJsonArray, username)
                biliBiliEntity.likeList = likeJsonArray.toString()
                val tossCoinJsonArray = biliBiliEntity.getTossCoinJsonArray()
                BotUtils.delAuto(tossCoinJsonArray, username)
                biliBiliEntity.tossCoinList = tossCoinJsonArray.toString()
                val favoritesJsonArray = biliBiliEntity.getFavoritesJsonArray()
                BotUtils.delAuto(favoritesJsonArray, username)
                biliBiliEntity.favoritesList = favoritesJsonArray.toString()
            }
            else -> return null
        }
        biliBiliService.save(biliBiliEntity)
        return "删除哔哩哔哩用户[$username]的${op}成功！！"
    }

    @Action("查哔哩哔哩自动{op}")
    @QMsg(at = true, atNewLine = true)
    fun queryAuto(biliBiliEntity: BiliBiliEntity, op: String): String?{
        val jsonArray= when (op){
            "评论" -> biliBiliEntity.getCommentJsonArray()
            "赞" -> biliBiliEntity.getLikeJsonArray()
            "转发" -> biliBiliEntity.getForwardJsonArray()
            "投硬币" -> biliBiliEntity.getTossCoinJsonArray()
            "收藏" -> biliBiliEntity.getFavoritesJsonArray()
            else -> return null
        }
        val sb = StringBuilder().appendln("您的哔哩哔哩${op}列表如下：")
        jsonArray.forEach {
            val jsonObject = it as JSONObject
            sb.appendln("${jsonObject.getString("name")}-${jsonObject.getString("id")}-${jsonObject.getString("content") ?: ""}")
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("哔哩哔哩上传")
    @QMsg(at = true, atNewLine = true)
    fun uploadImage(qq: Long, session: ContextSession, biliBiliEntity: BiliBiliEntity): String{
        reply(mif.at(qq).plus("请输入需要上传的图片"))
        val body = session.waitNextMessage().body
        val sb = StringBuilder().appendln("您上传的图片链接如下：")
        var i = 1
        for (item in body) {
            if (item is Image){
                val response = OkHttpClientUtils.get(item.url)
                val commonResult = biliBiliLogic.uploadImage(biliBiliEntity, OkHttpClientUtils.getByteStr(response))
                val result = commonResult.t?.getString("image_url") ?: commonResult.msg
                sb.appendln("${i++}、$result")
            }
        }
        return sb.removeSuffixLine().toString()
    }

    @Action("哔哩哔哩{op} {username}")
    @QMsg(at = true)
    fun biliBiliComment(biliBiliEntity: BiliBiliEntity, username: String, op: String, @PathVar(2) content: String?, @PathVar(3) numStr: String?): String?{
        val num = numStr?.toInt() ?: content?.toInt()
        val commonResult = this.queryDynamic(username, num)
        val biliBiliPojo = commonResult.t ?: return commonResult.msg
        val id = biliBiliPojo.id
        val rid = biliBiliPojo.rid
        val bvId = biliBiliPojo.bvId
        return when (op){
            "赞" -> biliBiliLogic.like(biliBiliEntity, id, true)
            "评论" -> biliBiliLogic.comment(biliBiliEntity, rid, biliBiliPojo.type.toString(), content ?: "我来评论了！！")
            "转发" -> biliBiliLogic.forward(biliBiliEntity, id, content ?: "转发")
            "投硬币" -> if (bvId != null) biliBiliLogic.tossCoin(biliBiliEntity, rid, bvId, 2) else "该动态没有视频，不能投硬币！！"
            "收藏" -> if (bvId != null) biliBiliLogic.favorites(biliBiliEntity, rid, content ?: "我的收藏夹") else "该动态没有视频，不能收藏！！"
            else -> null
        }
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

    private fun parseNum(list: List<*>, num: Int?): Int{
        var newNum = num ?: 1
        if (newNum > list.size - 1) newNum = 1
        return newNum
    }

    private fun queryDynamic(username: String, num: Int?): CommonResult<BiliBiliPojo>{
        val idResult = biliBiliLogic.getIdByName(username)
        val idList = idResult.t ?: return CommonResult(500, idResult.msg)
        val dynamicResult = biliBiliLogic.getDynamicById(idList[0].userId)
        val biliBiliList = dynamicResult.t ?: return CommonResult(500, dynamicResult.msg)
        if (biliBiliList.isEmpty()) return CommonResult(500, "这个用户没有发现动态哦！！")
        var newNum = num ?: 1
        if (newNum > biliBiliList.size) newNum = 1
        return CommonResult(200, "", biliBiliList[newNum - 1])
    }

    private fun searchToJsonObject(username: String, map: Map<String, String>? = null): CommonResult<JSONObject>{
        val commonResult = biliBiliLogic.getIdByName(username)
        val list = commonResult.t ?: return CommonResult(500, commonResult.msg)
        val biliBiliPojo = list[0]
        val jsonObject = JSONObject()
        jsonObject["id"] = biliBiliPojo.userId
        jsonObject["name"] = biliBiliPojo.name
        if (map != null) {
            for ((k, v) in map){
                jsonObject[k] = v
            }
        }
        return CommonResult(200, "", jsonObject)
    }
}
package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.logic.WeiboLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.WeiboPojo
import me.kuku.yuq.service.WeiboService
import me.kuku.yuq.utils.removeSuffixLine
import javax.inject.Inject

@GroupController
class WeiboController {
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var weiboService: WeiboService

    private val hotMap: MutableMap<Long, List<String>> = mutableMapOf()

    @Before
    fun before(qq: Long, message: Message): WeiboEntity?{
        val str = message.toPath()[0]
        return if (str in arrayOf("wbmy", "wbmymonitor", "微博关注监控", "微博评论")){
            return weiboService.findByQQ(qq) ?: throw mif.at(qq).plus("您还未绑定微博，请先私聊机器人发送（wb 账号 密码）进行绑定")
        }else null
    }

    @Action("热搜")
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
    @QMsg(at = true)
    fun myFriendWeibo(@PathVar(1) numStr: String?, weiboEntity: WeiboEntity): String{
        val commonResult = weiboLogic.getFriendWeibo(weiboEntity)
        return if (commonResult.code == 200) {
            val list = commonResult.t!!
            val num = try {
                numStr?.toInt()
            }catch (e: Exception){
                return "第一个参数应为整型！！"
            }
            val weiboPojo = this.getWeiboPojo(list, num)
            weiboLogic.convertStr(weiboPojo)
        }else commonResult.msg
    }

    @Action("wbtopic {keyword}")
    @Synonym(["微博话题 {keyword}"])
    fun weiboTopic(keyword: String, @PathVar(value = 2, type = PathVar.Type.Integer) num: Int?): String{
        val commonResult = weiboLogic.weiboTopic(keyword)
        if (commonResult.code != 200) return commonResult.msg
        val list = commonResult.t!!
        val weiboPojo = this.getWeiboPojo(list, num)
        return weiboLogic.convertStr(weiboPojo)
    }

    @Action("微博评论 {username} {content}")
    fun comment(@PathVar(4) numStr: String?, username: String, content: String, weiboEntity: WeiboEntity): String{
        val commonResult = this.queryWeibo(username, numStr)
        val weiboPojo = commonResult.t ?: return commonResult.msg
        return weiboLogic.comment(weiboEntity, weiboPojo.id, content)
    }

    private fun getWeiboPojo(list: List<WeiboPojo>, num: Int?): WeiboPojo {
        return when {
            num == null -> list[0]
            num >= list.size -> list[0]
            else -> list[num]
        }
    }

    private fun queryWeibo(username: String, numStr: String?): CommonResult<WeiboPojo> {
        val idResult = weiboLogic.getIdByName(username)
        val idList = idResult.t ?: return CommonResult(500, idResult.msg)
        val queryWeiboPojo = idList[0]
        val weiboResult = weiboLogic.getWeiboById(queryWeiboPojo.userId)
        val weiboList = weiboResult.t ?: return CommonResult(500, weiboResult.msg)
        val num = try {
            numStr?.toInt()
        }catch (e: Exception){
            return CommonResult(500, "第二个参数应为整型！！")
        }
        val weiboPojo = this.getWeiboPojo(weiboList, num)
        return CommonResult(200, "", weiboPojo)
    }

}
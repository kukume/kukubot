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
import me.kuku.yuq.service.WeiboService
import javax.inject.Inject

@GroupController
class WeiboController {
    @Inject
    private lateinit var weiboLogic: WeiboLogic
    @Inject
    private lateinit var weiboService: WeiboService

    @Before
    fun before(qq: Long, message: Message): WeiboEntity?{
        val str = message.toPath()[0]
        return if (str in arrayOf("wbmy", "wbmymonitor", "微博关注监控")){
            return weiboService.findByQQ(qq) ?: throw mif.at(qq).plus("您还未绑定微博，请先私聊机器人发送（wb 账号 密码）进行绑定")
        }else null
    }

    @Action("热搜")
    fun hotSearch() = weiboLogic.hotSearch()

    @Action("wb {username}")
    fun searchWeibo(username: String, @PathVar(2) numStr: String?): String{
        val idResult = weiboLogic.getIdByName(username)
        val idList = idResult.t ?: return idResult.msg
        val queryWeiboPojo = idList[0]
        val weiboResult = weiboLogic.getWeiboById(queryWeiboPojo.userId)
        val weiboList = weiboResult.t ?: return weiboResult.msg
        val num = try {
            numStr?.toInt()
        }catch (e: Exception){
            return "第二个参数应为整型！！"
        }
        val weiboPojo = when {
            num == null -> weiboList[0]
            num >= weiboList.size -> weiboList[0]
            else -> weiboList[num]
        }
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
            val list = commonResult.t
            val num = try {
                numStr?.toInt()
            }catch (e: Exception){
                return "第一个参数应为整型！！"
            }
            val weiboPojo = when {
                num == null -> list[0]
                num >= list.size -> list[0]
                else -> list[num]
            }
            weiboLogic.convertStr(weiboPojo)
        }else commonResult.msg
    }

}
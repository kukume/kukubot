package me.kuku.yuq.logic.impl

import com.IceCreamQAQ.Yu.util.Web
import com.alibaba.fastjson.JSON
import com.icecreamqaq.yuq.mirai.MiraiBot
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.logic.QQGroupLogic
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

class QQGroupLogicImpl: QQGroupLogic {

    @Inject
    private lateinit var web: Web
    @Inject
    private lateinit var miraiBot: MiraiBot

    override fun addGroupMember(qq: Long, group: Long): String {
        val str = web.post("https://qun.qq.com/cgi-bin/qun_mgr/add_group_member",
                mapOf("gc" to group.toString(), "ul" to qq.toString(),
                        "bkn" to miraiBot.gtk.toString()))
        val jsonObject = JSON.parseObject(str)
        return when (jsonObject.getInteger("ec")){
            0 -> "邀请${qq}成功"
            4 -> "邀请失败，请更新QQ！！！"
            else -> "邀请失败，${jsonObject.getString("em")}"
        }
    }

    override fun setGroupAdmin(qq: Long, group: Long, isAdmin: Boolean): String {
        val str = web.post("https://qun.qq.com/cgi-bin/qun_mgr/set_group_admin",
                mapOf("gc" to group.toString(), "ul" to qq.toString(),
                        "op" to if (isAdmin) "1" else "0",
                        "bkn" to miraiBot.gtk.toString()))
        val jsonObject = JSON.parseObject(str)
        return when (jsonObject.getInteger("ec")){
            0 -> "设置${qq}为管理员成功"
            4 -> "设置失败，请更新QQ！！！"
            14,3 -> "设置失败，木得权限！！"
            else -> "设置失败，${jsonObject.getString("em")}"
        }
    }

    override fun setGroupCard(qq: Long, group: Long, name: String): String {
        val str = web.post("https://qun.qq.com/cgi-bin/qun_mgr/set_group_card",
                mapOf("gc" to group.toString(), "ul" to qq.toString(),
                        "name" to name, "bkn" to miraiBot.gtk.toString()))
        val jsonObject = JSON.parseObject(str)
        return when (jsonObject.getInteger("ec")){
            0 -> "更改${qq}名片为${name}成功"
            4 -> "更改名片失败，请更新QQ！！！"
            14,3 -> "更改名片失败，木得权限！！"
            else -> "更改名片失败，${jsonObject.getString("em")}"
        }
    }

    override fun deleteGroupMember(qq: Long, group: Long, isFlag: Boolean): String {
        val str = web.post("https://qun.qq.com/cgi-bin/qun_mgr/delete_group_member",
                mapOf("gc" to group.toString(), "ul" to qq.toString(),
                        "name" to if (isFlag) "1" else "0", "bkn" to miraiBot.gtk.toString()))
        val jsonObject = JSON.parseObject(str)
        return when (jsonObject.getInteger("ec")){
            0 -> "踢${qq}成功"
            4 -> "踢人失败，请更新QQ！！！"
            14,3 -> "踢人失败，木得权限！！"
            else -> "踢人失败，${jsonObject.getString("em")}"
        }
    }

    override fun groupDragonKing(group: Long): CommonResult<Map<String, Long>> {
        val html = web.get("https://qun.qq.com/interactive/qunhonor?gc=$group&_wv=3&&_wwv=128&showMine=1")
        val jsonStr = BotUtils.regex("INITIAL_STATE__=", "<", html)
        val jsonObject = JSON.parseObject(jsonStr)
        return if (jsonObject.getJSONArray("admins").size != 0){
            val activeJsonObject = jsonObject.getJSONObject("currentTalkative")
            if (activeJsonObject.size == 0) CommonResult(500, "昨天木得龙王！！")
            //uin qq; day_count  蝉联时间 ; avatar  头像链接 ;avatar_size  头像大小
            else CommonResult(200, "", mapOf<String, Long>("qq" to activeJsonObject.getLong("uin"), "day" to activeJsonObject.getLong("day_count")))
        }else CommonResult(500, "获取龙王失败，请更新qun.qq.com的cookie")
    }
}
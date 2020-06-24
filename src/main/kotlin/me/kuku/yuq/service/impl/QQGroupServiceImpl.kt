package me.kuku.yuq.service.impl

import com.alibaba.fastjson.JSON
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils

class QQGroupServiceImpl: QQGroupService {
    override fun addGroupMember(qqEntity: QQEntity, qq: Long, group: Long): String {
        val response = OkHttpClientUtils.post("https://qun.qq.com/cgi-bin/qun_mgr/add_group_member", OkHttpClientUtils.addForms(
                "gc", group.toString(),
                "ul", qq.toString(),
                "bkn", qqEntity.getGtk()
        ), OkHttpClientUtils.addCookie(qqEntity.getCookieWithGroup()))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> "邀请${qq}成功"
            4 -> "邀请失败，请更新QQ！！！"
            else -> "邀请失败，${jsonObject.getString("em")}"
        }
    }

    override fun setGroupAdmin(qqEntity: QQEntity, qq: Long, group: Long, isAdmin: Boolean): String {
        val response = OkHttpClientUtils.post("https://qun.qq.com/cgi-bin/qun_mgr/set_group_admin", OkHttpClientUtils.addForms(
                "gc", group.toString(),
                "ul", qq.toString(),
                "op", if (isAdmin) "1" else "0",
                "bkn", qqEntity.getGtk()
        ), qqEntity.cookieWithGroup())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> "设置${qq}为管理员成功"
            4 -> "设置失败，请更新QQ！！！"
            14,3 -> "设置失败，木得权限！！"
            else -> "设置失败，${jsonObject.getString("em")}"
        }
    }

    override fun setGroupCard(qqEntity: QQEntity, qq: Long, group: Long, name: String): String {
        val response = OkHttpClientUtils.post("https://qun.qq.com/cgi-bin/qun_mgr/set_group_card", OkHttpClientUtils.addForms(
                "gc", group.toString(),
                "ul", qq.toString(),
                "name", name,
                "bkn", qqEntity.getGtk()
        ), qqEntity.cookieWithGroup())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> "更改${qq}名片为${name}成功"
            4 -> "更改名片失败，请更新QQ！！！"
            14,3 -> "更改名片失败，木得权限！！"
            else -> "更改名片失败，${jsonObject.getString("em")}"
        }
    }

    override fun deleteGroupMember(qqEntity: QQEntity, qq: Long, group: Long, isFlag: Boolean): String {
        val response = OkHttpClientUtils.post("https://qun.qq.com/cgi-bin/qun_mgr/delete_group_member", OkHttpClientUtils.addForms(
                "gc", group.toString(),
                "ul", qq.toString(),
                "name", if (isFlag) "1" else "0",
                "bkn", qqEntity.getGtk()
        ), qqEntity.cookieWithGroup())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ec")){
            0 -> "踢${qq}成功"
            4 -> "踢人失败，请更新QQ！！！"
            14,3 -> "踢人失败，木得权限！！"
            else -> "踢人失败，${jsonObject.getString("em")}"
        }
    }

    override fun groupDragonKing(qqEntity: QQEntity, group: Long): CommonResult<Map<String, Long>> {
        val response = OkHttpClientUtils.get("https://qun.qq.com/interactive/qunhonor?gc=$group&_wv=3&&_wwv=128&showMine=1", qqEntity.cookieWithGroup())
        val html = OkHttpClientUtils.getStr(response)
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
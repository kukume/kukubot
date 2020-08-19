package me.kuku.yuq.logic.impl

import com.IceCreamQAQ.Yu.util.Web
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.mirai.MiraiBot
import me.kuku.yuq.logic.QQGroupLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.GroupMember
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

    override fun addHomeWork(group: Long, courseName: String, title: String, content: String, needFeedback: Boolean): String {
        val str = web.post("https://qun.qq.com/cgi-bin/homework/hw/assign_hw.fcg", mapOf(
                "homework_id" to "",
                "group_id" to group.toString(),
                "course_id" to "2",
                "course_name" to courseName,
                "title" to title,
                "need_feedback" to if (needFeedback) "1" else "0",
                "c" to "{\"c\":[{\"type\":\"str\",\"text\":\"$content\"}]}",
                "team_id" to "0",
                "hw_type" to "0",
                "tsfeedback" to "",
                "syncgids" to "",
                "client_type" to "1",
                "bkn" to miraiBot.gtk.toString()
        ))
        val jsonObject = JSON.parseObject(str)
        return when (jsonObject.getInteger("retcode")){
            0 -> "发布作业成功！！"
            110002 -> "权限不足，无法发布作业！！"
            100000 -> "发布失败，请更新QQ！！"
            else -> jsonObject.getString("msg")
        }
    }

    override fun groupCharin(group: Long, content: String, time: Long): String {
        val str = web.post("https://qun.qq.com/cgi-bin/group_chain/chain_new", mapOf(
                "gc" to group.toString(),
                "desc" to content,
                "type" to "2",
                "expired" to time.toString(),
                "bkn" to miraiBot.gtk.toString()
        ))
        val jsonObject = JSON.parseObject(str)
        println(jsonObject)
        return when (jsonObject.getInteger("retcode")){
            0 -> "发布群接龙成功！！"
            11004 -> "到期时间格式有误"
            10013 -> "权限不足，无法发布群接龙！！"
            100000 -> "发布失败，请更新QQ！！"
            else -> jsonObject.getString("msg")
        }
    }

    override fun groupLevel(group: Long): CommonResult<List<Map<String, String>>> {
        val str = web.get("https://qun.qq.com/interactive/levellist?gc=$group&type=7&_wv=3&_wwv=128")
        val jsonStr = BotUtils.regex("window.__INITIAL_STATE__=", "</script>", str)
        val jsonObject = JSON.parseObject(jsonStr)
        val jsonArray = jsonObject.getJSONArray("membersList")
        if (jsonArray.size == 0) return CommonResult(500, "获取群等级列表失败，请更新QQ！！")
        val list= mutableListOf<Map<String, String>>()
        for (i in jsonArray.indices){
            val singleJsonObject = jsonArray.getJSONObject(i)
            val map = mapOf(
                    "name" to singleJsonObject.getString("name"),
                    "level" to singleJsonObject.getString("level"),
                    "tag" to singleJsonObject.getString("tag"),
                    "qq" to singleJsonObject.getString("uin")
            )
            list.add(map)
        }
        return CommonResult(200, "", list)
    }

    override fun queryMemberInfo(group: Long, qq: Long): CommonResult<GroupMember> {
        val str = web.post("https://qun.qq.com/cgi-bin/qun_mgr/search_group_members", mapOf(
                "gc" to group.toString(),
                "st" to "0",
                "end" to "20",
                "sort" to "0",
                "key" to qq.toString(),
                "bkn" to miraiBot.gtk.toString()
        ))
        val jsonObject = JSON.parseObject(str)
        return when (jsonObject.getInteger("ec")){
            0 -> {
                val jsonArray = jsonObject.getJSONArray("mems")
                if (jsonArray == null || jsonArray.size == 0) return CommonResult(500, "未搜索到该用户")
                val memberJsonObject = jsonArray.getJSONObject(0)
                val card = memberJsonObject.getString("card")
                val name = if (card == "") memberJsonObject.getString("nick")
                else card
                CommonResult(200, "", GroupMember(
                        memberJsonObject.getLong("uin"),
                        0,
                        0,
                        (memberJsonObject.getString("join_time") + "000").toLong(),
                        (memberJsonObject.getString("last_speak_time") + "000").toLong(),
                        memberJsonObject.getInteger("qage"),
                        name
                ))
            }
            4 -> CommonResult(500, "查询失败，请更新QQ！！！")
            else -> CommonResult(500, jsonObject.getString("em"))
        }
    }

    override fun essenceMessage(group: Long): CommonResult<List<String>> {
        val html = web.get("https://qun.qq.com/essence/index?gc=$group&_wv=3&_wwv=128&_wvx=2&_wvxBclr=f5f6fa")
        val jsonStr = BotUtils.regex("window.__INITIAL_STATE__=", "</", html)
        val jsonObject = JSON.parseObject(jsonStr)
        return if (jsonObject.getInteger("pageStart") == 1){
            val jsonArray = jsonObject.getJSONArray("msgList")
            if (jsonArray.size == 0) return CommonResult(500, "当前群内没有精华消息！！")
            val list = mutableListOf<String>()
            for (i in jsonArray.indices){
                val msgJsonObject = jsonArray.getJSONObject(i)
                val contentJsonArray = msgJsonObject.getJSONArray("msg_content")
                val sb = StringBuilder()
                contentJsonArray.forEach {
                    val singleJsonObject = it as JSONObject
                    sb.append(singleJsonObject.getString("text") ?: singleJsonObject.getString("face_text"))
                }
                list.add(sb.toString())
            }
            CommonResult(200, "", list)
        }else CommonResult(500, "查询失败，请更新qun.qq.com的cookie")
    }

    override fun queryGroup(): CommonResult<List<Long>> {
        val str = web.post(
            "https://qun.qq.com/cgi-bin/qun_mgr/get_group_list", mapOf(
                "bkn" to miraiBot.gtk.toString()
            )
        )
        val jsonObject = JSON.parseObject(str)
        return if (jsonObject.getInteger("ec") == 0){
            val list = mutableListOf<Long>()
            val manageJsonArray = jsonObject.getJSONArray("manage")
            manageJsonArray?.forEach {
                val groupJsonObject = it as JSONObject
                list.add(groupJsonObject.getLong("gc"))
            }
            val joinJsonArray = jsonObject.getJSONArray("join")
            joinJsonArray?.forEach {
                val groupJsonObject = it as JSONObject
                list.add(groupJsonObject.getLong("gc"))
            }
            val createJsonArray = jsonObject.getJSONArray("create")
            createJsonArray?.forEach {
                val groupJsonObject = it as JSONObject
                list.add(groupJsonObject.getLong("gc"))
            }
            CommonResult(200, "", list)
        }else CommonResult(500, "查询失败，请更新qun.qq.com的cookie")
    }
}
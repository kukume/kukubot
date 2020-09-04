package me.kuku.yuq.logic.impl

import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.mirai.MiraiBot
import me.kuku.yuq.logic.QQGroupLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.GroupMember
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.postQQUA
import javax.inject.Inject

class QQGroupLogicImpl: QQGroupLogic {

    @Inject
    private lateinit var web: OkHttpWebImpl
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
        val jsonArray = jsonObject.getJSONArray("msgList")
        if (jsonArray.size == 0) return CommonResult(500, "当前群内没有精华消息！！或者cookie已失效！！")
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
        return CommonResult(200, "", list)
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

    override fun groupHonor(group: Long, type: String): List<Map<String, String>> {
        val typeNum: Int
        val wwv: Int
        val param: String
        val image: String
        when (type){
            "talkAtIve" -> {
                typeNum = 1
                wwv = 129
                param = "talkativeList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-drgon.png"
            }
            "legend" -> {
                typeNum = 3
                wwv = 128
                param = "legendList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-fire-big.png"
            }
            "actor" -> {
                typeNum = 2
                wwv = 128
                param = "actorList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-fire-small.png"
            }
            "strongNewBie" -> {
                typeNum = 5
                wwv = 128
                param = "strongnewbieList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-shoots-small.png"
            }
            "emotion" -> {
                typeNum = 6
                wwv = 128
                param = "emotionList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-happy-stream.png"
            }
            else -> return listOf()
        }
        val html = web.get("https://qun.qq.com/interactive/honorlist?gc=$group&type=$typeNum&_wv=3&_wwv=$wwv")
        val jsonStr = BotUtils.regex("window.__INITIAL_STATE__=", "</script", html)
        val jsonObject = JSON.parseObject(jsonStr)
        val jsonArray = jsonObject.getJSONArray(param)
        val list = mutableListOf<Map<String, String>>()
        jsonArray.forEach {
            val singleJsonObject = it as JSONObject
            list.add(mapOf(
                    "qq" to singleJsonObject.getString("uin"),
                    "name" to singleJsonObject.getString("name"),
                    "desc" to singleJsonObject.getString("desc"),
                    "image" to image
            ))
        }
        return list
    }

    override fun groupSign(group: Long, place: String, text: String, name: String, picId: String?): String {
        val gtk = miraiBot.gtk
        var info: String? = null
        var templateId: String? = null
        val templateStr = web.get("https://qun.qq.com/cgi-bin/qiandao/gallery_template?gc=$group&bkn=$gtk&time=1014")
        val templateJsonObject = JSON.parseObject(templateStr)
        if (templateJsonObject.getInteger("retcode") != 0) return "qq群签到失败，请更新QQ！！"
        templateJsonObject.getJSONObject("data").getJSONArray("list").forEach {
            val singleJsonObject = it as JSONObject
            if (singleJsonObject.getString("name") == name){
                info = if (singleJsonObject.containsKey("gallery_info")){
                    val infoJsonObject = singleJsonObject.getJSONObject("gallery_info")
                    "{\"category_id\":${infoJsonObject.getInteger("category_id")},\"page\":${infoJsonObject.getInteger("page")},\"pic_id\":${infoJsonObject.getInteger("pic_id")}}"
                }else "{\"category_id\":\"\",\"page\":\"\",\"pic_id\":\"\"}"
                templateId = singleJsonObject.getString("id")
                return@forEach
            }
        }
        if (info == null || templateId == null){
            val template2Str = web.get("https://qun.qq.com/cgi-bin/qiandao/gallery_list?bkn=$gtk&category_ids=[9]&start=0&num=50")
            val template2JsonObject = JSON.parseObject(template2Str)
            val picJsonObject = template2JsonObject.getJSONObject("data").getJSONArray("picture_list").getJSONObject(0)
            picJsonObject.getJSONArray("picture_item").forEach {
                val singleJsonObject = it as JSONObject
                if (singleJsonObject.getString("name") == name){
                    info = "{\"category_id\":${picJsonObject.getInteger("category_id")},\"page\":${singleJsonObject.getInteger("page")},\"pic_id\":${singleJsonObject.getInteger("picture_id")}}"
                    templateId = "[object Object]"
                    return@forEach
                }
            }
        }
        if (info == null || templateId == null) return "群签到类型中没有${name}这个类型，请重试！！"
        val str = web.postQQUA("https://qun.qq.com/cgi-bin/qiandao/sign/publish", mapOf(
                "btn" to gtk.toString(),
                "template_data" to "",
                "gallery_info" to info!!,
                "template_id" to templateId!!,
                "gc" to group.toString(),
                "client" to "2",
                "lgt" to "0",
                "lat" to "0",
                "poi" to place,
                "text" to text,
                "pic_id" to (picId ?: "")
        ))
        val jsonObject = JSON.parseObject(str)
        return when (jsonObject.getInteger("retcode")){
            0 -> "qq群${group}签到成功"
            10013,10001 -> "qq群签到失败，已被禁言！"
            10016 -> "群签到一次性只能签到5个群，请10分钟后再试！"
            5 -> "qq群签到失败，请更新QQ！！"
            else -> "qq群签到失败，${jsonObject.getString("msg")}"
        }
    }
}
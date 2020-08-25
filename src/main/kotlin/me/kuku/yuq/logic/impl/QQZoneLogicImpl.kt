package me.kuku.yuq.logic.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.logic.QQZoneLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.GroupMember
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import okhttp3.FormBody
import java.util.*

class QQZoneLogicImpl: QQZoneLogic {

    override fun friendTalk(qqEntity: QQEntity): List<Map<String, String?>>? {
        val response = OkHttpClientUtils.post("https://h5.qzone.qq.com/webapp/json/mqzone_feeds/getActiveFeeds?g_tk=${qqEntity.getGtkP()}",
                OkHttpClientUtils.addForms("res_type", "0", "res_attach", "", "refresh_type", "2",
                        "format", "json", "attach_info", ""),
                OkHttpClientUtils.addCookie(qqEntity.getCookieWithQQZone()))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0){
            val feedsJsonArray = jsonObject.getJSONObject("data").getJSONArray("vFeeds")
            val list = mutableListOf<Map<String, String?>>()
            feedsJsonArray.forEach {
                val feedJsonObject = it as JSONObject
                val commJsonObject = feedJsonObject.getJSONObject("comm")
                val userJsonObject = feedJsonObject.getJSONObject("userinfo").getJSONObject("user")
                val map = mapOf(
                        "id" to feedJsonObject.getJSONObject("id").getString("cellid"),
                        "orgLikeKey" to commJsonObject.getString("orglikekey"),
                        "curLikeKey" to commJsonObject.getString("curlikekey"),
                        "like" to feedJsonObject.getJSONObject("like")?.getString("isliked"),
                        "qq" to userJsonObject.getString("uin")
                )
                list.add(map)
            }
            list
        }else null
    }

    override fun talkByQQ(qqEntity: QQEntity, qq: Long): List<Map<String, String?>>? {
        val response = OkHttpClientUtils.get("https://mobile.qzone.qq.com/get_feeds?g_tk=${qqEntity.getGtkP()}&hostuin=$qq&res_type=2&res_attach=&refresh_type=2&format=json",
                OkHttpClientUtils.addCookie(qqEntity.getCookieWithQQZone()))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0){
            val feedsJsonArray = jsonObject.getJSONObject("data").getJSONArray("vFeeds")
            val list = mutableListOf<Map<String, String?>>()
            feedsJsonArray.forEach {
                val feedJsonObject = it as JSONObject
                val map = mapOf(
                        "id" to feedJsonObject.getJSONObject("id").getString("cellid"),
                        "qq" to qqEntity.qq.toString()
                )
                list.add(map)
            }
            list
        }else null
    }

    override fun forwardTalk(qqEntity: QQEntity, id: String, qq: String, text: String): String {
        val response = OkHttpClientUtils.post("https://mobile.qzone.qq.com/operation/operation_add?g_tk=${qqEntity.getGtkP()}",
                OkHttpClientUtils.addForms("res_id", id, "res_uin", qq, "format", "json",
                        "reason", text, "res_type", "311", "opr_type", "forward", "operate", "1"),
                OkHttpClientUtils.addCookie(qqEntity.getCookieWithQQZone()))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0){
            "转发${qq}的说说成功"
        }else "转发说说失败，请更新QQ"
    }

    override fun publishTalk(qqEntity: QQEntity, text: String): String {
        val response = OkHttpClientUtils.post("https://mobile.qzone.qq.com/mood/publish_mood?g_tk=${qqEntity.getGtkP()}",
                OkHttpClientUtils.addForms("opr_type", "publish_shuoshuo", "res_uin", qqEntity.qq.toString(),
                        "content", text, "richval", "", "lat", "0", "lon", "0", "lbsid", "", "issyncweibo", "0",
                        "format", "json"), OkHttpClientUtils.addCookie(qqEntity.getCookieWithQQZone()))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0){
            "发说说成功"
        }else "发说说失败，请更新QQ！"
    }

    override fun removeTalk(qqEntity: QQEntity, id: String): String {
        val response = OkHttpClientUtils.post("https://mobile.qzone.qq.com/operation/operation_add?g_tk=${qqEntity.getGtkP()}",
                OkHttpClientUtils.addForms("opr_type", "delugc", "res_type", "311", "res_id", id,
                        "real_del", "0", "res_uin", qqEntity.qq.toString(), "format", "json"),
                OkHttpClientUtils.addCookie(qqEntity.getCookieWithQQZone()))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 0){
            "删除说说成功"
        }else "删除说说失败，请更新QQ！"
    }

    override fun commentTalk(qqEntity: QQEntity, id: String, qq: String, text: String): String {
        val response = OkHttpClientUtils.post("https://mobile.qzone.qq.com/operation/publish_addcomment?g_tk=${qqEntity.getGtkP()}",
                OkHttpClientUtils.addForms("res_id", id, "res_uin", qq, "format", "json", "res_type", "311",
                        "content", text, "busi_param", "", "opr_type", "addcomment"))
        return if (OkHttpClientUtils.getJson(response).getInteger("code") == 0){
            "评论${qq}的说说成功"
        }else "评论说说失败，请更新QQ"
    }

    override fun likeTalk(qqEntity: QQEntity, map: Map<String, String?>): String {
        val response = OkHttpClientUtils.post("https://h5.qzone.qq.com/proxy/domain/w.qzone.qq.com/cgi-bin/likes/internal_dolike_app?g_tk=${qqEntity.getGtkP()}",
                OkHttpClientUtils.addForms(
                        "opuin", map["qq"].toString(),
                        "unikey", map["orgLikeKey"].toString(),
                        "curkey", map["curLikeKey"].toString(),
                        "appid", "311",
                        "opr_type", "like",
                        "format", "purejson"
                ), OkHttpClientUtils.addCookie(qqEntity.getCookieWithQQZone()))
        return if (OkHttpClientUtils.getJson(response).getInteger("ret") == 0){
            "赞${map["qq"]}的说说成功"
        }else "赞说说失败，请更新QQ"
    }

    override fun addFriend(qqEntity: QQEntity, qq: Long, msg: String, realName: String?, group: String?): String {
        val gtkP = qqEntity.getGtkP()
        var groupId = 0
        this.visitQZoneMobile(qqEntity, qq)
        val refererUrl = "https://user.qzone.qq.com/$qq?ADUIN=${qqEntity.qq}&ADSESSION=${Date().time}&ADTAG=CLIENT.QQ.5761_FriendInfo_PersonalInfo.0&ADPUBNO=27041&source=namecardstar"
        val firstResponse = OkHttpClientUtils.get("https://user.qzone.qq.com/proxy/domain/r.qzone.qq.com/cgi-bin/tfriend/friend_getgroupinfo.cgi?uin=${qqEntity.qq}&rd=0.${BotUtils.randomNum(15)}&fupdate=1&fuin=$qq&g_tk=$gtkP&g_tk=$gtkP", OkHttpClientUtils.addHeaders(
                "referer", "https://user.qzone.qq.com/$qq",
                "cookie", qqEntity.getCookieWithQQZone()
        ))
        val firstJsonObject = OkHttpClientUtils.getJsonp(firstResponse)
        val dataFirstJsonObject = firstJsonObject.getJSONObject("data")
        if (firstJsonObject.getInteger("code") == 0){
            if (group != null) {
                val jsonArray = dataFirstJsonObject.getJSONArray("items")
                for (i in jsonArray.indices) {
                    val groupJsonObject = jsonArray.getJSONObject(i)
                    if (groupJsonObject.getString("groupname") == group) {
                        groupId = groupJsonObject.getInteger("groupId")
                        break
                    }
                }
            }
        }else return "请求添加好友失败，${firstJsonObject.getString("message")}"
        val secondResponse = OkHttpClientUtils.post("https://user.qzone.qq.com/proxy/domain/w.qzone.qq.com/cgi-bin/tfriend/friend_authfriend.cgi?&g_tk=$gtkP", OkHttpClientUtils.addForms(
                "sid", "0",
                "ouin", qq.toString(),
                "uin", qqEntity.qq.toString(),
                "fuin", qq.toString(),
                "sourceId", "1",
                "fupdate", "1",
                "qzreferrer", refererUrl
        ), OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookieWithQQZone(),
                "referer", refererUrl
        ))
        val secondJsonObject = OkHttpClientUtils.getJson(secondResponse, "(?<=frameElement.callback\\()[\\s\\S]*?(?=\\);)")
        if (secondJsonObject.getInteger("code") == 0){
            if (secondJsonObject.getJSONObject("data").getInteger("state") == 2) return "主人设置了权限，无法添加为好友！！"
            val builder = FormBody.Builder()
                    .add("sid", "3")
                    .add("ouin", qq.toString())
                    .add("uin", qqEntity.qq.toString())
                    .add("fuin", qq.toString())
                    .add("sourceId", "1")
                    .add("fupdate", "1")
                    .add("qzreferrer", refererUrl)
                    .add("rd", "0.${BotUtils.randomNum(16)}")
                    .add("groupId", groupId.toString())
                    .add("realname", realName ?: dataFirstJsonObject.getJSONObject("users").getString("realname"))
                    .add("flag", "0")
                    .add("key", "")
                    .add("verify", "")
                    .add("im", "0")
                    .add("format", "fs")
                    .add("from", "8")
                    .add("from_source", "8")
            val dataJsonObject = secondJsonObject.getJSONObject("data")
            if (dataJsonObject.getInteger("state") == 4){
                val size = dataJsonObject.getJSONObject("question").size
                for (i in 0 until size)
                    builder.add("ans$i", msg)
            }else builder.add("strmsg", msg)
            val response = OkHttpClientUtils.post("https://user.qzone.qq.com/proxy/domain/w.qzone.qq.com/cgi-bin/tfriend/friend_addfriend.cgi?&g_tk=$gtkP",
                    builder.build(), OkHttpClientUtils.addHeaders(
                    "cookie", qqEntity.getCookieWithQQZone(),
                    "referer", refererUrl
            ))
            val str = OkHttpClientUtils.getStr(response)
            val jsonStr = BotUtils.regex("(?<=frameElement.callback\\()[\\s\\S]*?(?=\\);)", str)
            val jsonObject = JSON.parseObject(jsonStr)
            return when (jsonObject.getInteger("code")){
                0 -> jsonObject.getString("message")
                -3000 -> "添加好友失败，请更新QQ！"
                else -> jsonObject.getString("message")
            }
        }else return secondJsonObject.getString("message")
    }

    override fun queryGroup(qqEntity: QQEntity): CommonResult<List<Map<String, String>>> {
        val response = OkHttpClientUtils.get("https://user.qzone.qq.com/proxy/domain/r.qzone.qq.com/cgi-bin/tfriend/qqgroupfriend_extend.cgi?uin=${qqEntity.qq}&rd=0.${BotUtils.randomNum(16)}&cntperpage=0&fupdate=1&g_tk=${qqEntity.getGtkP()}&g_tk=${qqEntity.getGtkP()}", OkHttpClientUtils.addHeaders(
                "referer", "https://user.qzone.qq.com/${qqEntity.qq}/myhome/friends/ofpmd",
                "cookie", qqEntity.getCookieWithQQZone()
        ))
        val jsonObject = OkHttpClientUtils.getJsonp(response)
        return if (jsonObject.getInteger("code") == 0){
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("group")
            val list = mutableListOf<Map<String, String>>()
            // groupcode
            for (i in jsonArray.indices){
                val singleJsonObject = jsonArray.getJSONObject(i)
                list.add(
                        mapOf("group" to singleJsonObject.getString("groupcode"), "groupName" to singleJsonObject.getString("groupname"))
                )
            }
            return CommonResult(200, "", list)
        }else CommonResult(500, jsonObject.getString("message"))
    }

    override fun queryGroupMember(qqEntity: QQEntity, group: String): CommonResult<List<Map<String, String>>> {
        val response = OkHttpClientUtils.get("https://user.qzone.qq.com/proxy/domain/r.qzone.qq.com/cgi-bin/tfriend/qqgroupfriend_groupinfo.cgi?uin=${qqEntity.qq}&gid=$group&fupdate=1&type=1&g_tk=${qqEntity.getGtkP()}&g_tk=${qqEntity.getGtkP()}", OkHttpClientUtils.addHeaders(
                "referer", "https://user.qzone.qq.com/${qqEntity.qq}/myhome/friends/ofpmd",
                "cookie", qqEntity.getCookieWithQQZone()
        ))
        val jsonObject = OkHttpClientUtils.getJsonp(response)
        return if (jsonObject.getInteger("code") == 0){
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("friends")
            val list = mutableListOf<Map<String, String>>()
            // groupcode
            for (i in jsonArray.indices){
                val singleJsonObject = jsonArray.getJSONObject(i)
                list.add(
                        mapOf("qq" to singleJsonObject.getString("fuin"), "name" to singleJsonObject.getString("name"))
                )
            }
            return CommonResult(200, "", list)
        }else CommonResult(500, jsonObject.getString("message"))
    }

    override fun leaveMessage(qqEntity: QQEntity, qq: Long, content: String): String {
        val response = OkHttpClientUtils.post("https://mobile.qzone.qq.com/msgb/fcg_add_msg?g_tk=${qqEntity.getGtkP()}", OkHttpClientUtils.addForms(
                "res_uin", qq.toString(),
                "format", "json",
                "content", content,
                "opr_type", "add_comment"
        ), qqEntity.cookieWithQQZone())
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("code")){
            0 -> "留言成功"
            -3000 -> "留言失败，请更新QQ！"
            else -> "留言失败，${jsonObject.getString("message")}"
        }
    }

    override fun visitQZone(qqEntity: QQEntity, qq: Long): String {
        val response = OkHttpClientUtils.get("https://user.qzone.qq.com/$qq/", OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookieWithQQZone(),
                "user-agent", OkHttpClientUtils.PC_UA
        ))
        return if (response.code == 200){
            response.close()
            val gtk = qqEntity.getGtkP()
            val cookie = OkHttpClientUtils.getCookie(response)
            val visitResponse = OkHttpClientUtils.get("https://user.qzone.qq.com/proxy/domain/g.qzone.qq.com/fcg-bin/cgi_emotion_list.fcg?uin=$qq&loginUin=${qqEntity.qq}&rd=0.${BotUtils.randomNum(16)}&num=3&noflower=1&jsonpCallback=_Callback&format=jsonp&g_tk=$gtk&g_tk=$gtk",
                    OkHttpClientUtils.addCookie(qqEntity.getCookieWithQQZone() + cookie))
            if (visitResponse.code == 200) "访问${qq}的空间成功"
            else "访问${qq}的空间失败，请更新QQ！！"
        }else "访问空间失败！！请更新QQ！！"
    }

    override fun visitQZoneMobile(qqEntity: QQEntity, qq: Long): CommonResult<GroupMember> {
        val response = OkHttpClientUtils.get("https://h5.qzone.qq.com/mqzone/profile?hostuin=$qq", OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookieWithQQZone(),
                "user-agent", OkHttpClientUtils.MOBILE_UA
        ))
        val html = OkHttpClientUtils.getStr(response)
        if (!html.contains("title")) return CommonResult(500, "访问失败，请更新QQ！！")
        val jsonStr = BotUtils.regex("\"profile\":", "\\}", html) + "}"
        val jsonObject = JSON.parseObject(jsonStr)
        return CommonResult(200, "", GroupMember(
                nickName = jsonObject.getString("nickname"),
                country = jsonObject.getString("country"),
                province = jsonObject.getString("province"),
                city = jsonObject.getString("city"),
                qq = qq,
                userAge = jsonObject.getInteger("age")
        ))
    }
}
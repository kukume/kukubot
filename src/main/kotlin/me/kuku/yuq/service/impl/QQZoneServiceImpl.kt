package me.kuku.yuq.service.impl

import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.service.QQZoneService
import me.kuku.yuq.utils.OkHttpClientUtils

class QQZoneServiceImpl: QQZoneService {

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

    override fun myTalk(qqEntity: QQEntity): List<Map<String, String?>>? {
        val response = OkHttpClientUtils.get("https://mobile.qzone.qq.com/get_feeds?g_tk=${qqEntity.getGtkP()}&hostuin=${qqEntity.qq}&res_type=2&res_attach=&refresh_type=2&format=json",
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
}
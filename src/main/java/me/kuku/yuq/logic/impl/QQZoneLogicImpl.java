package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQZoneLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class QQZoneLogicImpl implements QQZoneLogic {
    @Override
    public List<Map<String, String>> friendTalk(QQLoginEntity qqLoginEntity) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("res_type", "0");
        map.put("res_attach", "0");
        map.put("refresh_type", "2");
        map.put("format", "json");
        map.put("attach_info", "");
        JSONObject jsonObject = OkHttpUtils.postJson("https://h5.qzone.qq.com/webapp/json/mqzone_feeds/getActiveFeeds?g_tk=" + qqLoginEntity.getGtkP(),
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("code") == 0){
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("vFeeds");
            List<Map<String, String>> list = new ArrayList<>();
            jsonArray.forEach(obj -> {
                JSONObject feedJsonObject = (JSONObject) obj;
                JSONObject commJsonObject = feedJsonObject.getJSONObject("comm");
                JSONObject userJsonObject = feedJsonObject.getJSONObject("userinfo").getJSONObject("user");
                Map<String, String> resultMap = new HashMap<>();
                resultMap.put("id", feedJsonObject.getJSONObject("id").getString("cellid"));
                resultMap.put("orgLikeKey", commJsonObject.getString("orglikekey"));
                resultMap.put("curLikeKey", commJsonObject.getString("curlikekey"));
                JSONObject likeJsonObject = feedJsonObject.getJSONObject("like");
                String isLiked = null;
                if (likeJsonObject != null) isLiked = likeJsonObject.getString("isliked");
                resultMap.put("like", isLiked);
                resultMap.put("qq", userJsonObject.getString("uin"));
                list.add(resultMap);
            });
            return list;
        }else return null;
    }

    @Override
    public List<Map<String, String>> talkByQQ(QQLoginEntity qqLoginEntity, Long qq) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("https://mobile.qzone.qq.com/get_feeds?g_tk=" + qqLoginEntity.getGtkP() + "&hostuin=" + qq + "&res_type=2&res_attach=&refresh_type=2&format=json",
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("code") == 0){
            JSONArray feedsJsonArray = jsonObject.getJSONObject("data").getJSONArray("vFeeds");
            List<Map<String, String>> list = new ArrayList<>();
            feedsJsonArray.forEach(obj -> {
                JSONObject feedJsonObject = (JSONObject) obj;
                Map<String, String> map = new HashMap<>();
                map.put("id", feedJsonObject.getJSONObject("id").getString("cellid"));
                map.put("qq", qq.toString());
                map.put("time", feedJsonObject.getJSONObject("comm").getString("time"));
                list.add(map);
            });
            return list;
        }else return null;
    }

    @Override
    public String forwardTalk(QQLoginEntity qqLoginEntity, String id, String qq, String text) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("res_id", id);
        map.put("res_uin", qq);
        map.put("format", "json");
        map.put("reason", text);
        map.put("res_type", "311");
        map.put("opr_type", "forward");
        map.put("operate", "1");
        JSONObject jsonObject = OkHttpUtils.postJson("https://mobile.qzone.qq.com/operation/operation_add?g_tk=" + qqLoginEntity.getGtkP(), map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("code") == 0) return "转发" + qq + "的说说成功！";
        else return "转发说说失败，请更新QQ！";
    }

    @Override
    public String publishTalk(QQLoginEntity qqLoginEntity, String text) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("opr_type", "publish_shuoshuo");
        map.put("res_uin", qqLoginEntity.getQq().toString());
        map.put("content", text);
        map.put("richval", "");
        map.put("lat", "0");
        map.put("lon", "0");
        map.put("lbsid", "0");
        map.put("issyncweibo", "0");
        map.put("format", "json");
        JSONObject jsonObject = OkHttpUtils.postJson("\"https://mobile.qzone.qq.com/mood/publish_mood?g_tk=" + qqLoginEntity.getGtkP(),
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("code") == 0) return "发说说成功！！";
        else return "发说说失败！！请更新QQ！！";
    }

    @Override
    public String removeTalk(QQLoginEntity qqLoginEntity, String id) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("opr_type", "delugc");
        map.put("res_type", "311");
        map.put("res_id", id);
        map.put("real_del", "0");
        map.put("res_uin", qqLoginEntity.getQq().toString());
        map.put("format", "json");
        JSONObject jsonObject = OkHttpUtils.postJson("https://mobile.qzone.qq.com/operation/operation_add?g_tk=" + qqLoginEntity.getGtkP(),
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("code") == 0) return "删除说说成功！";
        else return "删除说说失败，请更新QQ！";
    }

    @Override
    public String commentTalk(QQLoginEntity qqLoginEntity, String id, String qq, String text) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("content", text);
        map.put("busi_param", "");
        map.put("opr_type", "addcomment");
        JSONObject jsonObject = OkHttpUtils.postJson("https://mobile.qzone.qq.com/operation/publish_addcomment?g_tk=" + qqLoginEntity.getGtkP(),
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("code") == 0) return "评论" + qq + "的说说成功！！";
        else return "评论说说失败，请更新QQ";
    }

    @Override
    public String likeTalk(QQLoginEntity qqLoginEntity, Map<String, String> map) throws IOException {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("opuin", map.get("qq"));
        paramMap.put("unikey", map.get("orgLikeKey"));
        paramMap.put("curkey", map.get("curLikeKey"));
        paramMap.put("appid", "311");
        paramMap.put("opr_type", "like");
        paramMap.put("format", "purejson");
        JSONObject jsonObject = OkHttpUtils.postJson("https://h5.qzone.qq.com/proxy/domain/w.qzone.qq.com/cgi-bin/likes/internal_dolike_app?g_tk=" +
                qqLoginEntity.getGtkP(), paramMap, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("ret") == 0) return "赞" + map.get("qq") + "的说说成功！！";
        else return "赞说说失败，请更新QQ";
    }

    @Override
    public Result<List<Map<String, String>>> queryGroup(QQLoginEntity qqLoginEntity) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://user.qzone.qq.com/proxy/domain/r.qzone.qq.com/cgi-bin/tfriend/qqgroupfriend_extend.cgi?uin=%d&rd=0.%s&cntperpage=0&fupdate=1&g_tk=%s&g_tk=%s",
                qqLoginEntity.getQq(), BotUtils.randomNum(16), qqLoginEntity.getGtkP(), qqLoginEntity.getGtkP()),
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("code") == 0){
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("group");
            List<Map<String, String>> list = new ArrayList<>();
            for (Object obj: jsonArray){
                JSONObject singleJsonObject = (JSONObject) obj;
                Map<String, String> map = new HashMap<>();
                map.put("group", singleJsonObject.getString("groupcode"));
                map.put("groupName", singleJsonObject.getString("groupname"));
                list.add(map);
            }
            return Result.success(list);
        }else return Result.failure(jsonObject.getString("message"), null);
    }

    @Override
    public Result<List<Map<String, String>>> queryGroupMember(QQLoginEntity qqLoginEntity, String group) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://user.qzone.qq.com/proxy/domain/r.qzone.qq.com/cgi-bin/tfriend/qqgroupfriend_groupinfo.cgi?uin=%s&gid=%s&fupdate=1&type=1&g_tk=%s&g_tk=%s",
                qqLoginEntity.getQq(), group, qqLoginEntity.getGtkP(), qqLoginEntity.getGtkP()),
                OkHttpUtils.addHeaders(qqLoginEntity.getCookieWithQQZone(), "https://user.qzone.qq.com/" + qqLoginEntity.getQq() + "/myhome/friends/ofpmd"));
        if (jsonObject.getInteger("code") == 0){
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("friends");
            List<Map<String, String>> list = new ArrayList<>();
            for (Object obj: jsonArray){
                JSONObject singleJsonObject = (JSONObject) obj;
                Map<String, String> map = new HashMap<>();
                map.put("qq", singleJsonObject.getString("fuin"));
                map.put("name", singleJsonObject.getString("name"));
                list.add(map);
            }
            return Result.success(list);
        }else return Result.failure(jsonObject.getString("message"), null);
    }

    @Override
    public String leaveMessage(QQLoginEntity qqLoginEntity, Long qq, String content) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("res_uin", qq.toString());
        map.put("format", "json");
        map.put("content", content);
        map.put("opr_type", "add_comment");
        JSONObject jsonObject = OkHttpUtils.postJson("https://mobile.qzone.qq.com/msgb/fcg_add_msg?g_tk=" + qqLoginEntity.getGtkP(),
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        switch (jsonObject.getInteger("code")){
            case 0: return "留言成功！！";
            case -3000: return "留言失败，请更新QQ！";
            default: return "留言失败，" + jsonObject.getString("message");
        }
    }

    @Override
    public String visitQZone(QQLoginEntity qqLoginEntity, Long qq) throws IOException {
        Response response = OkHttpUtils.get("https://user.qzone.qq.com/" + qq + "/",
                OkHttpUtils.addHeaders(qqLoginEntity.getCookieWithQQZone(), null, UA.PC));
        if (response.code() == 200){
            response.close();
            String gtk = qqLoginEntity.getGtkP();
            String cookie = OkHttpUtils.getCookie(response);
            Response visitResponse = OkHttpUtils.get(String.format("https://user.qzone.qq.com/proxy/domain/g.qzone.qq.com/fcg-bin/cgi_emotion_list.fcg?uin=%s&loginUin=%s&rd=0.%s&num=3&noflower=1&jsonpCallback=_Callback&format=jsonp&g_tk=%s&g_tk=%s",
                    qq, qqLoginEntity.getQq(), BotUtils.randomNum(16), gtk, gtk),
                    OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone() + cookie));
            visitResponse.close();
            if (visitResponse.code() == 200) return "访问" + qq + "的空间成功！";
            else return "访问" + qq + "的空间失败，请更新QQ！";
        }else return "访问空间失败，请更新QQ！";
    }
}

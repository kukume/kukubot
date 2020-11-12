package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.NeTeaseEntity;
import me.kuku.yuq.logic.NeTeaseLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.utils.AESUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NeTeaseLogicImpl implements NeTeaseLogic {
    private final String referer = "https://music.163.com/";
    private final String vi = "0102030405060708";
    private final String nonce = "0CoJUm6Qyw8W8jud";
    private final String secretKey = "TA3YiYCfY2dDJQgg";
    private final String encSecKey = "84ca47bca10bad09a6b04c5c927ef077d9b9f1e37098aa3eac6ea70eb59df0aa28b691b7e75e4f1f9831754919ea784c8f74fbfadf2898b0be17849fd656060162857830e241aba44991601f137624094c114ea8d17bce815b0cd4e5b8e2fbaba978c6d1d14dc3d1faf852bdd28818031ccdaaa13a6018e1024e2aae98844210";
    private final String UA = me.kuku.yuq.pojo.UA.PC.getValue();
    private final String api = "https://netease.kuku.me";

    private String aesEncode(String secretData, String secret){
        return AESUtils.encrypt(secretData, secret, vi);
    }

    private Map<String, String> prepare(Map<String, String> map){
        String param = aesEncode(JSON.toJSONString(map), nonce);
        param = aesEncode(param, secretKey);
        HashMap<String, String> resultMap = new HashMap<>();
        resultMap.put("params", param);
        resultMap.put("encSecKey", encSecKey);
        return resultMap;
    }

    @Override
    public Result<NeTeaseEntity> loginByPhone(String username, String password) throws IOException {
        Response response = OkHttpUtils.get(api + "/login/cellphone?phone=" + username + "&md5_password=" + password);
        JSONObject jsonObject = OkHttpUtils.getJson(response);
        if (jsonObject.getInteger("code") == 200){
            String cookie = OkHttpUtils.getCookie(response);
            return Result.success(new NeTeaseEntity(
                    OkHttpUtils.getCookie(cookie, "MUSIC_U"), OkHttpUtils.getCookie(cookie, "__csrf")
            ));
        }else return Result.failure(jsonObject.getString("msg"), null);
    }

    @Override
    public String sign(NeTeaseEntity neTeaseEntity) throws IOException {
        OkHttpUtils.get(api + "/daily_signin?type=1").close();
        JSONObject jsonObject = OkHttpUtils.getJson(api + "/daily_signin?type=0");
        Integer code = jsonObject.getInteger("code");
        switch (code){
            case 200: return "签到成功！！";
            case -2: return "今日已签到";
            default: return jsonObject.getString("msg");
        }
    }

    private Result<List<String>> recommend(NeTeaseEntity neTeaseEntity) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("csrf_token", neTeaseEntity.get__csrf());
        JSONObject jsonObject = OkHttpUtils.postJson("https://music.163.com/weapi/v1/discovery/recommend/resource",
                prepare(map), OkHttpUtils.addHeaders(neTeaseEntity.getCookie(), referer, UA));
        int code = jsonObject.getInteger("code");
        if (code == 200){
            JSONArray jsonArray = jsonObject.getJSONArray("recommend");
            List<String> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++){
                JSONObject singleJsonObject = jsonArray.getJSONObject(i);
                list.add(singleJsonObject.getString("id"));
            }
            return Result.success(list);
        }else if (code == 301) return Result.failure("您的网易cookie已失效，请重新登录！！", null);
        else return Result.failure(jsonObject.getString("msg"), null);
    }

    private JSONArray getSongId(NeTeaseEntity neTeaseEntity, String playListId) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("id", playListId);
        map.put("n", "1000");
        map.put("csrf_token", "");
        JSONObject jsonObject = OkHttpUtils.postJson("https://music.163.com/weapi/v3/playlist/detail?csrf_token=",
                prepare(map), OkHttpUtils.addHeaders(neTeaseEntity.getCookie(), referer, UA));
        return jsonObject.getJSONObject("playlist").getJSONArray("trackIds");
    }

    @Override
    public String listeningVolume(NeTeaseEntity neTeaseEntity) throws IOException {
        Result<List<String>> recommend = recommend(neTeaseEntity);
        if (recommend.getCode().equals(200)){
            List<String> playList = recommend.getData();
            JSONArray ids = new JSONArray();
            while (ids.size() < 310){
                JSONArray songIds = getSongId(neTeaseEntity, playList.get((int) (Math.random() * playList.size())));
                int k = 0;
                while (ids.size() < 310 && k < songIds.size()){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("download", 0);
                    jsonObject.put("end", "playend");
                    jsonObject.put("id", songIds.getJSONObject(k).getInteger("id"));
                    jsonObject.put("sourceId", "");
                    jsonObject.put("time", 240);
                    jsonObject.put("type", "song");
                    jsonObject.put("wifi", 0);
                    JSONObject totalJsonObject = new JSONObject();
                    totalJsonObject.put("json", jsonObject);
                    totalJsonObject.put("action", "play");
                    ids.add(totalJsonObject);
                    k++;
                }
            }
            Map<String, String> map = new HashMap<>();
            map.put("logs", ids.toString());
            JSONObject jsonObject = OkHttpUtils.postJson("http://music.163.com/weapi/feedback/weblog",
                    prepare(map), OkHttpUtils.addHeaders(neTeaseEntity.getCookie(), referer, UA));
            if (jsonObject.getInteger("code").equals(200)) return "刷每日300听歌量成功！！";
            else return jsonObject.getString("message");
        }else return recommend.getMessage();
    }
}

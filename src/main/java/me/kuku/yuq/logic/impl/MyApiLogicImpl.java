package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.pojo.InstagramPojo;
import me.kuku.yuq.pojo.TwitterPojo;
import me.kuku.yuq.utils.OkHttpUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class MyApiLogicImpl implements MyApiLogic {

    private final String api = "https://api.kuku.me";

    @Override
    public List<TwitterPojo> findTwitterIdByName(String name) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(api + "/twitter/id?name=" + URLEncoder.encode(name, "utf-8"));
        if (jsonObject.getInteger("code") == 200){
            return jsonObject.getJSONArray("data").toJavaList(TwitterPojo.class);
        }else return null;
    }

    @Override
    public List<TwitterPojo> findTweetsById(Long id) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(api + "/twitter/tweets?id=" + id);
        if (jsonObject.getInteger("code") == 200){
            return jsonObject.getJSONArray("data").toJavaList(TwitterPojo.class);
        }else return null;
    }

    @Override
    public List<InstagramPojo> findInsIdByName(String name) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(api + "/instagram/id?name=" + URLEncoder.encode(name, "utf-8"));
        if (jsonObject.getInteger("code") == 200){
            return jsonObject.getJSONArray("data").toJavaList(InstagramPojo.class);
        }else return null;
    }

    @Override
    public List<InstagramPojo> findInsPicById(String name, Long id, Integer page) throws IOException {
        if (page == null) page = 1;
        JSONObject jsonObject = OkHttpUtils.getJson(api + "/instagram/pic?id=" + id + "&name=" + name + "&page=" + page);
        if (jsonObject.getInteger("code") == 200){
            return jsonObject.getJSONArray("data").toJavaList(InstagramPojo.class);
        }else return null;
    }
}

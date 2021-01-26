package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.logic.AILogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.MD5Utils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.FormBody;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@SuppressWarnings("unused")
public class QQAILogicImpl implements AILogic {
    @Inject
    private ConfigService configService;

    private String getSign(Map<String, String> map, String appKey){
        TreeMap<String, String> treeMap = new TreeMap<>(map);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry: treeMap.entrySet()){
            try {
                sb.append(String.format("%s=%s&", entry.getKey(), URLEncoder.encode(entry.getValue(), "utf-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        sb.append("app_key=").append(appKey);
        return MD5Utils.toMD5(sb.toString()).toUpperCase();
    }

    private FormBody addParams(Map<String, String> otherParams){
        ConfigEntity configEntity1 = configService.findByType("qqAIAppId");
        ConfigEntity configEntity2 = configService.findByType("qqAIAppKey");
        String appId;
        String appKey;
        if (configEntity1 == null || configEntity2 == null) {
            appId = "";
            appKey = "";
        }else{
            appId = configEntity1.getContent();
            appKey = configEntity2.getContent();
        }
        Map<String, String> map = new HashMap<>();
        map.put("app_id", appId);
        map.put("time_stamp", String.valueOf(new Date().getTime() / 1000));
        map.put("nonce_str", BotUtils.randomStr(16));
        FormBody.Builder builder = new FormBody.Builder();
        map.putAll(otherParams);
        String sign = getSign(map, appKey);
        map.put("sign", sign);
        for (Map.Entry<String, String> entry: map.entrySet()){
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private String urlToBase64(String imageUrl) throws IOException {
        byte[] bytes = OkHttpUtils.getBytes(imageUrl);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public boolean pornIdentification(String imageUrl) throws IOException {
        String baseStr = urlToBase64(imageUrl);
        Map<String, String> map = new HashMap<>();
        map.put("image", baseStr);
        JSONObject jsonObject = OkHttpUtils.postJson("https://api.ai.qq.com/fcgi-bin/vision/vision_porn",
                addParams(map));
        if (jsonObject.getInteger("ret").equals(0)){
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("tag_list");
//            Integer normal = jsonArray.getJSONObject(0).getInteger("tag_confidence");
//            Integer hot = jsonArray.getJSONObject(1).getInteger("tag_confidence");
            Integer porn = jsonArray.getJSONObject(2).getInteger("tag_confidence");
            return porn > 83 /*|| hot > normal*/;
        }else return false;
    }

    @Override
    public String generalOCR(String imageUrl) throws IOException {
        String baseStr = urlToBase64(imageUrl);
        Map<String, String> map = new HashMap<>();
        map.put("image", baseStr);
        JSONObject jsonObject = OkHttpUtils.postJson("https://api.ai.qq.com/fcgi-bin/ocr/ocr_generalocr", addParams(map));
        if (jsonObject.getInteger("ret").equals(0)){
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("item_list");
            if (jsonArray.size() == 0) return "啥文字也没有识别到";
            StringBuilder sb = new StringBuilder();
            for (int i = 0 ; i < jsonArray.size(); i++){
                JSONObject singleJsonObject = jsonArray.getJSONObject(i);
                sb.append(singleJsonObject.getString("itemstring")).append("\n");
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        }else return jsonObject.getString("msg");
    }

    @Override
    public String textChat(String question, String session) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("session", session);
        map.put("question", question);
        JSONObject jsonObject = OkHttpUtils.postJson("https://api.ai.qq.com/fcgi-bin/nlp/nlp_textchat",
                addParams(map));
        Integer ret = jsonObject.getInteger("ret");
        switch (ret){
            case 0: return jsonObject.getJSONObject("data").getString("answer");
            case 16385: return "没有填入appid， 请联系机器人主人填写";
            case 16394: return "没有发现匹配的答案";
            default: jsonObject.getString("msg");
        }
        return null;
    }

    @Override
    public Result<byte[]> voiceSynthesis(String text) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("speaker", "5");
        map.put("format", "2");
        map.put("volume", "0");
        map.put("speed", "100");
        map.put("text", text);
        map.put("aht", "0");
        map.put("apc", "58");
        JSONObject jsonObject = OkHttpUtils.postJson("https://api.ai.qq.com/fcgi-bin/aai/aai_tts", addParams(map));
        if (jsonObject.getInteger("ret") == 0){
            String base64 = jsonObject.getJSONObject("data").getString("speech");
            return Result.success(Base64.getDecoder().decode(base64));
        }else return Result.failure(jsonObject.getString("msg"), null);
    }

    @Override
    public String voiceIdentify(String voiceUrl) throws IOException {
        return null;
    }
}

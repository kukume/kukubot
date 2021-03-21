package top.cubik65536.yuq.logic.impl;

import top.cubik65536.yuq.logic.ChatLogic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ChatLogicImpl
 * me.kuku.yuq.logic.impl
 * kukubot
 * <p>
 * Created by Cubik65536 on 2021-02-20.
 * Copyright © 2020-2021 Cubik Inc. All rights reserved.
 * <p>
 * Description: QQ智能聊天机器人API实现
 * History:
 * 1. 2021-02-20 [Cubik65536]: Create file ChatLogicImpl;
 * 2. 2021-02-20 [Cubik65536]: 实现青云客和海知智能聊天机器人方法;
 */

@SuppressWarnings("unused")
public class ChatLogicImpl implements ChatLogic {
    @Override
    public String getQingYunKe(String key, String msg) throws Exception {
        key= URLEncoder.encode(key, "UTF-8");
        msg= URLEncoder.encode(msg, "UTF-8");
        String generalUrl="http://api.qingyunke.com/api.php"+"?key="+key+"&"+"appid=0"+"&"+"msg="+msg;
        URL url = new URL(generalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String info : headers.keySet()) {
            System.err.println(info + "--->" + headers.get(info));
        }
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        return result;
    }

    @Override
    public String getHaiZhi(String key, String msg) throws Exception {
        String generalUrl = "http://api.ruyi.ai/v1/message"+"?q="+msg+"&"+"app_key="+key+"&"+"user_id="+ UUID.randomUUID().toString();
        URL url = new URL(generalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String info : headers.keySet()) {
            System.err.println(info + "--->" + headers.get(info));
        }
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        return result;
    }
}

package me.kuku.simbot.utils;

import com.alibaba.fastjson.JSONObject;
import me.kuku.utils.OkHttpUtils;

public class BotUtils {

	public static String shortUrl(String url){
		// http://www.uc4.cn/
		try {
			if (!url.startsWith("http") && !url.startsWith("https")) url = "http://" + url;
			JSONObject jsonObject = OkHttpUtils.postJson("https://links.iyou.eu.org/",
					OkHttpUtils.addJson("{\"url\":\"" + url + "\"}"));
			if (jsonObject.getInteger("status") == 200){
				return "https://links.iyou.eu.org" + jsonObject.getString("key");
			}else return url;
		} catch (Exception e) {
			return url;
		}
	}
}

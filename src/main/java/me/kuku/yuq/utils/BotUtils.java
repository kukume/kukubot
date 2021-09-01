package me.kuku.yuq.utils;

import com.IceCreamQAQ.Yu.util.OkHttpWebImpl;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.mirai.MiraiBot;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.QqUtils;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.entity.QqLoginEntity;
import okhttp3.Cookie;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BotUtils {

	private static OkHttpWebImpl web;
	private static MiraiBot miraiBot;

	public static void setWeb(OkHttpWebImpl web) {
		BotUtils.web = web;
	}

	public static void setMiraiBot(MiraiBot miraiBot) {
		BotUtils.miraiBot = miraiBot;
	}

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

	public static QqLoginEntity getQQLoginEntity(){
		try {
			ConcurrentHashMap<String, Map<String, Cookie>> map = web.getDomainMap();
			Map<String, Cookie> qunMap = map.get("qun.qq.com");
			String groupPsKey = qunMap.get("p_skey").value();
			Map<String, Cookie> qqMap = map.get("qq.com");
			String sKey = qqMap.get("skey").value();
			Map<String, Cookie> qZoneMap = map.get("qzone.qq.com");
			String psKey = qZoneMap.get("p_skey").value();
			return new QqLoginEntity(null, QqEntity.Companion.getInstance(miraiBot.getId()), sKey, psKey, miraiBot.superKey,
					QqUtils.getToken(miraiBot.superKey).toString(), "", groupPsKey);
		}catch (Exception e){
			return new QqLoginEntity();
		}
	}

	public static void sendMessage(QqEntity qqEntity, Message message){
		long qq = qqEntity.getQq();
		Set<GroupEntity> set = qqEntity.getGroups();
		if (set.size() != 0) {
			for (GroupEntity groupEntity : set) {
				long group = groupEntity.getGroup();
				FunKt.getYuq().getGroups().get(group).get(qq).sendMessage(message);
				break;
			}
		}else FunKt.getYuq().getFriends().get(qq).sendMessage(message);
	}

	public static void sendMessage(QqEntity qqEntity, String message){
		sendMessage(qqEntity, Message.Companion.toMessage(message));
	}

	public static void sendMessage(long group, String msg){
		sendMessage(group, Message.Companion.toMessage(msg));
	}

	public static void sendMessage(long group, Message message){
		FunKt.getYuq().getGroups().get(group).sendMessage(message);
	}
}

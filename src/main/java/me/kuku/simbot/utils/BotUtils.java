package me.kuku.simbot.utils;

import catcode.Neko;
import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.results.AuthInfo;
import love.forte.simbot.api.message.results.GroupList;
import love.forte.simbot.api.message.results.SimpleGroupInfo;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.QqUtils;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public static void sendPrivateMsg(Set<GroupEntity> groupSet, long qq, String msg){
		BotManager botManager = SpringUtils.getBean(BotManager.class);
		List<Bot> botList = botManager.getBots();
		out:for (Bot bot : botList) {
			GroupList groupList = bot.getSender().GETTER.getGroupList();
			for (SimpleGroupInfo simpleGroupInfo : groupList) {
				long group = simpleGroupInfo.getGroupCodeNumber();
				for (GroupEntity groupEntity : groupSet) {
					if (groupEntity.getGroup() == group){
						bot.getSender().SENDER.sendPrivateMsg(qq, group, msg);
						break out;
					}
				}
			}
		}
	}

	public static void sendGroupMsg(long group, String msg){
		BotManager botManager = SpringUtils.getBean(BotManager.class);
		List<Bot> botList = botManager.getBots();
		out:for (Bot bot : botList) {
			GroupList groupList = bot.getSender().GETTER.getGroupList();
			for (SimpleGroupInfo simpleGroupInfo : groupList) {
				long joinGroup = simpleGroupInfo.getGroupCodeNumber();
				if (joinGroup == group){
					bot.getSender().SENDER.sendGroupMsg(group, msg);
					break out;
				}
			}
		}
	}

	public static QqLoginEntity getBotQqLoginEntity(Getter getter){
		getter.getGroupNoteList(getter.getGroupList().getResults().get(0).getGroupCodeNumber());
		Map<String, String> map = getter.getAuthInfo().getAuths().toMap();
		String qq = map.get("uin");
		String sKey = cookieDecode(map.get("sKey:data"));
		String groupPsKey = cookieDecode(map.get("psKey:qun.qq.com:data"));
		String psKey = cookieDecode(map.get("psKey:qzone.qq.com:data"));
		String superKey = cookieDecode(map.get("superKey"));
		Long superToken = QqUtils.getToken(superKey);
		return QqLoginEntity.Companion.getInstance(QqEntity.Companion.getInstance(Long.parseLong(qq)), sKey, psKey, superKey, superToken, groupPsKey);
	}

	private static String cookieDecode(String str){
		String[] arr = str.split(",");
		int len = arr.length;
		byte[] bytes = new byte[len];
		for (int i = 0; i < len; i++){
			bytes[i] = (byte) Integer.parseInt(arr[i]);
		}
		return new String(bytes);
	}

	public static Long getAt(MessageContent messageContent){
		List<Neko> atList = messageContent.getCats("at");
		if (atList.size() == 0) return null;
		String s = atList.get(0).get("code");
		if (s == null) return null;
		return Long.parseLong(s);
	}
}

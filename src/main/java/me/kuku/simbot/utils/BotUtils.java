package me.kuku.simbot.utils;

import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.api.message.results.GroupList;
import love.forte.simbot.api.message.results.SimpleGroupInfo;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.utils.OkHttpUtils;

import java.util.List;
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
					if (groupEntity.getGroup().equals(group)){
						bot.getSender().SENDER.sendPrivateMsg(qq, group, msg);
						break out;
					}
				}
			}
		}
	}

	public static void sendGroupMsg(){

	}
}

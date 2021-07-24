package me.kuku.simbot.utils;

import catcode.Neko;
import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.results.AuthInfo;
import love.forte.simbot.api.message.results.GroupList;
import love.forte.simbot.api.message.results.SimpleGroupInfo;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.QqUtils;

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
		AuthInfo.Auths auths = getter.getAuthInfo().getAuths();
		String qq = auths.get("uin");
		String sKey = auths.get("sKey");
		String groupPsKey = auths.get("psKey:qun.qq.com");
		String psKey = auths.get("psKey:qzone.qq.com");
		String superKey = auths.get("superKey");
		Long superToken = QqUtils.getToken(superKey);
		return new QqLoginEntity(new QqEntity(Long.parseLong(qq)), sKey, psKey, superKey, superToken, groupPsKey);
	}

	public static Long getAt(MessageContent messageContent){
		List<Neko> atList = messageContent.getCats("at");
		if (atList.size() == 0) return null;
		String s = atList.get(0).get("code");
		if (s == null) return null;
		return Long.parseLong(s);
	}
}

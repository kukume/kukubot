package me.kuku.simbot.event;

import love.forte.simbot.annotation.Listen;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Sender;
import me.kuku.utils.MyUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GroupEvent {

	private final Map<Long, String> lastMessage = new ConcurrentHashMap<>();
	private final Map<Long, Long> lastQQ = new ConcurrentHashMap<>();
	private final Map<Long, String> lastRepeatMessage = new ConcurrentHashMap<>();

	@Listen(GroupMsg.class)
	public void repeat(GroupMsg groupMsg, Sender sender){
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		long group = groupMsg.getGroupInfo().getGroupCodeNumber();
		String nowMsg = groupMsg.getMsg();
		if (lastMessage.containsKey(group)){
			String oldMsg = lastMessage.get(group);
			if (equals(nowMsg, oldMsg) &&
					!nowMsg.equals(lastRepeatMessage.get(group)) &&
					lastQQ.get(group) != qq){
				lastRepeatMessage.put(group, nowMsg);
				sender.sendGroupMsg(group, nowMsg);
			}
		}
		lastMessage.put(group, nowMsg);
		lastQQ.put(group, qq);
	}

	private boolean equals(String nowMsg, String oldMsg){
		if (nowMsg == null || oldMsg == null) return false;
		if (nowMsg.contains("CAT:image") && oldMsg.contains("CAT:image")){
			String nowId = MyUtils.regex("id=", ",", nowMsg);
			String oldId = MyUtils.regex("id=", ",", oldMsg);
			if (nowId == null || oldId == null) return false;
			return nowId.equals(oldId);
		}
		return nowMsg.equals(oldMsg);
	}

}

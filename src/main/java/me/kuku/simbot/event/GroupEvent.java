package me.kuku.simbot.event;

import catcode.Neko;
import love.forte.simbot.annotation.Listen;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GroupEvent {

	@Autowired
	private MessageContentBuilderFactory messageContentBuilderFactory;

	private final Map<Long, List<Neko>> lastMessage = new ConcurrentHashMap<>();
	private final Map<Long, Long> lastQQ = new ConcurrentHashMap<>();
	private final Map<Long, List<Neko>> lastRepeatMessage = new ConcurrentHashMap<>();

	@Listen(GroupMsg.class)
	public void repeat(GroupMsg groupMsg, Sender sender){
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		long group = groupMsg.getGroupInfo().getGroupCodeNumber();
		List<Neko> nowNeko = groupMsg.getMsgContent().getCats();
		if (lastMessage.containsKey(group)){
			List<Neko> oldNeko = lastMessage.get(group);
			if (nowNeko.equals(oldNeko) &&
					!nowNeko.equals(lastRepeatMessage.get(group)) &&
					lastQQ.get(group) != qq){
				lastRepeatMessage.put(group, nowNeko);
				StringBuilder sb = new StringBuilder();
				nowNeko.forEach(sb::append);
				sender.sendGroupMsg(group, sb.toString());
			}
		}
		lastMessage.put(group, nowNeko);
		lastQQ.put(group, qq);
	}

}

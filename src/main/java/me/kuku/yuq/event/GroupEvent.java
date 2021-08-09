package me.kuku.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.GroupService;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventListener
public class GroupEvent {

	@Inject
	private GroupService groupService;

	private final Map<Long, String> lastMessage = new ConcurrentHashMap<>();
	private final Map<Long, Long> lastQQ = new ConcurrentHashMap<>();
	private final Map<Long, String> lastRepeatMessage = new ConcurrentHashMap<>();

	@Event(weight = Event.Weight.low)
	public void repeat(GroupMessageEvent e){
		long group = e.getGroup().getId();
		GroupEntity groupEntity = groupService.findByGroup(group);
		if (Boolean.TRUE.equals(groupEntity.getRepeat())) {
			long qq = e.getSender().getId();
			String nowMsg = e.getMessage().getCodeStr();
			if (lastMessage.containsKey(group)) {
				String oldMsg = lastMessage.get(group);
				if (oldMsg.equals(nowMsg) &&
						!nowMsg.equals(lastRepeatMessage.get(group))
						&& lastQQ.get(group) != qq) {
					lastRepeatMessage.put(group, nowMsg);
					e.getGroup().sendMessage(e.getMessage());
				}
			}
			lastMessage.put(group, nowMsg);
			lastQQ.put(group, qq);
		}
	}
}

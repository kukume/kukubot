package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.MsgInterceptContext;
import love.forte.simbot.listener.MsgInterceptor;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.service.GroupService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OnOrOffInterceptor implements MsgInterceptor {

	@Resource
	private GroupService groupService;

	@NotNull
	@Override
	public InterceptionType intercept(@NotNull MsgInterceptContext context) {
		MsgGet msgGet = context.getMsgGet();
		Long group = null;
		if (msgGet instanceof GroupMsg) {
			GroupMsg groupMsg = (GroupMsg) msgGet;
			String msg = groupMsg.getMsg();
			if ("kukubot开".equals(msg) || "kukubot关".equals(msg)) return InterceptionType.PASS;
			group = groupMsg.getGroupInfo().getGroupCodeNumber();
		}
		if (group != null){
			GroupEntity groupEntity = groupService.findByGroup(group);
			if (groupEntity == null || !Boolean.TRUE.equals(groupEntity.getStatus()))
				return InterceptionType.INTERCEPT;
		}
		return InterceptionType.PASS;
	}
}

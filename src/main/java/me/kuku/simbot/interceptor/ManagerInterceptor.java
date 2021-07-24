package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ScopeContext;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.service.GroupService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ManagerInterceptor extends GroupMsgGroupedInterceptor {

	@Value("${kukubot.master}")
	private Long master;
	@Resource
	private GroupService groupService;

	@Override
	protected InterceptionType doIntercept(GroupMsg groupMsg, BotSender botSender, ScopeContext scopeContext) {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		long group = groupMsg.getGroupInfo().getGroupCodeNumber();
		if (qq == master){
			GroupEntity groupEntity = groupService.findByGroup(group);
			scopeContext.set("groupEntity", groupEntity);
			return InterceptionType.PASS;
		}else return InterceptionType.INTERCEPT;
	}

	@NotNull
	@Override
	protected String[] getGroupRange() {
		return new String[]{"manager"};
	}
}
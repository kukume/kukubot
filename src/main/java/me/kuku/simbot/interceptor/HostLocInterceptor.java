package me.kuku.simbot.interceptor;

import catcode.StringTemplate;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ScopeContext;
import me.kuku.simbot.entity.HostLocEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.service.HostLocService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostLocInterceptor extends GroupMessageInterceptor{

	@Autowired
	private HostLocService hostLocService;
	@Autowired
	private StringTemplate stringTemplate;

	@Override
	protected InterceptionType doIntercept(GroupMsg groupMsg, BotSender botSender, ScopeContext scopeContext) {
		QqEntity qqEntity = (QqEntity) scopeContext.get("qq");
		HostLocEntity hostLocEntity = hostLocService.findByQqEntity(qqEntity);
		if (hostLocEntity == null){
			botSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(groupMsg.getAccountInfo().getAccountCode()) + "您还未绑定HostLoc账号，请先绑定HostLoc账号！");
			return InterceptionType.INTERCEPT;
		}else {
			scopeContext.set("hostLocEntity", hostLocEntity);
			return InterceptionType.PASS;
		}

	}

	@NotNull
	@Override
	protected String[] getGroupRange() {
		return new String[]{"hostLoc"};
	}
}

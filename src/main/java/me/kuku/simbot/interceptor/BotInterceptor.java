package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ScopeContext;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.simbot.utils.BotUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class BotInterceptor extends GroupMsgGroupedInterceptor {


	@Override
	protected InterceptionType doIntercept(GroupMsg groupMsg, BotSender botSender, ScopeContext scopeContext) {
		QqLoginEntity qqLoginEntity = BotUtils.getBotQqLoginEntity(botSender.GETTER);
		scopeContext.set("qqLoginEntity", qqLoginEntity);
		return InterceptionType.PASS;
	}

	@NotNull
	@Override
	protected String[] getGroupRange() {
		return new String[]{"bot"};
	}
}

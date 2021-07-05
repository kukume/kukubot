package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.core.intercept.FixedRangeGroupedListenerInterceptor;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.ListenerInterceptContext;
import love.forte.simbot.listener.ScopeContext;
import me.kuku.simbot.utils.SpringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PrivateMessageInterceptor extends FixedRangeGroupedListenerInterceptor {

	@NotNull
	protected InterceptionType doIntercept(@NotNull ListenerInterceptContext context, @Nullable String group) {
		MsgGet msgGet = context.getMsgGet();
		if (msgGet instanceof PrivateMsg) {
			PrivateMsg privateMsg = (PrivateMsg) msgGet;
			ScopeContext scopeContext = context.getListenerContext().getContext(ListenerContext.Scope.EVENT_INSTANT);
			BotManager botManager = SpringUtils.getBean(BotManager.class);
			BotSender botSender = botManager.getBot(privateMsg.getBotInfo().getBotCode()).getSender();
			return doIntercept(privateMsg, botSender, scopeContext);
		}
		return InterceptionType.INTERCEPT;
	}

	protected abstract InterceptionType doIntercept(PrivateMsg privateMsg, BotSender botSender, ScopeContext scopeContext);
}
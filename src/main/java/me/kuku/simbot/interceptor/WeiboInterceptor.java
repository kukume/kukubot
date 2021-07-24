package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ScopeContext;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.WeiboEntity;
import me.kuku.simbot.service.QqService;
import me.kuku.simbot.service.WeiboService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeiboInterceptor extends GroupMsgGroupedInterceptor {

	@Autowired
	private WeiboService weiboService;
	@Autowired
	private QqService qqService;

	@Override
	protected InterceptionType doIntercept(GroupMsg groupMsg, BotSender botSender, ScopeContext scopeContext) {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		QqEntity qqEntity = qqService.findByQq(qq);
		WeiboEntity weiboEntity = weiboService.findByQqEntity(qqEntity);
		if (weiboEntity == null) {
			botSender.SENDER.sendGroupMsg(groupMsg.getGroupInfo().getGroupCodeNumber(), "您没有绑定微博，请私聊机器人绑定！");
			return InterceptionType.INTERCEPT;
		}
		else {
			scopeContext.set("weiboEntity", weiboEntity);
			return InterceptionType.PASS;
		}
	}

	@NotNull
	@Override
	protected String[] getGroupRange() {
		return new String[]{"weibo"};
	}
}

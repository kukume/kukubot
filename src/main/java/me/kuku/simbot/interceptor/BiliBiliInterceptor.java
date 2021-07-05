package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ScopeContext;
import me.kuku.simbot.entity.BiliBiliEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.service.BiliBiliService;
import me.kuku.simbot.service.QqService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BiliBiliInterceptor extends GroupMessageInterceptor {

	@Autowired
	private BiliBiliService biliBiliService;
	@Autowired
	private QqService qqService;

	@Override
	protected InterceptionType doIntercept(GroupMsg groupMsg, BotSender botSender, ScopeContext scopeContext) {
		long group = groupMsg.getAccountInfo().getAccountCodeNumber();
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		QqEntity qqEntity = qqService.findByQq(qq);
		BiliBiliEntity biliBiliEntity = biliBiliService.findByQqEntity(qqEntity);
		if (biliBiliEntity == null){
			botSender.SENDER.sendGroupMsg(groupMsg, "您没有绑定哔哩哔哩账号，请先进行绑定！");
			return InterceptionType.INTERCEPT;
		}else {
			scopeContext.set("biliBiliEntity", biliBiliEntity);
			return InterceptionType.PASS;
		}

	}

	@NotNull
	@Override
	protected String[] getGroupRange() {
		return new String[]{"biliBili"};
	}
}

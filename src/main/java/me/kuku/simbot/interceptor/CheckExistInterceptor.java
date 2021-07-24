package me.kuku.simbot.interceptor;

import catcode.StringTemplate;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.BotSender;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ScopeContext;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.service.QqService;
import me.kuku.simbot.utils.SpringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

public abstract class CheckExistInterceptor<T> extends GroupMsgGroupedInterceptor {
	@Override
	protected InterceptionType doIntercept(GroupMsg groupMsg, BotSender botSender, ScopeContext scopeContext) {
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		Class<T> clazz = (Class<T>) parameterizedType.getActualTypeArguments()[0];
		T t = SpringUtils.getBean(clazz);
		StringTemplate stringTemplate = SpringUtils.getBean(StringTemplate.class);
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		try {
			Method method = clazz.getMethod("findByQqEntity", QqEntity.class);
			QqService qqService = SpringUtils.getBean(QqService.class);
			QqEntity qqEntity = qqService.findByQq(qq);
			Object o = method.invoke(t, qqEntity);
			if (o == null){
				botSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + notExistMsg());
				return InterceptionType.INTERCEPT;
			}else {
				String name = o.getClass().getSimpleName();
				name = String.valueOf(name.charAt(0)).toLowerCase() + name.substring(1);
				scopeContext.set(name, o);
				return InterceptionType.PASS;
			}
		} catch (Exception e) {
			botSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + "出现异常了，异常信息为：" + e.getMessage());
			return InterceptionType.INTERCEPT;
		}
	}

	protected abstract String notExistMsg();
}

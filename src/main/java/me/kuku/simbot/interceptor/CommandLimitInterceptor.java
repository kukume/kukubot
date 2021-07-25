package me.kuku.simbot.interceptor;

import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.ListenerInterceptContext;
import love.forte.simbot.listener.ListenerInterceptor;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.GroupService;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@SuppressWarnings("DuplicatedCode")
@Component
public class CommandLimitInterceptor implements ListenerInterceptor {

	@Resource
	private GroupService groupService;
	private final Cache cache;

	public CommandLimitInterceptor(CacheManager cacheManager){
		cache = cacheManager.getCache("CommandCountOnTime");
	}

	@NotNull
	@Override
	public InterceptionType intercept(@NotNull ListenerInterceptContext context) {
		MsgGet msgGet = context.getMsgGet();
		if (msgGet instanceof GroupMsg){
			Filter filter = context.getMainValue().getAnnotation(Filter.class);
			if (filter == null) return InterceptionType.PASS;
			String value = filter.value();
			if (value.equals(".*")) return InterceptionType.PASS;
			String command = value;
			if (filter.matchType() == MatchType.REGEX_MATCHES){
				command = value.replaceAll("\\{\\{.*}}", "").trim();
			}
			GroupMsg groupMsg = (GroupMsg) msgGet;
			long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
			long group = groupMsg.getGroupInfo().getGroupCodeNumber();
			GroupEntity groupEntity = groupService.findByGroup(group);
			if (groupEntity == null) return InterceptionType.PASS;
			JSONObject jsonObject = groupEntity.getCommandLimitJson();
			if (jsonObject.containsKey(command)){
				Integer maxCount = jsonObject.getInteger(command);
				if (maxCount < 0) return InterceptionType.PASS;
				String key = "group" + group + command;
				Integer num = 0;
				Cache.ValueWrapper valueWrapper = cache.get(key);
				if (valueWrapper != null) num = (Integer) valueWrapper.get();
				if (num >= maxCount) return InterceptionType.INTERCEPT;
				cache.put(key, ++num);
			}
		}
		return InterceptionType.PASS;
	}
}

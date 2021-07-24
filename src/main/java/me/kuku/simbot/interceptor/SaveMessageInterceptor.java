package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.constant.PriorityConstant;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.MsgInterceptContext;
import love.forte.simbot.listener.MsgInterceptor;
import me.kuku.simbot.entity.MessageEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.service.MessageService;
import me.kuku.simbot.service.QqService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Component
public class SaveMessageInterceptor implements MsgInterceptor {

	@Resource
	private QqService qqService;
	@Resource
	private MessageService messageService;

	@NotNull
	@Override
	@Transactional
	public InterceptionType intercept(@NotNull MsgInterceptContext context) {
		MsgGet msgGet = context.getMsgGet();
		if (msgGet instanceof GroupMsg){
			GroupMsg groupMsg = (GroupMsg) msgGet;
			QqEntity qqEntity = qqService.findByQq(groupMsg.getAccountInfo().getAccountCodeNumber());
			if (qqEntity == null) return InterceptionType.PASS;
			MessageEntity messageEntity = new MessageEntity(null, groupMsg.getId(), qqEntity,
					qqEntity.getGroup(groupMsg.getGroupInfo().getGroupCodeNumber()), groupMsg.getMsg(), new Date());
			messageService.save(messageEntity);
		}
		return InterceptionType.PASS;
	}

	@Override
	public int getPriority() {
		return PriorityConstant.SECOND;
	}
}

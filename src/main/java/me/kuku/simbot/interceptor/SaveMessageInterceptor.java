package me.kuku.simbot.interceptor;

import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.constant.PriorityConstant;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.MsgInterceptContext;
import love.forte.simbot.listener.MsgInterceptor;
import me.kuku.simbot.entity.MessageEntity;
import me.kuku.simbot.entity.MessageService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
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
			long group = groupMsg.getGroupInfo().getGroupCodeNumber();
			String groupName = groupMsg.getGroupInfo().getGroupName();
			long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
			String qqName = groupMsg.getAccountInfo().getAccountRemarkOrNickname();
			String msg = groupMsg.getMsg();
			log.info(String.format("[%s(%s)]%s(%s) -> ([ %s ])",
					groupName, group, qq, qqName, msg));
			QqEntity qqEntity = qqService.findByQq(groupMsg.getAccountInfo().getAccountCodeNumber());
			if (qqEntity == null) return InterceptionType.PASS;
			MessageEntity messageEntity = new MessageEntity(null, groupMsg.getId(), qqEntity,
					qqEntity.getGroup(groupMsg.getGroupInfo().getGroupCodeNumber()), msg, new Date());
			messageService.save(messageEntity);
		}else if (msgGet instanceof PrivateMsg){
			PrivateMsg privateMsg = (PrivateMsg) msgGet;
			long qq = privateMsg.getAccountInfo().getAccountCodeNumber();
			String name = privateMsg.getAccountInfo().getAccountRemarkOrNickname();
			String msg = privateMsg.getMsg();
			log.info(String.format("%s(%s) -> ([ %s ])",
					name, qq, msg));
		}
		return InterceptionType.PASS;
	}

	@Override
	public int getPriority() {
		return PriorityConstant.SECOND;
	}
}

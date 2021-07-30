package me.kuku.simbot.interceptor;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.MsgGet;
import love.forte.simbot.constant.PriorityConstant;
import love.forte.simbot.intercept.InterceptionType;
import love.forte.simbot.listener.MsgInterceptContext;
import love.forte.simbot.listener.MsgInterceptor;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.GroupService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class SaveInfoInterceptor implements MsgInterceptor {
	@Autowired
	private QqService qqService;
	@Autowired
	private GroupService groupService;
	@Override
	public int getPriority() {
		return PriorityConstant.FIRST;
	}

	@NotNull
	@Override
	@Transactional
	public synchronized InterceptionType intercept(@NotNull MsgInterceptContext context) {
		MsgGet msgGet = context.getMsgGet();
		if (msgGet instanceof GroupMsg){
			GroupMsg groupMsg = (GroupMsg) msgGet;
			long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
			long group = groupMsg.getGroupInfo().getGroupCodeNumber();
			boolean isSave = false;
			boolean isAdd = true;
			QqEntity qqEntity = qqService.findByQq(qq);
			if (qqEntity == null){
				qqEntity = QqEntity.Companion.getInstance(qq);
				isSave = true;
			}
			Set<GroupEntity> groups = qqEntity.getGroups();
			for (GroupEntity groupEntity : groups) {
				if (groupEntity.getGroup() == group){
					isAdd = false;
					isSave = true;
					break;
				}
			}
			if (isAdd){
				GroupEntity groupEntity = groupService.findByGroup(group);
				if (groupEntity == null) groupEntity = GroupEntity.Companion.getInstance(group);
				groups.add(groupEntity);
				groupEntity.getQqEntities().add(qqEntity);
			}
			if (isSave)
				qqService.save(qqEntity);
		}
		return InterceptionType.PASS;
	}
}

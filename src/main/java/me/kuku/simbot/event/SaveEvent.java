package me.kuku.simbot.event;

import catcode.CatCodeUtil;
import catcode.CodeTemplate;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnGroupMsgRecall;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.GroupMsgRecall;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.MessageEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.RecallMessageEntity;
import me.kuku.simbot.service.GroupService;
import me.kuku.simbot.service.MessageService;
import me.kuku.simbot.service.QqService;
import me.kuku.simbot.service.RecallMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class SaveEvent {

	@Autowired
	private QqService qqService;
	@Autowired
	private MessageService messageService;
	@Autowired
	private RecallMessageService recallMessageService;
	@Autowired
	private GroupService groupService;

	@OnGroup
	@Transactional
	public void saveMessage(GroupMsg groupMsg){
		QqEntity qqEntity = qqService.findByQq(groupMsg.getAccountInfo().getAccountCodeNumber());
		if (qqEntity == null) return;
		MessageEntity messageEntity = new MessageEntity(null, groupMsg.getId(), qqEntity,
				qqEntity.getGroup(groupMsg.getGroupInfo().getGroupCodeNumber()), groupMsg.getMsg(), new Date());
		messageService.save(messageEntity);
	}

	@OnGroupMsgRecall
	@Transactional
	public void saveRecall(GroupMsgRecall groupMsgRecall, MsgSender msgSender){
		String id = groupMsgRecall.getId().replace("REC-", "");
		GroupEntity groupEntity = groupService.findByGroup(groupMsgRecall.getGroupInfo().getGroupCodeNumber());
		if (groupEntity == null) return;
		QqEntity qqEntity = groupEntity.getQq(groupMsgRecall.getAccountInfo().getAccountCodeNumber());
		if (qqEntity == null) return;
		MessageEntity messageEntity = messageService.findByMessageIdAndGroupEntity(id, groupEntity);
		if (messageEntity == null) return;
		RecallMessageEntity entity = new RecallMessageEntity(null, qqEntity, groupEntity, messageEntity, new Date(groupMsgRecall.getRecallTime()));
		recallMessageService.save(entity);
		CodeTemplate<String> template = CatCodeUtil.getInstance().getStringTemplate();
		msgSender.SENDER.sendGroupMsg(groupMsgRecall.getGroupInfo(),
				template.at(qqEntity.getQq()) + "：妄图撤回一条消息，消息内容为：");
		msgSender.SENDER.sendGroupMsg(groupMsgRecall.getGroupInfo(), messageEntity.getContent());
	}






}

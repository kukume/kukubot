package me.kuku.simbot.event;

import catcode.CatCodeUtil;
import catcode.CodeTemplate;
import catcode.Neko;
import catcode.StringTemplate;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnGroupMsgRecall;
import love.forte.simbot.annotation.Priority;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.GroupMsgRecall;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.constant.PriorityConstant;
import me.kuku.simbot.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

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
	@Autowired
	private StringTemplate stringTemplate;

	@OnGroupMsgRecall
	@Transactional
	@Priority(PriorityConstant.LAST)
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
		if (Boolean.TRUE.equals(groupEntity.getRecall())) {
			CodeTemplate<String> template = CatCodeUtil.getInstance().getStringTemplate();
			msgSender.SENDER.sendGroupMsg(groupMsgRecall.getGroupInfo(),
					template.at(qqEntity.getQq()) + "：妄图撤回一条消息，消息内容为：");
			msgSender.SENDER.sendGroupMsg(groupMsgRecall.getGroupInfo(), messageEntity.getContent());
		}
	}

	@OnGroup
	public void flash(GroupMsg groupMsg, GroupEntity groupEntity, long qq, MsgSender msgSender){
		if (Boolean.TRUE.equals(groupEntity.getFlashNotify())) {
			List<Neko> list = groupMsg.getMsgContent().getCats("image");
			for (Neko neko : list) {
				if ("true".equals(neko.get("flash"))){
					String url = neko.get("url");
					msgSender.SENDER.sendGroupMsg(groupMsg,
							stringTemplate.at(qq) + "：妄图发送闪照：");
					msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.image(url));
				}
			}
		}
	}





}

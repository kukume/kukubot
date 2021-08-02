package me.kuku.simbot.event;

import catcode.CatCodeUtil;
import catcode.CodeTemplate;
import catcode.Neko;
import catcode.StringTemplate;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Listen;
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
@Slf4j
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

	@Listen(MiraiUserMessagePostSendEvent.class)
	@Listen(MiraiGroupMessagePostSendEvent.class)
	public void listen(MiraiMessagePostSendEvent miraiMessagePostSendEvent){
		String msg = miraiMessagePostSendEvent.getMsg();
		if (miraiMessagePostSendEvent instanceof MiraiGroupMessagePostSendEvent) {
			MiraiGroupMessagePostSendEvent miraiGroupMessagePostSendEvent = (MiraiGroupMessagePostSendEvent) miraiMessagePostSendEvent;
			long group = miraiGroupMessagePostSendEvent.getGroupInfo().getGroupCodeNumber();
			String groupName = miraiGroupMessagePostSendEvent.getGroupInfo().getGroupName();
			log.info(String.format("%s(%s) <- ([ %s ])", groupName, group, msg));
		}else if (miraiMessagePostSendEvent instanceof MiraiTempMessagePostSendEvent){
			MiraiTempMessagePostSendEvent miraiTempMessagePostSendEvent = (MiraiTempMessagePostSendEvent) miraiMessagePostSendEvent;
			long qq = miraiTempMessagePostSendEvent.getQq();
			long group = miraiTempMessagePostSendEvent.getGroupInfo().getGroupCodeNumber();
			String name = miraiTempMessagePostSendEvent.getAccountInfo().getAccountRemarkOrNickname();
			String groupName = miraiTempMessagePostSendEvent.getGroupInfo().getGroupName();
			log.info(String.format("%s(%s)[%s(%s)] <- ([ %s ])", name, qq, groupName,
					group, msg));
		}else if (miraiMessagePostSendEvent instanceof MiraiFriendMessagePostSendEvent){
			MiraiFriendMessagePostSendEvent miraiFriendMessagePostSendEvent = (MiraiFriendMessagePostSendEvent) miraiMessagePostSendEvent;
			long qq = miraiFriendMessagePostSendEvent.getQq();
			log.info(String.format("(%s) <- ([ %s ])", qq, msg));
		}
		// (734669014) <- ([ "请输入网易云音乐账号！！" ])
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

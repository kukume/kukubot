package me.kuku.simbot.event;

import catcode.StringTemplate;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.events.GroupAddRequest;
import love.forte.simbot.api.message.events.GroupMemberIncrease;
import love.forte.simbot.api.message.events.GroupMemberReduce;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.message.event.MiraiBotLeaveEvent;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.MessageEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.service.GroupService;
import me.kuku.simbot.service.MessageService;
import me.kuku.simbot.service.QqService;
import me.kuku.utils.DateTimeFormatterUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.List;

@SuppressWarnings("ConstantConditions")
@Service
public class GroupManagerEvent {

	@Resource
	private StringTemplate stringTemplate;
	@Resource
	private GroupService groupService;
	@Resource
	private MessageService messageService;
	@Resource
	private QqService qqService;
	@Resource
	private ThreadPoolTaskScheduler threadPoolTaskScheduler;

	@OnGroup
	public void qa(GroupMsg groupMsg, GroupEntity groupEntity, MsgSender msgSender){
		JSONArray qaJsonArray = groupEntity.getQaJson();
		String str = groupMsg.getMsg();
		for (int i = 0; i < qaJsonArray.size(); i++){
			JSONObject jsonObject = qaJsonArray.getJSONObject(i);
			String type = jsonObject.getString("type");
			String q = jsonObject.getString("q");
			boolean status = false;
			if ("ALL".equals(type)){
				if (str.equals(q)) status = true;
			}else if (str.contains(jsonObject.getString("q"))) status = true;
			if (status){
//				Integer maxCount = groupEntity.getMaxCommandCountOnTime();
//				if (maxCount == null) maxCount = -1;
//				if (maxCount > 0){
//					String key = "qq" + e.getSender().getId() + q;
//					Integer num = eh.get(key);
//					if (num == null) num = 0;
//					if (num >= maxCount) return;
//					eh.set(key, ++num);
//				}
				String sendMsg = jsonObject.getString("a");
				msgSender.SENDER.sendGroupMsg(groupMsg, sendMsg);
			}
		}
	}

	@OnGroup
	@ListenBreak
	public boolean inter(GroupMsg groupMsg, GroupEntity groupEntity, MsgSender msgSender, long qq){
		String str = groupMsg.getMsg();
		JSONArray interceptJsonArray = groupEntity.getInterceptJson();
		for (int i = 0; i < interceptJsonArray.size(); i++){
			String inter = interceptJsonArray.getString(i);
			if (str.contains(inter)) return true;
		}
		JSONArray violationJsonArray = groupEntity.getViolationJson();
		int code = 0;
		String vio = null;
		for (int i = 0; i < violationJsonArray.size(); i++) {
			String violation = violationJsonArray.getString(i);
			String nameCard = groupMsg.getAccountInfo().getAccountNicknameAndRemark();
			if (nameCard.contains(violation)) {
				code = 3;
				vio = violation;
				break;
			}
			if (str.contains(violation)){
				code = 1;
				vio = violation;
				break;
			}
		}
		if (code != 0){
			StringBuilder sb = new StringBuilder();
			if (code == 2) sb.append("检测到色情图片。").append("\n");
			else if (code == 1) sb.append("检测到违规词\"").append(vio).append("\"。").append("\n");
			else sb.append("检测到违规去群名片\"").append(vio).append("\"。").append("\n");
			sb.append("您已被禁言！");
			msgSender.SETTER.setGroupBan(groupMsg, 60 * 30);
			msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + sb.toString());
			return true;
		}
		return false;
	}

	@OnGroupAddRequest
	public void groupMemberRequest(GroupAddRequest groupAddRequest, MsgSender msgSender){
		long group = groupAddRequest.getGroupInfo().getGroupCodeNumber();
		long qq = groupAddRequest.getAccountInfo().getAccountCodeNumber();
		GroupEntity groupEntity = groupService.findByGroup(group);
		if (groupEntity.getAutoReview() != null && groupEntity.getAutoReview()){
			boolean status = true;
			JSONArray blackJsonArray = groupEntity.getBlackJson();
			for (int i = 0; i < blackJsonArray.size(); i++){
				long black = blackJsonArray.getLong(i);
				if (black == qq){
					status = false;
					break;
				}
			}
			if (status)
				msgSender.SETTER.acceptGroupAddRequest(groupAddRequest.getFlag());
			else msgSender.SETTER.rejectGroupAddRequest(groupAddRequest.getFlag());
		}
	}

	@OnGroupMemberReduce
	public void groupMemberReduce(GroupMemberReduce groupMemberReduce, MsgSender msgSender){
		long group = groupMemberReduce.getGroupInfo().getGroupCodeNumber();
		long qq = groupMemberReduce.getAccountInfo().getAccountCodeNumber();
		String name = groupMemberReduce.getAccountInfo().getAccountNicknameAndRemark();
		GroupEntity groupEntity = groupService.findByGroup(group);
		QqEntity qqEntity = groupEntity.getQq(qq);
		String msg;
		if (groupMemberReduce.getReduceType() == GroupMemberReduce.Type.LEAVE){
			if (groupEntity.getLeaveGroupBlack() != null && groupEntity.getLeaveGroupBlack()){
				JSONArray blackJsonArray = groupEntity.getBlackJson();
				blackJsonArray.add(String.valueOf(qq));
				groupEntity.setBlackJson(blackJsonArray);
				groupService.save(groupEntity);
				msg = "刚刚，" + name + "（" + qq + "）退群了，已加入本群黑名单！！";
			}else msg = "刚刚，" + name + "（" + qq + "）离开了我们！！";
			List<MessageEntity> list = messageService.findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity);
			String finallyMessage;
			if (list.size() == 0){
				finallyMessage = "他好像还没有说过话！！";
			}else {
				MessageEntity messageEntity = list.get(0);
				msg += "\n尽管他就这么的走了，但是我们仍然不要忘记他在[" +
						DateTimeFormatterUtils.format(messageEntity.getDate().getTime(), "yyyy-MM-dd HH:mm:ss") +
						"]说的最后一句话：";
				finallyMessage = messageEntity.getContent();
			}
			msgSender.SENDER.sendGroupMsg(group, msg);
			msgSender.SENDER.sendGroupMsg(group, finallyMessage);
		}
		qqService.delete(qqEntity);
	}

	@OnGroupMemberIncrease
	public void increase(GroupMemberIncrease groupMemberIncrease, MsgSender msgSender){
		long group = groupMemberIncrease.getGroupInfo().getGroupCodeNumber();
		long qq = groupMemberIncrease.getAccountInfo().getAccountCodeNumber();
		GroupEntity groupEntity = groupService.findByGroup(group);
		QqEntity qqEntity = groupEntity.getQq(qq);

		if (Boolean.valueOf(true).equals(groupEntity.getKickWithoutSpeaking())){
			msgSender.SENDER.sendGroupMsg(group, stringTemplate.at(qq) + "欢迎进群，请尽快发言哦，进群5分钟未发言将会被移出本群。");
			threadPoolTaskScheduler.schedule(() -> {
				List<MessageEntity> list = messageService.findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity);
				if (list.size() == 0){
					msgSender.SETTER.setGroupMemberKick(group, qq, "进群未发言踢出", true);
					msgSender.SENDER.sendGroupMsg(group, qq + "未发送消息，已被移除本群！");
				}
			}, Instant.now().plusMillis(5));
		}
	}

	@Listen(value = MiraiBotLeaveEvent.class)
	public void botLeave(MiraiBotLeaveEvent miraiBotLeaveEvent){
		long group = miraiBotLeaveEvent.getGroupInfo().getGroupCodeNumber();
		groupService.deleteByGroup(group);
	}


}

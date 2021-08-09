package me.kuku.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.IceCreamQAQ.Yu.cache.EhcacheHelp;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.event.*;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.utils.DateTimeFormatterUtils;
import me.kuku.yuq.entity.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventListener
public class GroupManagerEvent {
	@Inject
	private GroupService groupService;
	@Inject
	private QqService qqService;
	@Inject
	private MessageService messageService;
	@Inject
	private JobManager jobManager;
	@Inject
	@Named("CommandCountOnTime")
	public EhcacheHelp<Integer> eh;

	@Event(weight = Event.Weight.high)
	public void status(com.IceCreamQAQ.Yu.event.events.Event e){
		Long group = null;
		Message message = null;
		if (e instanceof GroupMemberEvent){
			group = ((GroupMemberEvent) e).getGroup().getId();
		}else if (e instanceof GroupMemberRequestEvent){
			group = ((GroupMemberRequestEvent) e).getGroup().getId();
		}else if (e instanceof GroupRecallEvent){
			group = ((GroupRecallEvent) e).getGroup().getId();
		}else if (e instanceof GroupMessageEvent){
			GroupMessageEvent groupMessageEvent = (GroupMessageEvent) e;
			group = groupMessageEvent.getGroup().getId();
			message = groupMessageEvent.getMessage();
		}
		if (group == null) return;
		GroupEntity groupEntity = groupService.findByGroup(group);
		boolean status = true;
		if (message != null) {
			List<String> list = message.toPath();
			if (list.size() == 2) {
				String pa = list.get(1);
				if ("kukubot".equals(list.get(0)) && pa.equals("开") || pa.equals("关")) {
					status = false;
				}
			}
		}
		if (groupEntity != null && Boolean.TRUE.equals(groupEntity.getStatus())){
			status = false;
		}
		if (status){
			e.setCancel(true);
		}
	}

	@Event
	public void qa(GroupMessageEvent e){
		GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
		if (groupEntity == null) return;
		Message message = e.getMessage();
		if (message.toPath().size() == 0) return;
		if ("删问答".equals(message.toPath().get(0))) return;
		String str;
		try {
			str = Message.Companion.firstString(message);
		}catch (IllegalStateException ex){
			return;
		}
		JSONArray qaJsonArray = groupEntity.getQaJson();
		List<String> resultList = new ArrayList<>();
		for (int i = 0; i < qaJsonArray.size(); i++){
			JSONObject jsonObject = qaJsonArray.getJSONObject(i);
			String type = jsonObject.getString("type");
			String q = jsonObject.getString("q");
			boolean status = false;
			if ("ALL".equals(type)){
				if (str.equals(q)) status = true;
			}else if (str.contains(jsonObject.getString("q"))) status = true;
			if (status){
				String a = jsonObject.getString("a");
				resultList.add(a);
			}
		}
		int size = resultList.size();
		if (resultList.size() != 0){
			String a = resultList.get(new Random().nextInt(size));
			e.getGroup().sendMessage(Message.Companion.toMessageByRainCode(a));
		}
	}

	@Event
	public void groupMemberRequest(GroupMemberRequestEvent e){
		GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
		if (groupEntity == null) return;
		if (groupEntity.getAutoReview() != null && groupEntity.getAutoReview()){
			boolean status = true;
			JSONArray blackJsonArray = groupEntity.getBlackJson();
			for (int i = 0; i < blackJsonArray.size(); i++){
				long black = blackJsonArray.getLong(i);
				if (black == e.getQq().getId()){
					status = false;
					break;
				}
			}
			e.setAccept(status);
			e.cancel = true;
		}
	}

	@Event
	public void groupMemberLeave(GroupMemberLeaveEvent e){
		long qq = e.getMember().getId();
		long group = e.getGroup().getId();
		GroupEntity groupEntity = groupService.findByGroup(group);
		QqEntity qqEntity = groupEntity.getQq(qq);
		String msg;
		if (e instanceof GroupMemberLeaveEvent.Leave) {
			String name = e.getMember().getName();
			if (groupEntity.getLeaveGroupBlack() != null && groupEntity.getLeaveGroupBlack()) {
				JSONArray blackJsonArray = groupEntity.getBlackJson();
				blackJsonArray.add(String.valueOf(qq));
				groupEntity.setBlackJson(blackJsonArray);
				groupService.save(groupEntity);
				msg = "刚刚，" + name + "（" + qq + "）退群了，已加入本群黑名单！！";
			} else msg = "刚刚，" + name + "（" + qq + "）离开了我们！！";
			List<MessageEntity> messageList = messageService.findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity);
			Message finallyMessage;
			if (messageList.size() == 0) {
				finallyMessage = Message.Companion.toMessage("他好像还没有说过话！！");
			} else {
				MessageEntity messageEntity = messageList.get(0);
				msg += "\n尽管他就这么的走了，但是我们仍然不要忘记他在[" +
						DateTimeFormatterUtils.format(messageEntity.getDate().getTime(), "yyyy-MM-dd HH:mm:ss") +
						"]说的最后一句话：";
				finallyMessage = Message.Companion.toMessageByRainCode(messageEntity.getContent());
			}
			e.getGroup().sendMessage(Message.Companion.toMessage(msg));
			e.getGroup().sendMessage(finallyMessage);
		}
		qqService.delete(qqEntity);
	}

	@Event
	public void groupMemberJoin(GroupMemberJoinEvent e) {
		long group = e.getGroup().getId();
		long qq = e.getMember().getId();
		GroupEntity groupEntity = groupService.findByGroup(group);
		QqEntity qqEntity = groupEntity.getQq(qq);
		if (Boolean.valueOf(true).equals(groupEntity.getKickWithoutSpeaking())){
			e.getGroup().sendMessage(FunKt.getMif().at(qq).plus("请尽快发言哦，进群5分钟未发言将会被移出本群。"));
			jobManager.registerTimer(() -> {
				List<MessageEntity> list = messageService.findByQqEntityAndGroupEntityOrderByDateDesc(qqEntity, groupEntity);
				if (list.size() == 0){
					e.getMember().kick("进群未发言踢出");
					e.getGroup().sendMessage(Message.Companion.toMessage(qq + "未发送消息，已移出本群"));
				}
			}, 1000 * 60 * 1000);
		}
	}
}

package me.kuku.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.IceCreamQAQ.Yu.event.events.AppStartEvent;
import com.IceCreamQAQ.Yu.util.OkHttpWebImpl;
import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.event.BotLeaveGroupEvent;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.event.GroupRecallEvent;
import com.icecreamqaq.yuq.event.PrivateMessageEvent;
import com.icecreamqaq.yuq.message.FlashImage;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItem;
import com.icecreamqaq.yuq.message.MessageSource;
import com.icecreamqaq.yuq.mirai.MiraiBot;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@SuppressWarnings("DuplicatedCode")
@EventListener
public class SaveInfoEvent {

	@Inject
	private QqService qqService;
	@Inject
	private GroupService groupService;
	@Inject
	private MessageService messageService;
	@Inject
	private RecallMessageService recallMessageService;
	@Inject
	private OkHttpWebImpl web;
	@Inject
	private MiraiBot miraiBot;

	@Event(weight = Event.Weight.high)
	@Transactional
	public void saveInfo(GroupMessageEvent e){
		synchronized (this) {
			long group = e.getGroup().getId();
			long qq = e.getSender().getId();
			boolean isSave = false;
			boolean isAdd = true;
			QqEntity qqEntity = qqService.findByQq(qq);
			if (qqEntity == null) {
				qqEntity = QqEntity.Companion.getInstance(qq);
				isSave = true;
			}
			Set<GroupEntity> groups = qqEntity.getGroups();
			for (GroupEntity groupEntity : groups) {
				if (groupEntity.getGroup() == group) {
					isAdd = false;
					isSave = true;
					break;
				}
			}
			if (isAdd) {
				GroupEntity groupEntity = groupService.findByGroup(group);
				if (groupEntity == null) groupEntity = GroupEntity.Companion.getInstance(group);
				groups.add(groupEntity);
				groupEntity.getQqEntities().add(qqEntity);
			}
			if (isSave)
				qqService.save(qqEntity);
		}
	}

	@Event(weight = Event.Weight.high)
	public void savePrivateInfo(PrivateMessageEvent e){
		synchronized (this){
			long qq = e.getSender().getId();
			boolean isSave = false;
			QqEntity qqEntity = qqService.findByQq(qq);
			if (qqEntity == null){
				qqEntity = QqEntity.Companion.getInstance(qq);
				isSave = true;
			}
			if (e instanceof PrivateMessageEvent.TempMessage){
				boolean isAdd = true;
				PrivateMessageEvent.TempMessage temp = (PrivateMessageEvent.TempMessage) e;
				long group = temp.getSender().getGroup().getId();
				Set<GroupEntity> groups = qqEntity.getGroups();
				for (GroupEntity groupEntity : groups) {
					if (groupEntity.getGroup() == group) {
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
			}
			if (isSave)
				qqService.save(qqEntity);
		}
	}

	@Event
	@Transactional
	public void saveMessage(GroupMessageEvent e){
		long qq = e.getSender().getId();
		long group = e.getGroup().getId();
		messageService.save(new MessageEntity(null, e.getMessage().getSource().getId(),
				qqService.findByQq(qq), groupService.findByGroup(group), e.getMessage().getCodeStr(), new Date()));
	}

	@Event
	public void readMessage(GroupMessageEvent e){
		Message message = e.getMessage();
		MessageSource reply = message.getReply();
		List<String> list = message.toPath();
		if (list.size() == 0) return;
		String lastPath = list.get(list.size() - 1);
		if (reply != null && lastPath.endsWith("读消息")){
			GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
			MessageEntity messageEntity = messageService.findByMessageIdAndGroupEntity(reply.getId(), groupEntity);
			String msg;
			if (messageEntity == null){
				msg = "找不到您当前回复的消息！！";
			}else {
				msg = messageEntity.getContent();
			}
			e.getGroup().sendMessage(Message.Companion.toMessage(msg));
		}
	}

	@Event
	@Transactional
	public void recallEvent(GroupRecallEvent e){
		long qq = e.getSender().getId();
		long group = e.getGroup().getId();
		GroupEntity groupEntity = groupService.findByGroup(group);
		MessageEntity messageEntity = messageService.findByMessageIdAndGroupEntity(e.getMessageId(), groupEntity);
		if (messageEntity == null) return;
		QqEntity qqEntity = qqService.findByQq(qq);
		RecallMessageEntity recallEntity = new RecallMessageEntity(null, qqEntity, groupEntity, messageEntity, new Date());
		recallMessageService.save(recallEntity);
		if (Boolean.valueOf(true).equals(groupEntity.getRecall())){
			if (!e.getSender().equals(e.getOperator())) return;
			e.getGroup().sendMessage(FunKt.getMif().text("群成员").plus(FunKt.getMif().at(qq)).plus("\n妄图撤回一条消息。\n消息内容为："));
			e.getGroup().sendMessage(Message.Companion.toMessageByRainCode(messageEntity.getContent()));
		}
	}

	@Event
	public void flashNotify(GroupMessageEvent e){
		long group = e.getGroup().getId();
		GroupEntity groupEntity = groupService.findByGroup(group);
		if (Boolean.valueOf(true).equals(groupEntity.getFlashNotify())){
			ArrayList<MessageItem> body = e.getMessage().getBody();
			long qq = e.getSender().getId();
			for (MessageItem item : body) {
				if (item instanceof FlashImage){
					FlashImage fl = (FlashImage) item;
					Message msg = FunKt.getMif().text("群成员：").plus(FunKt.getMif().at(qq))
							.plus("\n妄图发送闪照：\n")
							.plus(FunKt.getMif().imageByUrl(fl.getUrl()));
					e.getGroup().sendMessage(msg);
				}
			}
		}
	}

	@Event
	public void start(AppStartEvent e){
		BotUtils.setMiraiBot(miraiBot);
		BotUtils.setWeb(web);
	}

	@Event
	public void leave(BotLeaveGroupEvent e){
		long group = e.getGroup().getId();
		GroupEntity groupEntity = groupService.findByGroup(group);
		if (groupEntity != null)
			groupService.delete(groupEntity.getId());
	}

}

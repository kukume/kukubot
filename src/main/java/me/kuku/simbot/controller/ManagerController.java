package me.kuku.simbot.controller;

import catcode.StringTemplate;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.annotation.SkipListenGroup;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.RecallMessageEntity;
import me.kuku.simbot.logic.QqLoginLogic;
import me.kuku.simbot.service.GroupService;
import me.kuku.simbot.service.QqService;
import me.kuku.simbot.service.RecallMessageService;
import me.kuku.simbot.utils.BotUtils;
import me.kuku.utils.DateTimeFormatterUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@OnGroup
@ListenGroup("manager")
@Service
public class ManagerController {

	@Resource
	private GroupService groupService;
	@Resource
	private RecallMessageService recallMessageService;
	@Resource
	private QqService qqService;
	@Resource
	private StringTemplate stringTemplate;
	@Resource
	private QqLoginLogic qqLoginLogic;

	@RegexFilter("kukubot{{statusStr}}")
	public String openOrOff(@ContextValue("groupEntity") GroupEntity groupEntity,
	                        @FilterValue("statusStr") String statusStr){
		boolean status = statusStr.contains("开");
		groupEntity.setStatus(status);
		groupService.save(groupEntity);
		return "kukubot" + (status ? "开启" : "关闭") + "成功！";
	}

	@Filter(value = "查撤回", anyAt = true)
	@SkipListenGroup
	public String queryRecall(GroupMsg groupMsg, MsgSender msgSender, ContextSession session){
		long qqq = groupMsg.getAccountInfo().getAccountCodeNumber();
		Long qq = BotUtils.getAt(groupMsg.getMsgContent());
		assert qq != null;
		QqEntity qqEntity = qqService.findByQq(qq);
		List<RecallMessageEntity> list = recallMessageService.findByQqEntityOrderByDateDesc(qqEntity);
		int size = list.size();
		if (size == 0) return "该qq没有撤回过消息哦！";
		msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qqq) + "该qq有" + size + "条信息，您需要查看第几条？");
		String str = session.waitNextMessage();
		if (!str.matches("[0-9]+")) return "您输入的不为数字!";
		int num = Integer.parseInt(str);
		if (num < 1 || num > size) return "您输入的数字越界了！";
		RecallMessageEntity recallMessageEntity = list.get(size - 1);
		msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + "在" +
				DateTimeFormatterUtils.format(recallMessageEntity.getDate().getTime(), "yyyy-MM-dd HH:mm:ss") +
				"妄图撤回一条消息，消息内容为：");
		return recallMessageEntity.getMessageEntity().getContent();
	}

	@RegexFilter("全体禁言{{statusStr}}")
	public String allShutUp(String statusStr, MsgSender msgSender, GroupMsg groupMsg) throws IOException {
		boolean status = statusStr.contains("开");
		return qqLoginLogic.allShutUp(BotUtils.getBotQqLoginEntity(msgSender.GETTER), groupMsg.getGroupInfo().getGroupCodeNumber(), status);
	}

	@Filter(value = "t", anyAt = true)
	public String kick(GroupMsg groupMsg, MsgSender msgSender){
		long group = groupMsg.getGroupInfo().getGroupCodeNumber();
		long qq = BotUtils.getAt(groupMsg.getMsgContent());
		try {
			msgSender.SETTER.setGroupMemberKick(group, qq, "", true);
			return "踢出成功！";
		} catch (Exception e) {
			e.printStackTrace();
			return "踢出失败，可能权限不足！";
		}
	}

	@RegexFilter("指令限制{{count}}")
	public String maxCommandCount(GroupEntity groupEntity, int count){
		groupEntity.setMaxCommandCountOnTime(count);
		groupService.save(groupEntity);
		return "已设置本群单个指令每人十分钟最大触发次数为" + count + "次";
	}

	@RegexFilter("加指令限制 {{command}} {count}")
	public String addCommandLimit(GroupEntity groupEntity, String command, int count){
		JSONObject jsonObject = groupEntity.getCommandLimitJson();
		jsonObject.put(command, count);
		groupEntity.setCommandLimitJson(jsonObject);
		groupService.save(groupEntity);
		return "加指令限制成功！！已设置指令{" + command + "}十分钟之内只会响应" + count + "次";
	}

	@RegexFilter("删指令限制{{command}}")
	public String delCommandLimit(GroupEntity groupEntity, String command){
		JSONObject jsonObject = groupEntity.getCommandLimitJson();
		jsonObject.remove(command);
		groupEntity.setCommandLimitJson(jsonObject);
		groupService.save(groupEntity);
		return "删指令{" + command + "}限制成功！！";
	}

	@RegexFilter("加问答{{q}}")
	public String qa(ContextSession session, long qq, GroupEntity groupEntity, String q, long group, MsgSender msgSender){
		msgSender.SENDER.sendGroupMsg(group, stringTemplate.at(qq) + "请输入问答类型，1为精准匹配，其他为模糊匹配");
		String type;
		String typeMsg = session.waitNextMessage();
		if ("1".equals(typeMsg)) type = "ALL";
		else type = "PARTIAL";
		msgSender.SENDER.sendGroupMsg(group, stringTemplate.at(qq) + "请输入回答语句！！");
		String msg = session.waitNextMessage();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("q", q);
		jsonObject.put("a", msg);
		jsonObject.put("type", type);
		JSONArray jsonArray = groupEntity.getQaJson();
		jsonArray.add(jsonObject);
		groupEntity.setQaJson(jsonArray);
		groupService.save(groupEntity);
		return "添加问答成功！！";
	}

	@RegexFilter("删问答{{q}}")
	public String delQa(GroupEntity groupEntity, String q){
		JSONArray qaJsonArray = groupEntity.getQaJson();
		List<JSONObject> delList = new ArrayList<>();
		for (int i = 0; i < qaJsonArray.size(); i++){
			JSONObject jsonObject = qaJsonArray.getJSONObject(i);
			if (q.equals(jsonObject.getString("q"))){
				delList.add(jsonObject);
			}
		}
		delList.forEach(qaJsonArray::remove);
		groupEntity.setQaJson(qaJsonArray);
		groupService.save(groupEntity);
		return "删除问答成功！！";
	}

}
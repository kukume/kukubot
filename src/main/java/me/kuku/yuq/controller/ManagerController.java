package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.At;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.Result;
import me.kuku.utils.DateTimeFormatterUtils;
import me.kuku.utils.MyUtils;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.BiliBiliPojo;
import me.kuku.yuq.pojo.WeiboPojo;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@GroupController
public class ManagerController {

	@Inject
	private GroupService groupService;
	@Inject
	private RecallMessageService recallMessageService;
	@Inject
	private QqService qqService;
	@Inject
	private WeiboLogic weiboLogic;
	@Inject
	private BiliBiliLogic biliBiliLogic;
	@Inject
	private MessageItemFactory mif;
	@Config("YuQ.Mirai.bot.master")
	private String master;

	@Before(except = {""})
	public void before(long qq){
		if (!String.valueOf(qq).equals(master))
			throw mif.at(qq).plus("您不为机器人主人，无法执行！").toThrowable();
	}

	@Action("kukubot {status}")
	@Synonym({"loc监控 {status}", "整点报时 {status}", "自动审核 {status}", "退群拉黑 {status}", "鉴黄 {status}",
			"色图 {status}", "撤回通知 {status}", "闪照通知 {status}", "复读 {status}", "进群未发言踢出 {status}",
			"群管理权限 {status}"})
	@QMsg(at = true)
	public String openOrOff(GroupEntity groupEntity, boolean status, @PathVar(0) String op){
		switch (op){
			case "kukubot": groupEntity.setStatus(status); break;
			case "loc监控": groupEntity.setLocMonitor(status); break;
			case "整点报时": groupEntity.setOnTimeAlarm(status); break;
			case "自动审核": groupEntity.setAutoReview(status); break;
			case "退群拉黑": groupEntity.setLeaveGroupBlack(status); break;
			case "鉴黄": groupEntity.setPic(status); break;
			case "色图": groupEntity.setColorPic(status); break;
			case "撤回通知": groupEntity.setRecall(status); break;
			case "闪照通知": groupEntity.setFlashNotify(status); break;
			case "复读": groupEntity.setRepeat(status); break;
			case "进群未发言踢出": groupEntity.setKickWithoutSpeaking(status); break;
			case "群管理权限": groupEntity.setGroupAdminAuth(status); break;
		}
		groupService.save(groupEntity);
		return op + (status ? "开启" : "关闭") + "成功！";
	}

	@Action(value = "查撤回 {qqNo}")
	@QMsg(at = true)
	public String queryRecall(ContextSession session, long qq, long qqNo, Group group){
		QqEntity qqEntity = qqService.findByQq(qqNo);
		List<RecallMessageEntity> list = recallMessageService.findByQqEntityOrderByDateDesc(qqEntity);
		int size = list.size();
		if (size == 0) return "该qq没有撤回过消息哦！";
		group.sendMessage(mif.at(qq).plus("该qq有" + size + "条信息，您需要查看第几条？"));
		String str = Message.Companion.firstString(session.waitNextMessage());
		if (!str.matches("[0-9]+")) return "您输入的不为数字!";
		int num = Integer.parseInt(str);
		if (num < 1 || num > size) return "您输入的数字越界了！";
		RecallMessageEntity recallMessageEntity = list.get(size - 1);
		group.sendMessage(mif.at(qq).plus("在" +
				DateTimeFormatterUtils.format(recallMessageEntity.getDate().getTime(), "yyyy-MM-dd HH:mm:ss") +
				"妄图撤回一条消息，消息内容为："));
		return recallMessageEntity.getMessageEntity().getContent();
	}

	@Action("全体禁言 {status}")
	@QMsg(at = true)
	public String allShutUp(boolean status, Group group) throws IOException {
		group.banAll();
		return "全体禁言成功！";
	}

	@Action("t {qqNo}")
	@QMsg(at = true)
	public String kick(long qqNo, Group group){
		try {
			group.get(qqNo).kick("");
			return "踢出成功！";
		} catch (Exception e) {
			e.printStackTrace();
			return "踢出失败，可能权限不足！";
		}
	}

	@Action("指令限制 {count}")
	@QMsg(at = true)
	public String maxCommandCount(GroupEntity groupEntity, int count){
		groupEntity.setMaxCommandCountOnTime(count);
		groupService.save(groupEntity);
		return "已设置本群单个指令每人十分钟最大触发次数为" + count + "次";
	}

	@Action("加指令限制 {command} {count}")
	@QMsg(at = true)
	public String addCommandLimit(GroupEntity groupEntity, String command, int count){
		JSONObject jsonObject = groupEntity.getCommandLimitJson();
		jsonObject.put(command, count);
		groupEntity.setCommandLimitJson(jsonObject);
		groupService.save(groupEntity);
		return "加指令限制成功！！已设置指令{" + command + "}十分钟之内只会响应" + count + "次";
	}

	@Action("删指令限制 {command}")
	@QMsg(at = true)
	public String delCommandLimit(GroupEntity groupEntity, String command){
		JSONObject jsonObject = groupEntity.getCommandLimitJson();
		jsonObject.remove(command);
		groupEntity.setCommandLimitJson(jsonObject);
		groupService.save(groupEntity);
		return "删指令{" + command + "}限制成功！！";
	}

	@Action("加问答 {q}")
	@QMsg(at = true)
	public String qa(ContextSession session, long qq, GroupEntity groupEntity, String q, Group group){
		group.sendMessage(mif.at(qq).plus("请输入问答类型，1为精准匹配，其他为模糊匹配"));
		String type;
		String typeMsg = Message.Companion.firstString(session.waitNextMessage());
		if ("1".equals(typeMsg)) type = "ALL";
		else type = "PARTIAL";
		group.sendMessage(mif.at(qq).plus("请输入回答语句！！"));
		String msg = Message.Companion.firstString(session.waitNextMessage());
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

	@Action("删问答 {q}")
	@QMsg(at = true)
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

	@Action("清屏")
	@QMsg(at = true)
	public String clear(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1000; i++) sb.append("\n");
		return sb.toString();
	}

	@Action("禁言 {qqNo}")
	@QMsg(at = true)
	public String shutUp(long group, long qqNo, @PathVar(2) String timeStr){
		int time;
		if (timeStr == null) time = 0;
		else {
			if (timeStr.length() == 1) return "未发现时间单位！！单位可为（s,m,h,d）";
			int num = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
			switch (timeStr.charAt(timeStr.length() - 1)){
				case 's': time = num; break;
				case 'm': time = num * 60; break;
				case 'h': time = num * 60 * 60; break;
				case 'd': time = num * 60 * 60 * 24; break;
				default: return "禁言时间格式不正确";
			}
		}
		FunKt.getYuq().getGroups().get(group).get(qqNo).ban(time);
		return "禁言成功！！";
	}

	@Action("加shell {command}")
	@QMsg(at = true)
	public String addShellCommand(GroupEntity groupEntity, String command, Group group, long qq, ContextSession session){
		JSONArray jsonArray = groupEntity.getShellCommandJson();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			if (singleJsonObject.getString("command").equals(command)){
				return "指令重复，请先删除该指令再添加！！";
			}
		}
		At at = FunKt.getMif().at(qq);
		group.sendMessage(at.plus("请输入命令权限设置，0为主人，1为普管，2为用户"));
		Message authMessage = session.waitNextMessage();
		String authStr = Message.Companion.firstString(authMessage);
		int auth = Integer.parseInt(authStr);
		group.sendMessage(at.plus("请输入需要执行的shell命令"));
		Message shellMessage = session.waitNextMessage();
		String shell = Message.Companion.firstString(shellMessage);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("auth", auth);
		jsonObject.put("shell", shell);
		jsonObject.put("command", command);
		jsonArray.add(jsonObject);
		groupEntity.setShellCommandJson(jsonArray);
		groupService.save(groupEntity);
		return "添加shell指令成功！！";
	}

	@Action("删shell {command}")
	@QMsg(at = true)
	public String delShellCommand(GroupEntity groupEntity, String command){
		JSONArray jsonArray = groupEntity.getShellCommandJson();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			if (singleJsonObject.getString("command").equals(command)){
				jsonArray.remove(i);
				groupEntity.setShellCommandJson(jsonArray);
				groupService.save(groupEntity);
				return "删除shell指令成功！！";
			}
		}
		return "没有找到这个shell指令！！";
	}

	@Action("加违规词 {content}")
	@Synonym({"加黑名单 {content}", "加拦截 {content}", "加微博监控 {content}", "加哔哩哔哩监控 {content}",
			"加哔哩哔哩开播提醒 {content}"})
	@QMsg(at = true)
	public String add(GroupEntity groupEntity, @PathVar(0) String type, String content, ContextSession session, long qq,
	                  Group group) throws IOException {
		switch (type){
			case "加违规词":
				groupEntity.setViolationJson(groupEntity.getViolationJson().fluentAdd(content));
				break;
			case "加黑名单":
				groupEntity.setBlackJson(groupEntity.getBlackJson().fluentAdd(content));
				break;
			case "加拦截":
				groupEntity.setInterceptJson(groupEntity.getInterceptJson().fluentAdd(content));
				break;
			case "加微博监控":
				Result<List<WeiboPojo>> result = weiboLogic.getIdByName(content);
				if (result.getCode() != 200) return "该用户不存在！！";
				List<WeiboPojo> wbList = result.getData();
				StringBuilder weiboSb = new StringBuilder().append("请输入序号：").append("\n");
				for (int i = 0; i < wbList.size(); i++) {
					WeiboPojo pojo = wbList.get(i);
					weiboSb.append(i + 1).append("、").append(pojo.getUserId()).append("-").append(pojo.getName())
							.append("\n");
				}
				group.sendMessage(mif.at(qq).plus(MyUtils.removeLastLine(weiboSb)));
				String wbNum = Message.Companion.firstString(session.waitNextMessage());
				if (!wbNum.matches("[0-9]+")) return "您输入的参数不正确！";
				int wbN = Integer.parseInt(wbNum);
				if (wbN < 1 || wbN > wbList.size()) return "您输入的参数不正确！";
				WeiboPojo weiboPojo = wbList.get(wbN - 1);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", weiboPojo.getUserId());
				jsonObject.put("name", weiboPojo.getName());
				groupEntity.setWeiboJson(groupEntity.getWeiboJson().fluentAdd(jsonObject));
				break;
			case "加哔哩哔哩监控":
			case "加哔哩哔哩开播提醒":
				Result<List<BiliBiliPojo>> blResult = biliBiliLogic.getIdByName(content);
				if (blResult.getCode() != 200) return "该用户不存在";
				List<BiliBiliPojo> biList = blResult.getData();
				StringBuilder biSb = new StringBuilder().append("请输入序号：").append("\n");
				for (int i = 0; i < biList.size(); i++) {
					BiliBiliPojo pojo = biList.get(i);
					biSb.append(i + 1).append("、").append(pojo.getUserId()).append("-").append(pojo.getName())
							.append("\n");
				}
				group.sendMessage(mif.at(qq).plus(MyUtils.removeLastLine(biSb)));
				String biNum = Message.Companion.firstString(session.waitNextMessage());
				if (!biNum.matches("[0-9]+")) return "您输入的参数不正确！";
				int biN = Integer.parseInt(biNum);
				if (biN < 1 || biN > biList.size()) return "您输入的参数不正确！";
				BiliBiliPojo biliBiliPojo = biList.get(biN - 1);
				JSONObject blJsonObject = new JSONObject();
				blJsonObject.put("id", biliBiliPojo.getUserId());
				blJsonObject.put("name", biliBiliPojo.getName());
				if ("加哔哩哔哩监控".equals(type))
					groupEntity.setBiliBiliJson(groupEntity.getBiliBiliJson().fluentAdd(blJsonObject));
				else
					groupEntity.setBiliBiliLiveJson(groupEntity.getBiliBiliLiveJson().fluentAdd(blJsonObject));
				break;
		}
		groupService.save(groupEntity);
		return type + "成功";
	}

	@Action("删违规词 {content}")
	@Synonym({"删黑名单 {content}", "删拦截 {content}", "删微博监控 {content}", "删哔哩哔哩监控 {content}",
			"删哔哩哔哩开播提醒 {content}"})
	@QMsg(at = true)
	public String del(GroupEntity groupEntity, @PathVar(0) String type, String content){
		switch (type){
			case "删违规词":
				JSONArray violationJsonArray = groupEntity.getViolationJson();
				delManager(violationJsonArray, content);
				groupEntity.setViolationJson(violationJsonArray);
				break;
			case "删黑名单":
				JSONArray blackJsonArray = groupEntity.getBlackJson();
				delManager(blackJsonArray, content);
				groupEntity.setBlackJson(blackJsonArray);
				break;
			case "删拦截":
				JSONArray interceptJsonArray = groupEntity.getInterceptJson();
				delManager(interceptJsonArray, content);
				groupEntity.setInterceptJson(interceptJsonArray);
				break;
			case "删微博监控":
				JSONArray weiboJsonArray = groupEntity.getWeiboJson();
				delMonitorList(weiboJsonArray, content);
				groupEntity.setWeiboJson(weiboJsonArray);
				break;
			case "删哔哩哔哩监控":
				JSONArray biliBiliJsonArray = groupEntity.getBiliBiliJson();
				delMonitorList(biliBiliJsonArray, content);
				groupEntity.setBiliBiliJson(biliBiliJsonArray);
				break;
			case "删哔哩哔哩开播提醒":
				JSONArray biliBiliLiveJsonArray = groupEntity.getBiliBiliLiveJson();
				delMonitorList(biliBiliLiveJsonArray, content);
				groupEntity.setBiliBiliLiveJson(biliBiliLiveJsonArray);
				break;
		}
		groupService.save(groupEntity);
		return type + "成功！！";
	}

	private void delManager(JSONArray jsonArray, String content){
		for (int i = 0; i < jsonArray.size(); i++){
			String str = jsonArray.getString(i);
			if (content.equals(str)){
				jsonArray.remove(str);
				break;
			}
		}
	}

	private void delMonitorList(JSONArray jsonArray, String username){
		List<JSONObject> list = new ArrayList<>();
		jsonArray.forEach(obj -> {
			JSONObject jsonObject = (JSONObject) obj;
			if (username.equals(jsonObject.getString("name"))) list.add(jsonObject);
		});
		list.forEach(jsonArray::remove);
	}

	@Action("查违规词")
	@Synonym({"查黑名单", "查拦截", "查微博监控", "查哔哩哔哩监控",
			"查哔哩哔哩开播提醒", "查问答", "查指令限制", "查shell"})
	@QMsg(at = true)
	public String query(GroupEntity groupEntity, @PathVar(0) String type){
		StringBuilder sb = new StringBuilder();
		switch (type){
			case "查黑名单":
				sb.append("本群黑名单列表如下：").append("\n");
				groupEntity.getBlackJson().forEach(obj -> sb.append(obj).append("\n"));
				break;
			case "查违规词":
				sb.append("本群违规词列表如下：").append("\n");
				groupEntity.getViolationJson().forEach(obj -> sb.append(obj).append("\n"));
				break;
			case "查拦截":
				sb.append("本群被拦截的指令列表如下：").append("\n");
				groupEntity.getInterceptJson().forEach(obj -> sb.append(obj).append("\n"));
				break;
			case "查微博监控":
				sb.append("本群微博监控列表如下：").append("\n");
				groupEntity.getWeiboJson().forEach( obj -> {
					JSONObject weiboJsonObject = (JSONObject) obj;
					sb.append(weiboJsonObject.getString("id")).append("-")
							.append(weiboJsonObject.getString("name")).append("\n");
				});
				break;
			case "查哔哩哔哩监控":
				sb.append("本群哔哩哔哩监控列表如下：").append("\n");
				groupEntity.getBiliBiliJson().forEach( obj -> {
					JSONObject biliBiliJsonObject = (JSONObject) obj;
					sb.append(biliBiliJsonObject.getString("id")).append("-")
							.append(biliBiliJsonObject.getString("name")).append("\n");
				});
				break;
			case "查哔哩哔哩开播提醒":
				sb.append("本群哔哩哔哩开播提醒如下：").append("\n");
				groupEntity.getBiliBiliLiveJson().forEach( obj -> {
					JSONObject biliBiliJsonObject = (JSONObject) obj;
					sb.append(biliBiliJsonObject.getString("id")).append("-")
							.append(biliBiliJsonObject.getString("name")).append("\n");
				});
				break;
			case "查问答":
				sb.append("本群问答列表如下：").append("\n");
				groupEntity.getQaJson().forEach(obj -> {
					JSONObject jsonObject = (JSONObject) obj;
					sb.append(jsonObject.getString("q")).append("-").append(jsonObject.getString("type")).append("\n");
				});
				break;
			case "查指令限制":
				sb.append("本群的指令限制列表如下：").append("\n");
				groupEntity.getCommandLimitJson().forEach((k, v) ->
						sb.append(k).append("->").append(v).append("次").append("\n"));
				break;
			case "查shell":
				sb.append("本群的shell命令存储如下").append("\n");
				groupEntity.getShellCommandJson().forEach(obj -> {
					JSONObject shellCommandJsonObject = (JSONObject) obj;
					sb.append(shellCommandJsonObject.getInteger("auth")).append("->")
							.append(shellCommandJsonObject.getString("command")).append("->")
							.append(shellCommandJsonObject.getString("shell"));
				});
				break;
			default: return null;
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

}

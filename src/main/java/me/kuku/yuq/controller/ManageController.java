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
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.At;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.QQEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.entity.RecallEntity;
import me.kuku.yuq.logic.*;
import me.kuku.yuq.pojo.BiliBiliPojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.WeiboPojo;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.QQService;
import me.kuku.yuq.service.RecallService;
import me.kuku.yuq.utils.BotUtils;
import net.mamoe.mirai.contact.PermissionDeniedException;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@GroupController
@SuppressWarnings("unused")
public class ManageController {

	@Inject
	private GroupService groupService;
	@Config("YuQ.Mirai.bot.master")
	private String master;

	@Before
	public GroupEntity before(long group, long qq){
		GroupEntity groupEntity = groupService.findByGroup(group);
		if (groupEntity == null) groupEntity = new GroupEntity();
		if (String.valueOf(qq).equals(master)) return groupEntity;
		else throw FunKt.getMif().at(qq).plus("抱歉，您的权限不足，无法执行！！").toThrowable();
	}

	@Action("加管 {qqNum}")
	@Synonym({"加超管 {qqNum}"})
	@QMsg(at = true)
	public String addManager(GroupEntity groupEntity, @PathVar(0) String type, Long qqNum){
		switch (type){
			case "加管":
				groupEntity.setAdminJsonArray(groupEntity.getAdminJsonArray().fluentAdd(qqNum.toString()));
				break;
			case "加超管":
				groupEntity.setSuperAdminJsonArray(groupEntity.getSuperAdminJsonArray().fluentAdd(qqNum.toString()));
				break;
			default: return null;
		}
		groupService.save(groupEntity);
		return type + "成功！！";
	}

	@Action("删管 {qqNum}")
	@Synonym({"删超管 {qqNum}"})
	@QMsg(at = true)
	public String delManager(GroupEntity groupEntity, @PathVar(0) String type, Long qqNum){
		switch (type){
			case "删管":
				JSONArray adminJsonArray = groupEntity.getAdminJsonArray();
				BotUtils.delManager(adminJsonArray, qqNum.toString());
				groupEntity.setAdminJsonArray(adminJsonArray);
				break;
			case "删超管":
				JSONArray superAdminJsonArray = groupEntity.getSuperAdminJsonArray();
				BotUtils.delManager(superAdminJsonArray, qqNum.toString());
				groupEntity.setSuperAdminJsonArray(superAdminJsonArray);
				break;
			default: return null;
		}
		groupService.save(groupEntity);
		return type + "成功！！";
	}

	@Action("加shell {command}")
	@QMsg(at = true)
	public String addShellCommand(GroupEntity groupEntity, String command, Group group, long qq, ContextSession session){
		JSONArray jsonArray = groupEntity.getShellCommandJsonArray();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			if (singleJsonObject.getString("command").equals(command)){
				return "指令重复，请先删除该指令再添加！！";
			}
		}
		At at = FunKt.getMif().at(qq);
		group.sendMessage(at.plus("请输入命令权限设置，0为主人，1为超管，2为普管，3为用户"));
		Message authMessage = session.waitNextMessage();
		String authStr = BotUtils.firstString(authMessage);
		int auth = Integer.parseInt(authStr);
		group.sendMessage(at.plus("请输入需要执行的shell命令"));
		Message shellMessage = session.waitNextMessage();
		String shell = BotUtils.firstString(shellMessage);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("auth", auth);
		jsonObject.put("shell", shell);
		jsonObject.put("command", command);
		jsonArray.add(jsonObject);
		groupEntity.setShellCommandJsonArray(jsonArray);
		groupService.save(groupEntity);
		return "添加shell指令成功！！";
	}

	@Action("删shell {command}")
	@QMsg(at = true)
	public String delShellCommand(GroupEntity groupEntity, String command){
		JSONArray jsonArray = groupEntity.getShellCommandJsonArray();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			if (singleJsonObject.getString("command").equals(command)){
				jsonArray.remove(i);
				groupEntity.setShellCommandJsonArray(jsonArray);
				groupService.save(groupEntity);
				return "删除shell指令成功！！";
			}
		}
		return "没有找到这个shell指令！！";
	}

	@Action("群管理权限 {status}")
	@QMsg(at = true)
	public String groupAdminAuth(GroupEntity groupEntity, boolean status){
		groupEntity.setGroupAdminAuth(status);
		groupService.save(groupEntity);
		if (status) return "群管理权限开启成功！！";
		else return "群管理权限关闭成功！！";
	}

	@GroupController
	public static class ManageAdminController {
		@Config("YuQ.Mirai.bot.master")
		private String master;
		@Inject
		private GroupService groupService;

		@Before
		public GroupEntity before(Member qq, long group){
			GroupEntity groupEntity = groupService.findByGroup(group);
			if (groupEntity == null) groupEntity = new GroupEntity(group);
			if (groupEntity.isAdmin(qq.getId()) || groupEntity.isSuperAdmin(qq.getId())
					|| qq.getId() == Long.parseLong(master) || (qq.isAdmin() && Boolean.valueOf(true).equals(groupEntity.getGroupAdminAuth()))){
				return groupEntity;
			}else throw FunKt.getMif().at(qq).plus("您的权限不足，无法执行！！").toThrowable();
		}

		@Action("清屏")
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

		@Action("kukubot {status}")
		@Synonym({"loc监控 {status}", "整点报时 {status}", "自动审核 {status}",
				"欢迎语 {status}", "退群拉黑 {status}", "鉴黄 {status}", "色图 {status}",
				"撤回通知 {status}", "闪照通知 {status}", "复读 {status}", "语音识别 {status}",
				"进群未发言踢出 {status}"})
		@QMsg(at = true)
		public String onOrOff(GroupEntity groupEntity, boolean status, @PathVar(0) String op){
			switch (op){
				case "kukubot": groupEntity.setStatus(status); break;
				case "loc监控": groupEntity.setLocMonitor(status); break;
				case "整点报时": groupEntity.setOnTimeAlarm(status); break;
				case "自动审核": groupEntity.setAutoReview(status); break;
				case "欢迎语": groupEntity.setWelcomeMsg(status); break;
				case "退群拉黑": groupEntity.setLeaveGroupBlack(status); break;
				case "鉴黄": groupEntity.setPic(status); break;
				case "色图": groupEntity.setColorPic(status); break;
				case "撤回通知": groupEntity.setRecall(status); break;
				case "闪照通知": groupEntity.setFlashNotify(status); break;
				case "复读": groupEntity.setRepeat(status); break;
				case "语音识别": groupEntity.setVoiceIdentify(status); break;
				case "进群未发言踢出": groupEntity.setKickWithoutSpeaking(status); break;
				default: return null;
			}
			groupService.save(groupEntity);
			if (status) return op + "开启成功";
			else return op + "关闭成功";
		}
	}

	@GroupController
	public static class ManageSuperAdminController {
		@Config("YuQ.Mirai.bot.master")
		private String master;
		@Inject
		private GroupService groupService;
		@Inject
		private WeiboLogic weiboLogic;
		@Inject
		private BiliBiliLogic biliBiliLogic;
		@Inject
		private QQService qqService;
		@Inject
		private BotLogic botLogic;
		@Inject
		private QQLoginLogic qqLoginLogic;
		@Inject
		private QQGroupLogic qqGroupLogic;

		@Before
		public GroupEntity before(long group, Member qq){
			GroupEntity groupEntity = groupService.findByGroup(group);
			if (String.valueOf(qq.getId()).equals(master) || groupEntity.isSuperAdmin(qq.getId()) ||
					(qq.isAdmin() && Boolean.valueOf(true).equals(groupEntity.getGroupAdminAuth()))) return groupEntity;
			else throw FunKt.getMif().at(qq).plus("您的权限不足，无法执行！！").toThrowable();
		}

		@Action("加违规词 {content}")
		@Synonym({"加黑名单 {content}", "加白名单 {content}", "加拦截 {content}",
				"加微博监控 {content}", "加哔哩哔哩监控 {content}"})
		@QMsg(at = true)
		public String add(GroupEntity groupEntity, @PathVar(0) String type, String content, ContextSession session, long qq) throws IOException {
			switch (type){
				case "加违规词":
					groupEntity.setViolationJsonArray(groupEntity.getViolationJsonArray().fluentAdd(content));
					break;
				case "加黑名单":
					groupEntity.setBlackJsonArray(groupEntity.getBlackJsonArray().fluentAdd(content));
					break;
				case "加白名单":
					groupEntity.setWhiteJsonArray(groupEntity.getWhiteJsonArray().fluentAdd(content));
					break;
				case "加拦截":
					groupEntity.setInterceptJsonArray(groupEntity.getInterceptJsonArray().fluentAdd(content));
					break;
				case "加微博监控":
					Result<List<WeiboPojo>> result = weiboLogic.getIdByName(content);
					if (result.getCode() != 200) return "该用户不存在！！";
					WeiboPojo weiboPojo = result.getData().get(0);
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", weiboPojo.getUserId());
					jsonObject.put("name", weiboPojo.getName());
					groupEntity.setWeiboJsonArray(groupEntity.getWeiboJsonArray().fluentAdd(jsonObject));
					break;
				case "加哔哩哔哩监控":
					Result<List<BiliBiliPojo>> blResult = biliBiliLogic.getIdByName(content);
					if (blResult.getCode() != 200) return "该用户不存在";
					BiliBiliPojo biliBiliPojo = blResult.getData().get(0);
					JSONObject blJsonObject = new JSONObject();
					blJsonObject.put("id", biliBiliPojo.getUserId());
					blJsonObject.put("name", biliBiliPojo.getName());
					groupEntity.setBiliBiliJsonArray(groupEntity.getBiliBiliJsonArray().fluentAdd(blJsonObject));
					break;
				default: return null;
			}
			groupService.save(groupEntity);
			return type + "成功";
		}

		@Action("删违规词 {content}")
		@Synonym({"删黑名单 {content}", "删白名单 {content}", "删拦截 {content}",
				"删微博监控 {content}", "删哔哩哔哩监控 {content}"})
		@QMsg(at = true)
		public String del(GroupEntity groupEntity, @PathVar(0) String type, String content){
			switch (type){
				case "删违规词":
					JSONArray violationJsonArray = groupEntity.getViolationJsonArray();
					BotUtils.delManager(violationJsonArray, content);
					groupEntity.setViolationJsonArray(violationJsonArray);
					break;
				case "删黑名单":
					JSONArray blackJsonArray = groupEntity.getBlackJsonArray();
					BotUtils.delManager(blackJsonArray, content);
					groupEntity.setBlackJsonArray(blackJsonArray);
					break;
				case "删白名单":
					JSONArray whiteJsonArray = groupEntity.getWhiteJsonArray();
					BotUtils.delManager(whiteJsonArray, content);
					groupEntity.setWhiteJsonArray(whiteJsonArray);
					break;
				case "删拦截":
					JSONArray interceptJsonArray = groupEntity.getInterceptJsonArray();
					BotUtils.delManager(interceptJsonArray, content);
					groupEntity.setInterceptJsonArray(interceptJsonArray);
					break;
				case "删微博监控":
					JSONArray weiboJsonArray = groupEntity.getWeiboJsonArray();
					BotUtils.delMonitorList(weiboJsonArray, content);
					groupEntity.setWeiboJsonArray(weiboJsonArray);
					break;
				case "删哔哩哔哩监控":
					JSONArray biliBiliJsonArray = groupEntity.getBiliBiliJsonArray();
					BotUtils.delMonitorList(biliBiliJsonArray, content);
					groupEntity.setBiliBiliJsonArray(biliBiliJsonArray);
					break;
				default: return null;
			}
			groupService.save(groupEntity);
			return type + "成功！！";
		}

		@Action("全体禁言 {status}")
		public String allShutUp(long group, boolean status) throws IOException {
			QQLoginEntity qqLoginEntity = botLogic.getQQLoginEntity();
			return qqLoginLogic.allShutUp(qqLoginEntity, group, status);
		}

		@Action("t {qqNo}")
		@QMsg(at = true)
		public String kick(Member qqNo) throws IOException {
			try {
				qqNo.kick("");
				return "踢出成功！！";
			} catch (PermissionDeniedException e) {
				return "权限不足，踢出失败！！";
			} catch (Exception e){
				return qqGroupLogic.deleteGroupMember(botLogic.getQQLoginEntity(), qqNo.getId(), qqNo.getGroup().getId(), true);
			}
		}

		@Action("违规次数 {count}")
		@QMsg(at = true)
		public String maxViolationCount(GroupEntity groupEntity, int count){
			groupEntity.setMaxViolationCount(count);
			groupService.save(groupEntity);
			return "已设置本群最大违规次数为" + count + "次";
		}

		@Action("清除违规 {qqNum}")
		public String clear(GroupEntity groupEntity, long qq){
			QQEntity qqEntity = qqService.findByQQAndGroup(qq, groupEntity.getGroup());
			if (qqEntity == null) qqEntity = new QQEntity(qq, groupEntity);
			qqEntity.setViolationCount(0);
			qqService.save(qqEntity);
			return "清除违规成功！！";
		}

		@Action("指令限制 {count}")
		@QMsg(at = true)
		public String maxCommandCount(GroupEntity groupEntity, int count){
			groupEntity.setMaxCommandCountOnTime(count);
			groupService.save(groupEntity);
			return "已设置本群单个指令每人十分钟最大触发次数为" + count + "次";
		}

		@Action("色图切换 {type}")
		@QMsg(at = true)
		public String colorPicType(GroupEntity groupEntity, String type){
			String colorPicType;
			if ("lolicon".equals(type) || "loliconR18".equals(type) || "quickly".equals(type)){
				colorPicType = type;
			}else return "没有该类型，请重试！！";
			groupEntity.setColorPicType(colorPicType);
			groupService.save(groupEntity);
			return "色图切换成" + type + "成功！！";
		}

		@Action("加指令限制 {command} {count}")
		@QMsg(at = true)
		public String addCommandLimit(GroupEntity groupEntity, String command, int count){
			JSONObject jsonObject = groupEntity.getCommandLimitJsonObject();
			jsonObject.put(command, count);
			groupEntity.setCommandLimitJsonObject(jsonObject);
			groupService.save(groupEntity);
			return "加指令限制成功！！已设置指令{" + command + "}十分钟之内只会响应" + count + "次";
		}

		@Action("删指令限制 {command}")
		@QMsg(at = true)
		public String delCommandLimit(GroupEntity groupEntity, String command){
			JSONObject jsonObject = groupEntity.getCommandLimitJsonObject();
			jsonObject.remove(command);
			groupEntity.setCommandLimitJsonObject(jsonObject);
			groupService.save(groupEntity);
			return "删指令{" + command + "}限制成功！！";
		}

		@Action("加问答 {q}")
		@QMsg(at = true)
		public String qa(ContextSession session, long qq, GroupEntity groupEntity, String q, Group group, @PathVar(2) String type){
			MessageItemFactory mif = FunKt.getMif();
			group.sendMessage(mif.at(qq).plus("请输入回答语句！！"));
			Message a = session.waitNextMessage();
			JSONObject jsonObject = new JSONObject();
			JSONArray aJsonArray = BotUtils.messageToJsonArray(a);
			jsonObject.put("q", q);
			jsonObject.put("a", aJsonArray);
			if (type == null) type = "PARTIAL";
			if (!"ALL".equalsIgnoreCase(type)) type = "PARTIAL";
			else type = "ALL";
			jsonObject.put("type", type);
			JSONArray jsonArray = groupEntity.getQaJsonArray();
			jsonArray.add(jsonObject);
			groupEntity.setQaJsonArray(jsonArray);
			groupService.save(groupEntity);
			return "添加问答成功！！";
		}

		@Action("删问答 {q}")
		@QMsg(at = true)
		public String delQa(GroupEntity groupEntity, String q){
			JSONArray qaJsonArray = groupEntity.getQaJsonArray();
			List<JSONObject> delList = new ArrayList<>();
			for (int i = 0; i < qaJsonArray.size(); i++){
				JSONObject jsonObject = qaJsonArray.getJSONObject(i);
				if (q.equals(jsonObject.getString("q"))){
					delList.add(jsonObject);
				}
			}
			delList.forEach(qaJsonArray::remove);
			groupEntity.setQaJsonArray(qaJsonArray);
			groupService.save(groupEntity);
			return "删除问答成功！！";
		}
	}

	@GroupController
	public static class ManageNotController {
		@Inject
		private GroupService groupService;
		@Inject
		private RecallService recallService;
		@Inject
		private ToolLogic toolLogic;
		@Config("YuQ.Mirai.bot.version")
		private String version;

		@Before
		public GroupEntity before(Long group){
			GroupEntity groupEntity = groupService.findByGroup(group);
			if (groupEntity == null) groupEntity = new GroupEntity(group);
			return groupEntity;
		}

		@Action("查管")
		@Synonym({"查黑名单", "查白名单", "查违规词", "查拦截", "查微博监控", "查哔哩哔哩监控", "查问答", "查超管", "查指令限制", "查shell"})
		@QMsg(at = true, atNewLine = true)
		public String query(GroupEntity groupEntity, @PathVar(0) String type){
			StringBuilder sb = new StringBuilder();
			switch (type){
				case "查管":
					sb.append("本群管理员列表如下：").append("\n");
					groupEntity.getAdminJsonArray().forEach(obj -> sb.append(obj).append("\n"));
					break;
				case "查超管":
					sb.append("本群超级管理员列表如下").append("\n");
					groupEntity.getSuperAdminJsonArray().forEach(obj -> sb.append(obj).append("\n"));
					break;
				case "查黑名单":
					sb.append("本群黑名单列表如下：").append("\n");
					groupEntity.getBlackJsonArray().forEach(obj -> sb.append(obj).append("\n"));
					break;
				case "查白名单":
					sb.append("本群白名单列表如下：").append("\n");
					groupEntity.getWhiteJsonArray().forEach(obj -> sb.append(obj).append("\n"));
					break;
				case "查违规词":
					sb.append("本群违规词列表如下：").append("\n");
					groupEntity.getViolationJsonArray().forEach(obj -> sb.append(obj).append("\n"));
					break;
				case "查拦截":
					sb.append("本群被拦截的指令列表如下：").append("\n");
					groupEntity.getInterceptJsonArray().forEach(obj -> sb.append(obj).append("\n"));
					break;
				case "查微博监控":
					sb.append("本群微博监控列表如下：").append("\n");
					groupEntity.getWeiboJsonArray().forEach( obj -> {
						JSONObject weiboJsonObject = (JSONObject) obj;
						sb.append(weiboJsonObject.getString("id")).append("-")
								.append(weiboJsonObject.getString("name")).append("\n");
					});
					break;
				case "查哔哩哔哩监控":
					sb.append("本群哔哩哔哩监控列表如下：").append("\n");
					groupEntity.getBiliBiliJsonArray().forEach( obj -> {
						JSONObject biliBiliJsonObject = (JSONObject) obj;
						sb.append(biliBiliJsonObject.getString("id")).append("-")
								.append(biliBiliJsonObject.getString("name")).append("\n");
					});
					break;
				case "查问答":
					sb.append("本群问答列表如下：").append("\n");
					groupEntity.getQaJsonArray().forEach(obj -> {
						JSONObject jsonObject = (JSONObject) obj;
						sb.append(jsonObject.getString("q")).append("\n");
					});
					break;
				case "查指令限制":
					sb.append("本群的指令限制列表如下：").append("\n");
					groupEntity.getCommandLimitJsonObject().forEach((k, v) ->
							sb.append(k).append("->").append(v).append("次").append("\n"));
					break;
				case "查shell":
					sb.append("本群的shell命令存储如下").append("\n");
					groupEntity.getShellCommandJsonArray().forEach(obj -> {
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

		@Action("查撤回 {qqNo}")
		public Message queryRecall(long group, long qqNo, long qq, @PathVar(value = 2, type = PathVar.Type.Integer) Integer num){
			List<RecallEntity> recallList = recallService.findByGroupAndQQ(group, qqNo);
			int all = recallList.size();
			if (num == null) num = 1;
			if (num > all || num < 0) return FunKt.getMif().at(qq).plus("您要查询的QQ只有" + all + "条撤回消息，超过范围了！！");
			RecallEntity recallEntity = recallList.get(num - 1);
			String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recallEntity.getDate());
			return FunKt.getMif().at(qq).plus("\n该消息撤回时间为" + timeStr + "\n消息内容为：\n")
					.plus(BotUtils.jsonArrayToMessage(recallEntity.getMessageEntity().getContentJsonArray()));
		}

		@Action("检查版本")
		public String checkUpdate() throws IOException {
			String gitVersion = toolLogic.queryVersion();
			return "当前程序版本：" + this.version + "\n" +
					"最新程序版本：" + gitVersion;
		}

		@Action("开关")
		@QMsg(at = true, atNewLine = true)
		public String kai(GroupEntity groupEntity){
			StringBuilder sb = new StringBuilder("本群开关情况如下：\n");
			sb.append("色图：").append(this.boolToStr(groupEntity.getColorPic())).append("、").append(groupEntity.getColorPicType()).append("\n");
			sb.append("鉴黄：").append(this.boolToStr(groupEntity.getPic())).append("\n");
			sb.append("欢迎语：").append(this.boolToStr(groupEntity.getWelcomeMsg())).append("\n");
			sb.append("退群拉黑：").append(this.boolToStr(groupEntity.getLeaveGroupBlack())).append("\n");
			sb.append("自动审核：").append(this.boolToStr(groupEntity.getAutoReview())).append("\n");
			sb.append("撤回通知：").append(this.boolToStr(groupEntity.getRecall())).append("\n");
			sb.append("整点报时：").append(this.boolToStr(groupEntity.getOnTimeAlarm())).append("\n");
			sb.append("闪照通知：").append(this.boolToStr(groupEntity.getFlashNotify())).append("\n");
			Integer maxCommandCountOnTime = groupEntity.getMaxCommandCountOnTime();
			if (maxCommandCountOnTime == null) maxCommandCountOnTime = -1;
			String ss = maxCommandCountOnTime.toString();
			if (maxCommandCountOnTime < 0) ss = "无限制";
			sb.append("指令限制：").append(ss).append("\n");
			Integer maxViolationCount = groupEntity.getMaxViolationCount();
			if (maxViolationCount == null) maxViolationCount = 5;
			sb.append("最大违规次数：").append(maxViolationCount);
			return sb.toString();
		}

		private String boolToStr(Boolean b){
			if (b == null || !b) return "关";
			else return "开";
		}
	}

}

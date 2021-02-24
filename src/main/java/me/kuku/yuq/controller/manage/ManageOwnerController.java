package me.kuku.yuq.controller.manage;

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
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;

@GroupController
@SuppressWarnings("unused")
public class ManageOwnerController {

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

}

package me.kuku.yuq.controller.manage;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.alibaba.fastjson.JSONArray;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
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

}

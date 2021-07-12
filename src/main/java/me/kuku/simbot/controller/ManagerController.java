package me.kuku.simbot.controller;

import love.forte.simbot.annotation.ContextValue;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.ListenGroup;
import love.forte.simbot.annotation.OnGroup;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.service.GroupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@OnGroup
@ListenGroup("manager")
@Service
public class ManagerController {

	@Resource
	private GroupService groupService;

	@RegexFilter("kukubot{{statusStr}}")
	public String openOrOff(@ContextValue("groupEntity") GroupEntity groupEntity,
	                      @FilterValue("statusStr") String statusStr){
		boolean status = statusStr.contains("开");
		groupEntity.setStatus(status);
		groupService.save(groupEntity);
		return "kukubot" + (status ? "开启" : "关闭") + "成功！";
	}



}

package me.kuku.simbot.controller;

import catcode.StringTemplate;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.HostLocEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.HostLocLogic;
import me.kuku.simbot.service.HostLocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@ListenGroup("hostLoc")
@OnGroup
public class HostLocController {

	@Autowired
	private HostLocService hostLocService;
	@Autowired
	private HostLocLogic hostLocLogic;
	@Autowired
	private StringTemplate stringTemplate;

	@OnPrivate
	@ListenGroup(value = "", append = false)
	@RegexFilter("loc {{username}} {{password}}")
	public String login(@ContextValue("qqEntity")QqEntity qqEntity,
	                  @FilterValue("username") String username,
	                  @FilterValue("password") String password) throws IOException {
		Result<String> result = hostLocLogic.login(username, password);
		if (result.isFailure()) return result.getMessage();
		HostLocEntity hostLocEntity = hostLocService.findByQqEntity(qqEntity);
		if (hostLocEntity == null) hostLocEntity = new HostLocEntity(qqEntity);
		hostLocEntity.setUsername(username);
		hostLocEntity.setPassword(password);
		hostLocEntity.setCookie(result.getData());
		hostLocService.save(hostLocEntity);
		return "绑定HostLoc成功！";
	}

	@Filter("loc签到")
	public String locSign(@ContextValue("hostLocEntity") HostLocEntity hostLocEntity,
	                    MsgSender msgSender, GroupMsg groupMsg) throws IOException {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + "正在为您进行loc签到中，请稍后！");
		String cookie = hostLocEntity.getCookie();
		boolean isLogin = hostLocLogic.isLogin(cookie);
		if (isLogin) hostLocLogic.sign(cookie);
		else {
			Result<String> result = hostLocLogic.login(hostLocEntity.getUsername(), hostLocEntity.getPassword());
			if (result.getCode() == 200){
				hostLocEntity.setCookie(result.getData());
				hostLocService.save(hostLocEntity);
				hostLocLogic.sign(cookie);
			}else return "loc签到失败，失败原因：" + result.getMessage();
		}
		return "loc签到成功！";
	}




}

package me.kuku.simbot.controller;

import catcode.StringTemplate;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.HostLocEntity;
import me.kuku.simbot.entity.HostLocService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.HostLocLogic;
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
	public String login(@ContextValue("qq") QqEntity qqEntity,
	                  @FilterValue("username") String username,
	                  @FilterValue("password") String password) throws IOException {
		Result<String> result = hostLocLogic.login(username, password);
		if (result.isFailure()) return result.getMessage();
		HostLocEntity hostLocEntity = hostLocService.findByQqEntity(qqEntity);
		if (hostLocEntity == null) hostLocEntity = HostLocEntity.Companion.getInstance(qqEntity);
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
			return "loc签到失败，cookie失效，请重新登录！";
		}
		return "loc签到成功！";
	}

	@RegexFilter("loc{{type,签到|监控}}{{statusStr}}")
	public String status(@ContextValue("hostLocEntity") HostLocEntity hostLocEntity,
	                     @FilterValue("statusStr") String statusStr, @FilterValue("type") String type){
		boolean status = statusStr.contains("开");
		switch (type){
			case "监控": hostLocEntity.setMonitor(status); break;
			case "签到": hostLocEntity.setSign(status); break;
		}
		hostLocService.save(hostLocEntity);
		return "loc" + type + (status ? "开启成功" : "关闭成功");
	}
}

package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.HostLocEntity;
import me.kuku.yuq.entity.HostLocService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.logic.HostLocLogic;

import javax.inject.Inject;
import java.io.IOException;

@GroupController
@PrivateController
public class HostLocController {

	@Inject
	private HostLocService hostLocService;
	@Inject
	private HostLocLogic hostLocLogic;
	@Inject
	private MessageItemFactory mif;

	@Action("loc {username} {password}")
	public String login(QqEntity qqEntity, String username, String password) throws IOException {
		Result<String> result = hostLocLogic.login(username, password);
		if (result.isFailure()) return result.getMessage();
		HostLocEntity hostLocEntity = hostLocService.findByQqEntity(qqEntity);
		if (hostLocEntity == null) hostLocEntity = HostLocEntity.Companion.getInstance(qqEntity);
		hostLocEntity.setCookie(result.getData());
		hostLocService.save(hostLocEntity);
		return "绑定HostLoc成功！";
	}

	@Before(except = "login")
	public HostLocEntity before(QqEntity qqEntity){
		HostLocEntity hostLocEntity = hostLocService.findByQqEntity(qqEntity);
		if (hostLocEntity == null)
			throw mif.at(qqEntity.getQq()).plus("您还未绑定HostLoc账号，请先绑定HostLoc账号！").toThrowable();
		else return hostLocEntity;
	}

	@Action("loc签到")
	@QMsg(at = true)
	public String locSign(HostLocEntity hostLocEntity, long qq, Group group) throws IOException {
		group.sendMessage(mif.at(qq).plus("正在为您进行loc签到中，请稍后！"));
		String cookie = hostLocEntity.getCookie();
		boolean isLogin = hostLocLogic.isLogin(cookie);
		if (isLogin) hostLocLogic.sign(cookie);
		else {
			return "loc签到失败，cookie失效，请重新登录！";
		}
		return "loc签到成功！";
	}

	@Action("loc签到 {status}")
	@Synonym({"loc监控 {status}"})
	public String status(HostLocEntity hostLocEntity, boolean status, @PathVar(0) String type){
		switch (type){
			case "loc监控": hostLocEntity.setMonitor(status); break;
			case "loc签到": hostLocEntity.setSign(status); break;
		}
		hostLocService.save(hostLocEntity);
		return type + (status ? "开启成功" : "关闭成功");
	}

}

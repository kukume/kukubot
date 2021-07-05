package me.kuku.simbot.controller;

import love.forte.simbot.annotation.*;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.NetEaseEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.NetEaseLogic;
import me.kuku.simbot.service.NetEaseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class NetEaseController {

	@Resource
	private NetEaseService netEaseService;
	@Resource
	private NetEaseLogic netEaseLogic;

	@OnPrivate
	@RegexFilter("网易 {{phone}} {{password}}")
	public String login(@FilterValue("phone") String phone, @FilterValue("password") String password,
	                    @ContextValue("qq") QqEntity qqEntity) throws IOException {
		Result<NetEaseEntity> result = netEaseLogic.loginByPhone(phone, password);
		if (result.isSuccess()){
			NetEaseEntity netEaseEntity = netEaseService.findByQqEntity(qqEntity);
			if (netEaseEntity == null) netEaseEntity = new NetEaseEntity(qqEntity);
			NetEaseEntity newNetEaseEntity = result.getData();
			netEaseEntity.setMusicU(newNetEaseEntity.getMusicU());
			netEaseEntity.setCsrf(newNetEaseEntity.getCsrf());
			netEaseService.save(netEaseEntity);
			return "绑定网易成功！";
		}else return result.getMessage();
	}

	@OnGroup
	@Filter("网易")
	public String sign(@ContextValue("qq") QqEntity qqEntity) throws IOException {
		NetEaseEntity netEaseEntity = netEaseService.findByQqEntity(qqEntity);
		if (netEaseEntity == null) return "您还没有绑定网易账号，请私聊机器人进行绑定！";
		Result<?> result = netEaseLogic.sign(netEaseEntity);
		if (result.isFailure()) return "网易云音乐签到失败！失败原因：" + result.getMessage();
		netEaseLogic.listeningVolume(netEaseEntity);
		return "网易云音乐签到成功！";
	}
}

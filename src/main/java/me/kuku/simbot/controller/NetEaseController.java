package me.kuku.simbot.controller;

import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.NetEaseEntity;
import me.kuku.simbot.entity.NetEaseService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.NetEaseLogic;
import me.kuku.simbot.pojo.NetEaseQrcode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class NetEaseController {

	@Resource
	private NetEaseService netEaseService;
	@Resource
	private NetEaseLogic netEaseLogic;
	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Resource
	private MessageContentBuilderFactory messageContentBuilderFactory;

	@OnPrivate
	@RegexFilter("网易 {{phone}} {{password}}")
	public String login(String phone, String password, QqEntity qqEntity) throws IOException {
		Result<NetEaseEntity> result = netEaseLogic.loginByPhone(phone, password);
		if (result.isSuccess()){
			NetEaseEntity netEaseEntity = netEaseService.findByQqEntity(qqEntity);
			if (netEaseEntity == null) netEaseEntity = NetEaseEntity.Companion.getInstance(qqEntity);
			NetEaseEntity newNetEaseEntity = result.getData();
			netEaseEntity.setMusicU(newNetEaseEntity.getMusicU());
			netEaseEntity.setCsrf(newNetEaseEntity.getCsrf());
			netEaseService.save(netEaseEntity);
			return "绑定网易成功！";
		}else return result.getMessage();
	}

	@RegexFilter("网易二维码")
	@OnGroup
	public void qrcode(MsgSender msgSender, long group, QqEntity qqEntity) throws IOException {
		NetEaseQrcode netEaseQrcode = netEaseLogic.loginByQrcode();
		long qq = qqEntity.getQq();
		MessageContent messageContent = messageContentBuilderFactory.getMessageContentBuilder()
				.at(qq).image(netEaseQrcode.getBytes()).text("请使用网易云音乐APP扫码登录！").build();
		msgSender.SENDER.sendGroupMsg(group, messageContent);
		threadPoolTaskExecutor.execute(() -> {
			String msg;
			try {
				while (true) {
					TimeUnit.SECONDS.sleep(3);
					Result<NetEaseEntity> result = netEaseLogic.checkQrcode(netEaseQrcode);
					if (result.isSuccess()){
						NetEaseEntity netEaseEntity = netEaseService.findByQqEntity(qqEntity);
						if (netEaseEntity == null) netEaseEntity = NetEaseEntity.Companion.getInstance(qqEntity);
						NetEaseEntity newEntity = result.getData();
						netEaseEntity.setCsrf(newEntity.getCsrf());
						netEaseEntity.setMusicU(newEntity.getMusicU());
						netEaseService.save(netEaseEntity);
						msg = "绑定网易云音乐成功！";
						break;
					}else if (result.getCode() == 500){
						msg = result.getMessage();
						break;
					}
				}
			} catch (Exception e) {
				msg = "出现异常了，异常信息为" + e.getMessage();
			}
			msgSender.SENDER.sendGroupMsg(group, messageContentBuilderFactory.getMessageContentBuilder()
					.at(qq).text(msg).build());
		});
	}

	@OnGroup
	@Filter("网易")
	public String sign(QqEntity qqEntity) throws IOException {
		NetEaseEntity netEaseEntity = netEaseService.findByQqEntity(qqEntity);
		if (netEaseEntity == null) return "您还没有绑定网易账号，请私聊机器人进行绑定！";
		Result<?> result = netEaseLogic.sign(netEaseEntity);
		if (result.isFailure()) return "网易云音乐签到失败！失败原因：" + result.getMessage();
		netEaseLogic.listeningVolume(netEaseEntity);
		return "网易云音乐签到成功！";
	}
}

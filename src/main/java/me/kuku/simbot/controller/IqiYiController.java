package me.kuku.simbot.controller;

import catcode.StringTemplate;
import love.forte.simbot.annotation.ContextValue;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.entity.IqiYiEntity;
import me.kuku.simbot.entity.IqiYiService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.IqiYiLogic;
import me.kuku.simbot.pojo.IqiYiQrcode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@OnGroup
public class IqiYiController {

	@Resource
	private IqiYiLogic iqiYiLogic;
	@Resource
	private IqiYiService iqiYiService;
	@Resource
	private MessageContentBuilderFactory messageContentBuilderFactory;
	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Resource
	private StringTemplate stringTemplate;

	@Filter("爱奇艺二维码")
	public void qrcode(GroupMsg groupMsg, MsgSender msgSender, @ContextValue("qq") QqEntity qqEntity) throws IOException {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		IqiYiQrcode qrcode = iqiYiLogic.getQrcode();
		MessageContent messageContent = messageContentBuilderFactory.getMessageContentBuilder()
				.at(qq).image(qrcode.getUrl()).text("请使用爱奇艺APP扫码登录！").build();
		msgSender.SENDER.sendGroupMsg(groupMsg, messageContent);
		threadPoolTaskExecutor.execute(() -> {
			String msg;
			try {
				while (true){
					try {
						TimeUnit.SECONDS.sleep(3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Result<IqiYiEntity> result = iqiYiLogic.checkQrcode(qrcode);
					Integer code = result.getCode();
					if (code == 200){
						IqiYiEntity newIqiYiEntity = result.getData();
						IqiYiEntity iqiYiEntity = iqiYiService.findByQqEntity(qqEntity);
						if (iqiYiEntity == null) iqiYiEntity = IqiYiEntity.Companion.getInstance(qqEntity);
						iqiYiEntity.setCookie(newIqiYiEntity.getCookie());
						iqiYiEntity.setPOne(newIqiYiEntity.getPOne());
						iqiYiEntity.setPThree(newIqiYiEntity.getPThree());
						iqiYiService.save(iqiYiEntity);
						msg = "绑定爱奇艺信息成功！";
						break;
					}else if (code == 500){
						msg = result.getMessage();
						break;
					}
				}
			}catch (IOException e){
				msg = "爱奇艺登录出现异常了，异常信息为：" + e.getMessage();
			}
			msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + msg);
		});
	}

	@Filter("爱奇艺签到")
	public String sign(@ContextValue("qq") QqEntity qqEntity) throws IOException {
		IqiYiEntity iqiYiEntity = iqiYiService.findByQqEntity(qqEntity);
		if (iqiYiEntity == null) return "您还没有绑定爱奇艺账号，请发送<爱奇艺二维码>进行绑定";
		Result<Void> result = iqiYiLogic.sign(iqiYiEntity);
		if (result.isFailure()) return result.getMessage();
		iqiYiLogic.task(iqiYiEntity);
		iqiYiLogic.draw(iqiYiEntity);
		return "爱奇艺签到成功！";
	}


}

package me.kuku.simbot.controller;

import catcode.StringTemplate;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.ListenGroup;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.annotation.SkipListenGroup;
import me.kuku.simbot.entity.HeyTapEntity;
import me.kuku.simbot.entity.HeyTapService;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.HeyTapLogic;
import me.kuku.simbot.pojo.HeyTapQrcode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
@OnGroup
@ListenGroup("heyTap")
public class HeyTapController {

	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Resource
	private StringTemplate stringTemplate;
	@Resource
	private MessageContentBuilderFactory messageContentBuilderFactory;
	@Resource
	private HeyTapService heyTapService;
	@Resource
	private HeyTapLogic heyTapLogic;

	@SkipListenGroup
	@OnPrivate
	@RegexFilter("欢太{{cookie}}")
	public String bindCookie(String cookie, QqEntity qqEntity){
		HeyTapEntity heyTapEntity = heyTapService.findByQqEntity(qqEntity);
		if (heyTapEntity == null) heyTapEntity = HeyTapEntity.Companion.getInstance(qqEntity);
		heyTapEntity.setCookie(cookie);
		heyTapService.save(heyTapEntity);
		return "绑定欢太成功！";
	}

	@SkipListenGroup
	@Filter("欢太二维码")
	public void getQrcode(QqEntity qqEntity, MsgSender msgSender, GroupMsg groupMsg) throws IOException {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		HeyTapQrcode qrcode = heyTapLogic.getQrcode();
		String url = qrcode.getUrl();
		msgSender.SENDER.sendGroupMsg(groupMsg, messageContentBuilderFactory.getMessageContentBuilder().at(qq).image(url).text("请使用oppo手机打开设置->我的->右上角扫码登录").build());
		threadPoolTaskExecutor.execute(() -> {
			String msg;
			try {
				while (true) {
					Result<HeyTapEntity> result = heyTapLogic.checkQrcode(qrcode);
					if (result.isSuccess()){
						HeyTapEntity entity = heyTapService.findByQqEntity(qqEntity);
						if (entity == null) entity = HeyTapEntity.Companion.getInstance(qqEntity);
						HeyTapEntity newEntity = result.getData();
						entity.setCookie(newEntity.getCookie());
						heyTapService.save(entity);
						msg = "绑定欢太账号成功！";
						break;
					}else if (result.getCode() == 500) {
						msg = result.getMessage();
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				msg = "出现异常了，请重试。异常信息为：" + e.getMessage();
			}
			msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + msg);
		});
	}

	@Filter("欢太签到")
	public String sign(HeyTapEntity heyTapEntity) throws IOException {
		Result<Void> result = heyTapLogic.sign(heyTapEntity);
		if (result.isSuccess())
			return "签到成功！";
		else return "签到失败：" + result.getMessage();
	}

	@RegexFilter("早睡打卡{{statusStr}}")
	public String earlyToBed(HeyTapEntity heyTapEntity, String statusStr){
		boolean status = statusStr.contains("开");
		heyTapEntity.setEarlyToBedClock(status);
		heyTapService.save(heyTapEntity);
		return "欢太商城自动早睡打卡" + (status ? "开启" : "关闭") + "成功！如晚上打卡失败将会私聊提醒！";
	}


}

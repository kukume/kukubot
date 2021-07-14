package me.kuku.simbot.controller;

import catcode.StringTemplate;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.QqLoginPojo;
import me.kuku.pojo.QqLoginQrcode;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.annotation.SkipListenGroup;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.simbot.entity.QqVideoEntity;
import me.kuku.simbot.logic.QqLoginLogic;
import me.kuku.simbot.repository.QqVideoRepository;
import me.kuku.simbot.service.QqLoginService;
import me.kuku.simbot.service.QqService;
import me.kuku.simbot.service.QqVideoService;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.QqQrCodeLoginUtils;
import okhttp3.Response;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@ListenGroup("qqLogin")
@OnGroup
@Service
public class QqLoginController {

	@Resource
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Resource
	private MessageContentBuilderFactory messageContentBuilderFactory;
	@Resource
	private StringTemplate stringTemplate;
	@Resource
	private QqLoginService qqLoginService;
	@Resource
	private QqService qqService;
	@Resource
	private QqLoginLogic qqLoginLogic;
	@Resource
	private QqVideoService qqVideoService;

	@SkipListenGroup
	@Filter("qq二维码")
	public void qqQrcode(GroupMsg groupMsg, MsgSender msgSender){
		try {
			long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
			QqLoginQrcode qrcode = QqQrCodeLoginUtils.getQrCode();
			byte[] bytes = qrcode.getBytes();
			MessageContent messageContent = messageContentBuilderFactory.getMessageContentBuilder()
					.at(qq).image(bytes).text("扫码登录").build();
			msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + "QQ8.4.8版本以上的不支持直接图片或者相册识别，\n" +
					"解决方法：用tim或QQhd扫码或使用旧版本QQ（https://wwx.lanzoux.com/igkqMhpj5gh）");
			msgSender.SENDER.sendGroupMsg(groupMsg, messageContent);
			QqEntity qqEntity = qqService.findByQq(qq);
			threadPoolTaskExecutor.execute(() -> {
				String msg;
				try {
					Result<QqLoginPojo> result;
					do {
						try {
							TimeUnit.SECONDS.sleep(3);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						result = QqQrCodeLoginUtils.checkQrCode(qrcode.getSig());
					}while (result.getCode() == 0);
					if (result.getCode() == 200){
						QqLoginEntity qqLoginEntity = qqLoginService.findByQqEntity(qqEntity);
						if (qqLoginEntity == null) qqLoginEntity = new QqLoginEntity(qqEntity);
						QqLoginPojo pojo = result.getData();
						qqLoginEntity.setSKey(pojo.getSKey());
						qqLoginEntity.setPsKey(pojo.getPsKey());
						qqLoginEntity.setSuperKey(pojo.getSuperKey());
						qqLoginEntity.setSuperToken(pojo.getSuperToken());
						qqLoginEntity.setPt4Token(pojo.getPt4Token());
						qqLoginService.save(qqLoginEntity);
						msg = "绑定qq信息成功！";
					}else msg = result.getMessage();
				}catch (IOException e){
					msg = "qq登录出现异常了，异常信息为：" + e.getMessage();
				}
				msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + msg);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Filter("超级签到")
	public String sign(@ContextValue("qqLoginEntity") QqLoginEntity qqLoginEntity, GroupMsg groupMsg, MsgSender msgSender){
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + "正在为您超级签到中！");
		try {
			StringBuilder sb = new StringBuilder();
			String str1;
			if (qqLoginLogic.vipSign(qqLoginEntity).contains("失败"))
				str1 = "签到失败";
			else str1 = "签到成功";
			String str2 = qqLoginLogic.yellowSign(qqLoginEntity);
			String str3 = qqLoginLogic.qqVideoSign1(qqLoginEntity);
			String str4 = qqLoginLogic.qqVideoSign2(qqLoginEntity);
			String str5 = qqLoginLogic.bigVipSign(qqLoginEntity);
			String str6;
			if (qqLoginLogic.qqMusicSign(qqLoginEntity).contains("失败"))
				str6 = "签到失败";
			else str6 = "签到成功";
			String str7;
			if (qqLoginLogic.qPetSign(qqLoginEntity).contains("失败"))
				str7 = "领取失败";
			else str7 = "领取成功";
			String str9;
			if (qqLoginLogic.blueSign(qqLoginEntity).contains("成功"))
				str9 = "签到成功";
			else str9 = "签到失败";
			String str11 = qqLoginLogic.weiYunSign(qqLoginEntity);
			String str12 = qqLoginLogic.growthLike(qqLoginEntity);
			sb.append("会员签到：").append(str1).append("\n")
					.append("黄钻签到：").append(str2).append("\n")
					.append("腾讯视频签到1：").append(str3).append("\n")
					.append("腾讯视频签到2：").append(str4).append("\n")
					.append("大会员签到；").append(str5).append("\n")
					.append("音乐签到：").append(str6).append("\n")
					.append("大乐斗签到：").append(str7).append("\n")
					.append("蓝钻签到：").append(str9).append("\n")
					.append("微云签到：").append(str11).append("\n")
					.append("排行榜点赞：").append(str12);
			return sb.toString();
//                return "超级签到成功！！";
		}catch (Exception e){
			return "超级签到失败！！异常信息为：" + e.getMessage();
		}
	}

	@RegexFilter("气泡{{text}}")
	public String bubble(@ContextValue("qqLoginEntity") QqLoginEntity qqLoginEntity,
	                   @FilterValue("text") String text) throws IOException {
		return qqLoginLogic.diyBubble(qqLoginEntity, text, null);
	}

	@Filter("成长")
	public String growth(@ContextValue("qqLoginEntity") QqLoginEntity qqLoginEntity) throws IOException {
		return qqLoginLogic.vipGrowthAdd(qqLoginEntity);
	}

	@RegexFilter("自定义机型 {{iMei}} {{name}}")
	public String changePhoneOnline(@ContextValue("qqLoginEntity") QqLoginEntity qqLoginEntity,
	                                @FilterValue("iMei") String iMei,
	                                @FilterValue("name") String name) throws IOException {
		return qqLoginLogic.changePhoneOnline(qqLoginEntity, iMei, name);
	}

	@SkipListenGroup
	@Filter("腾讯视频二维码")
	public void tencentVideoQr(GroupMsg groupMsg, MsgSender msgSender) throws IOException {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		QqLoginQrcode qrcode = QqQrCodeLoginUtils.getQrCode(716027609L, 383, 101483052L);
		MessageContent messageContent = messageContentBuilderFactory.getMessageContentBuilder()
				.at(qq).image(qrcode.getBytes()).text("请使用QQ扫码登录腾讯视频").build();
		msgSender.SENDER.sendGroupMsg(groupMsg, messageContent);
		QqEntity qqEntity = qqService.findByQq(qq);
		threadPoolTaskExecutor.execute(() -> {
			String at = stringTemplate.at(qq);
			String msg;
			try {
				while (true) {
					TimeUnit.SECONDS.sleep(3);
					Result<QqLoginPojo> result = QqQrCodeLoginUtils.checkQrCode(716027609L, 383, 101483052L,
							"https://graph.qq.com/oauth2.0/login_jump", qrcode.getSig());
					if (result.getCode() == 200) {
						Result<String> authorizeResult = QqQrCodeLoginUtils.authorize(result.getData(), 101483052L, "",
								"https://access.video.qq.com/user/auth_login?vappid=11059694&vsecret=fdf61a6be0aad57132bc5cdf78ac30145b6cd2c1470b0cfe&raw=1&type=qq&appid=101483052");
						if (authorizeResult.isFailure()) {
							msg = result.getMessage();
						}else{
							String url = authorizeResult.getData();
							Response response = OkHttpUtils.get(url);
							response.close();
							String cookie = OkHttpUtils.getCookie(response);
							String vuSession = OkHttpUtils.getCookie(cookie, "vqq_vusession");
							String accessToken = OkHttpUtils.getCookie(cookie, "vqq_access_token");
							QqVideoEntity qqVideoEntity = qqVideoService.findByQqEntity(qqEntity);
							if (qqVideoEntity == null) qqVideoEntity = new QqVideoEntity(qqEntity);
							qqVideoEntity.setCookie(cookie);
							qqVideoEntity.setVuSession(vuSession);
							qqVideoEntity.setAccessToken(accessToken);
							qqVideoService.save(qqVideoEntity);
							msg = "绑定腾讯视频成功！";
						}
						break;
					} else if (result.getCode() == 500) {
						msg = result.getMessage();
						break;
					}
				}
				msgSender.SENDER.sendGroupMsg(groupMsg, at + msg);
			}catch (Exception e){

			}
		});
	}

	@SkipListenGroup
	@Filter("腾讯视频签到")
	public String tencentVideoSign(@ContextValue("qq") QqEntity qqEntity) throws IOException {
		QqVideoEntity qqVideoEntity = qqVideoService.findByQqEntity(qqEntity);
		if (qqVideoEntity == null) return "您没有绑定腾讯视频信息，请先发送<腾讯视频二维码>进行绑定";
		return qqLoginLogic.videoSign(qqVideoEntity).getMessage();
	}

}

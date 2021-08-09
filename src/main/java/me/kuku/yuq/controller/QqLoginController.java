package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.QqLoginPojo;
import me.kuku.pojo.QqLoginQrcode;
import me.kuku.pojo.Result;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.QqQrCodeLoginUtils;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.QqLoginLogic;
import okhttp3.Response;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@GroupController
public class QqLoginController {
	@Inject
	private QqLoginService qqLoginService;
	@Inject
	private QqService qqService;
	@Inject
	private QqLoginLogic qqLoginLogic;
	@Inject
	private QqVideoService qqVideoService;
	@Inject
	private MessageItemFactory mif;
	@Inject
	private JobManager jobManager;

	@Before(except = {"qqQrcode", "tencentVideoQr", "tencentVideoSign"})
	public QqLoginEntity before(QqEntity qqEntity){
		QqLoginEntity qqLoginEntity = qqLoginService.findByQqEntity(qqEntity);
		if (qqLoginEntity == null)
			throw mif.at(qqEntity.getQq()).plus("你还没有绑定qq， 请发送<qq二维码>进行绑定").toThrowable();
		else return qqLoginEntity;
	}

	@Action("qq二维码")
	public void qqQrcode(long qq, Group group, QqEntity qqEntity){
		try {
			QqLoginQrcode qrcode = QqQrCodeLoginUtils.getQrCode();
			byte[] bytes = qrcode.getBytes();
			group.sendMessage(mif.at(qq).plus(mif.imageByByteArray(bytes).plus("扫码登录")));
			jobManager.registerTimer(() -> {
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
						if (qqLoginEntity == null) qqLoginEntity = QqLoginEntity.Companion.getInstance(qqEntity);
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
				group.sendMessage(mif.at(qq).plus(msg));
			}, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Action("超级签到")
	@QMsg(at = true)
	public String sign(QqLoginEntity qqLoginEntity, long qq, Group group){
		group.sendMessage(mif.at(qq).plus("正在为您超级签到中！"));
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

	@Action("气泡 {text}")
	@QMsg(at = true)
	public String bubble(QqLoginEntity qqLoginEntity, String text) throws IOException {
		return qqLoginLogic.diyBubble(qqLoginEntity, text, null);
	}

	@Action("成长")
	@QMsg(at = true)
	public String growth(QqLoginEntity qqLoginEntity) throws IOException {
		return qqLoginLogic.vipGrowthAdd(qqLoginEntity);
	}

	@Action("自定义机型 {iMei} {name}")
	@QMsg(at = true)
	public String changePhoneOnline(QqLoginEntity qqLoginEntity, String iMei, String name) throws IOException {
		return qqLoginLogic.changePhoneOnline(qqLoginEntity, iMei, name);
	}

	@Action("腾讯视频二维码")
	public void tencentVideoQr(long qq, Group group) throws IOException {
		QqLoginQrcode qrcode = QqQrCodeLoginUtils.getQrCode(716027609L, 383, 101483052L);
		group.sendMessage(mif.at(qq).plus(mif.imageByByteArray(qrcode.getBytes())).plus("请使用QQ扫码登录腾讯视频"));
		QqEntity qqEntity = qqService.findByQq(qq);
		jobManager.registerTimer(() -> {
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
							if (qqVideoEntity == null) qqVideoEntity = QqVideoEntity.Companion.getInstance(qqEntity);
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
				group.sendMessage(mif.at(qq).plus(msg));
			}catch (Exception e){

			}
		}, 0);
	}

	@Action("腾讯视频签到")
	@QMsg(at = true)
	public String tencentVideoSign(QqEntity qqEntity) throws IOException {
		QqVideoEntity qqVideoEntity = qqVideoService.findByQqEntity(qqEntity);
		if (qqVideoEntity == null) return "您没有绑定腾讯视频信息，请先发送<腾讯视频二维码>进行绑定";
		return qqLoginLogic.videoSign(qqVideoEntity).getMessage();
	}

}

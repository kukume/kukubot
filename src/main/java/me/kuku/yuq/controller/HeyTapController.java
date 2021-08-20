package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.QqLoginQrcode;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.HeyTapEntity;
import me.kuku.yuq.entity.HeyTapService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.entity.QqService;
import me.kuku.yuq.logic.HeyTapLogic;
import me.kuku.yuq.pojo.HeyTapQrcode;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@GroupController
@PrivateController
public class HeyTapController {
	
	@Inject
	private HeyTapService heyTapService;
	@Inject
	private HeyTapLogic heyTapLogic;
	@Inject
	private MessageItemFactory mif;
	@Inject
	private JobManager jobManager;
	@Inject
	private QqService qqService;
	
	@Action("欢太绑定 {cookie}")
	public String bind(String cookie, QqEntity qqEntity){
		HeyTapEntity hetTapEntity = heyTapService.findByQqEntity(qqEntity);
		if (hetTapEntity == null) hetTapEntity = HeyTapEntity.Companion.getInstance(qqEntity);
		hetTapEntity.setCookie(cookie);
		heyTapService.save(hetTapEntity);
		return "绑定欢太成功！";
	}

	@Action("欢太 {password}")
	public String bindByPassword(long qq, String password, QqEntity qqEntity) throws IOException {
		Result<HeyTapEntity> result = heyTapLogic.loginByQqPassword(qq, password);
		if (result.isFailure()) return result.getMessage();
		HeyTapEntity newEntity = result.getData();
		HeyTapEntity hetTapEntity = heyTapService.findByQqEntity(qqEntity);
		if (hetTapEntity == null) hetTapEntity = HeyTapEntity.Companion.getInstance(qqEntity);
		hetTapEntity.setCookie(newEntity.getCookie());
		heyTapService.save(hetTapEntity);
		qqEntity.setPassword(password);
		qqService.save(qqEntity);
		return "绑定欢太成功！";
	}

	@Action("欢太qq二维码")
	public void getQqQrcode(QqEntity qqEntity, long qq, Group group) throws IOException {
		QqLoginQrcode qrcode = heyTapLogic.getQqQrcode();
		group.sendMessage(mif.at(qq).plus(mif.imageByByteArray(qrcode.getBytes()))
				.plus("请使用QQ扫码登录！请确保您的欢太账号已绑定QQ！"));
		jobManager.registerTimer(() -> {
			String msg;
			try {
				while (true){
					try {
						TimeUnit.SECONDS.sleep(3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Result<HeyTapEntity> result = heyTapLogic.checkQqQrcode(qrcode);
					Integer code = result.getCode();
					if (code == 200){
						HeyTapEntity newHeyTapEntity = result.getData();
						HeyTapEntity heyTapEntity = heyTapService.findByQqEntity(qqEntity);
						if (heyTapEntity == null) heyTapEntity = HeyTapEntity.Companion.getInstance(qqEntity);
						heyTapEntity.setCookie(newHeyTapEntity.getCookie());
						heyTapService.save(heyTapEntity);
						msg = "绑定欢太信息成功！";
						break;
					}else if (code == 500){
						msg = result.getMessage();
						break;
					}
				}
			}catch (IOException e){
				msg = "欢太登录出现异常了，异常信息为：" + e.getMessage();
			}
			group.sendMessage(mif.at(qq).plus(msg));
		}, 0);
	}

	@Before(except = {"getQrcode", "bind", "getQqQrcode", "bindByPassword"})
	public HeyTapEntity before(QqEntity qqEntity){
		HeyTapEntity heyTapEntity = heyTapService.findByQqEntity(qqEntity);
		if (heyTapEntity == null)
			throw FunKt.getMif().at(qqEntity.getQq()).plus("您还没有绑定欢太账号，请发送<欢太二维码>进行绑定！").toThrowable();
		else return heyTapEntity;
	}

	@Action("欢太二维码")
	public void getQrcode(QqEntity qqEntity, long qq, Group group) throws IOException {
		HeyTapQrcode qrcode = heyTapLogic.getQrcode();
		String url = qrcode.getUrl();
		group.sendMessage(mif.at(qq).plus(mif.imageByUrl(url)).plus(mif.text("请使用oppo手机打开设置->我的->右上角扫码登录")));
		jobManager.registerTimer(() -> {
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
			group.sendMessage(mif.at(qq).plus(msg));
		}, 0);
	}

	@Action("欢太签到")
	@QMsg(at = true)
	public String sign(HeyTapEntity heyTapEntity, Group group, long qq) throws IOException {
		group.sendMessage(mif.at(qq).plus("正在为您签到中！"));
		Result<Void> result = heyTapLogic.sign(heyTapEntity);
		if (result.isSuccess()) {
			heyTapLogic.viewGoods(heyTapEntity);
			heyTapLogic.shareGoods(heyTapEntity);
			heyTapLogic.viewPush(heyTapEntity);
			heyTapLogic.transferPoints(heyTapEntity);
			return "欢太签到成功！";
		}
		else return "欢太签到失败：" + result.getMessage();
	}

	@Action("早睡打卡 {status}")
	@QMsg(at = true)
	public String earlyToBed(HeyTapEntity heyTapEntity, boolean status){
		heyTapEntity.setEarlyToBedClock(status);
		heyTapService.save(heyTapEntity);
		return "欢太商城自动早睡打卡" + (status ? "开启" : "关闭") + "成功！如晚上打卡失败将会私聊提醒！";
	}
	
}

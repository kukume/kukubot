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
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.HeyTapEntity;
import me.kuku.yuq.entity.HeyTapService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.logic.HeyTapLogic;
import me.kuku.yuq.pojo.HeyTapQrcode;

import javax.inject.Inject;
import java.io.IOException;

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
	
	@Action("欢太 {cookie}")
	public String bind(String cookie, QqEntity qqEntity){
		HeyTapEntity hetTapEntity = heyTapService.findByQqEntity(qqEntity);
		if (hetTapEntity == null) hetTapEntity = HeyTapEntity.Companion.getInstance(qqEntity);
		hetTapEntity.setCookie(cookie);
		heyTapService.save(hetTapEntity);
		return "绑定欢太成功！";
	}

	@Before(except = {"getQrcode", "bind"})
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
	public String sign(HeyTapEntity heyTapEntity) throws IOException {
		Result<Void> result = heyTapLogic.sign(heyTapEntity);
		if (result.isSuccess())
			return "签到成功！";
		else return "签到失败：" + result.getMessage();
	}

	@Action("早睡打卡 {status}")
	@QMsg(at = true)
	public String earlyToBed(HeyTapEntity heyTapEntity, boolean status){
		heyTapEntity.setEarlyToBedClock(status);
		heyTapService.save(heyTapEntity);
		return "欢太商城自动早睡打卡" + (status ? "开启" : "关闭") + "成功！如晚上打卡失败将会私聊提醒！";
	}
	
}

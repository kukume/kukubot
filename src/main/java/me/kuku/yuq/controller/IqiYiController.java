package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.IqiYiEntity;
import me.kuku.yuq.entity.IqiYiService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.logic.IqiYiLogic;
import me.kuku.yuq.pojo.IqiYiQrcode;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@GroupController
public class IqiYiController {

	@Inject
	private IqiYiLogic iqiYiLogic;
	@Inject
	private IqiYiService iqiYiService;
	@Inject
	private MessageItemFactory mif;
	@Inject
	private JobManager jobManager;

	@Action("爱奇艺二维码")
	public void qrcode(long qq, QqEntity qqEntity, Group group) throws IOException {
		IqiYiQrcode qrcode = iqiYiLogic.getQrcode();
		group.sendMessage(mif.at(qq).plus(mif.imageByUrl(qrcode.getUrl())).plus("请使用爱奇艺APP扫码登录！"));
		jobManager.registerTimer(() -> {
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
			group.sendMessage(mif.at(qq).plus(msg));
		}, 0);
	}

	@Action("爱奇艺签到")
	public String sign(QqEntity qqEntity) throws IOException {
		IqiYiEntity iqiYiEntity = iqiYiService.findByQqEntity(qqEntity);
		if (iqiYiEntity == null) return "您还没有绑定爱奇艺账号，请发送<爱奇艺二维码>进行绑定";
		Result<Void> result = iqiYiLogic.sign(iqiYiEntity);
		if (result.isFailure()) return result.getMessage();
		iqiYiLogic.task(iqiYiEntity);
		iqiYiLogic.draw(iqiYiEntity);
		return "爱奇艺签到成功！";
	}


}

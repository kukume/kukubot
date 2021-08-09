package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.NetEaseEntity;
import me.kuku.yuq.entity.NetEaseService;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.logic.NetEaseLogic;
import me.kuku.yuq.pojo.NetEaseQrcode;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@GroupController
public class NetEaseController {

	@Inject
	private NetEaseService netEaseService;
	@Inject
	private NetEaseLogic netEaseLogic;
	@Inject
	private MessageItemFactory mif;
	@Inject
	private JobManager jobManager;

	@Action("网易二维码")
	public void qrcode(Group group, QqEntity qqEntity) throws IOException {
		NetEaseQrcode netEaseQrcode = netEaseLogic.loginByQrcode();
		long qq = qqEntity.getQq();
		group.sendMessage(mif.at(qq).plus(mif.imageByByteArray(netEaseQrcode.getBytes())).plus("请使用网易云音乐APP扫码登录！"));
		jobManager.registerTimer(() -> {
			String msg;
			try {
				while (true) {
					TimeUnit.SECONDS.sleep(3);
					Result<NetEaseEntity> result = netEaseLogic.checkQrcode(netEaseQrcode);
					if (result.isSuccess()) {
						NetEaseEntity netEaseEntity = netEaseService.findByQqEntity(qqEntity);
						if (netEaseEntity == null) netEaseEntity = NetEaseEntity.Companion.getInstance(qqEntity);
						NetEaseEntity newEntity = result.getData();
						netEaseEntity.setCsrf(newEntity.getCsrf());
						netEaseEntity.setMusicU(newEntity.getMusicU());
						netEaseService.save(netEaseEntity);
						msg = "绑定网易云音乐成功！";
						break;
					} else if (result.getCode() == 500) {
						msg = result.getMessage();
						break;
					}
				}
			} catch (Exception e) {
				msg = "出现异常了，异常信息为" + e.getMessage();
			}
			group.sendMessage(mif.at(qq).plus(msg));
		}, 0);
	}

	@Before(except = "qrcode")
	public NetEaseEntity before(QqEntity qqEntity){
		NetEaseEntity netEaseEntity = netEaseService.findByQqEntity(qqEntity);
		if (netEaseEntity == null)
			throw mif.at(qqEntity.getQq()).plus("您还没有绑定网易账号，请发送<网易二维码>进行绑定！").toThrowable();
		else return netEaseEntity;
	}

	@Action("网易签到")
	@QMsg(at = true)
	public String sign(NetEaseEntity netEaseEntity) throws IOException {
		Result<?> result = netEaseLogic.sign(netEaseEntity);
		if (result.isFailure()) return "网易云音乐签到失败！失败原因：" + result.getMessage();
		netEaseLogic.listeningVolume(netEaseEntity);
		return "网易云音乐签到成功！";
	}

	@Action("网易音乐人签到")
	@QMsg(at = true)
	public String musicianSign(NetEaseEntity netEaseEntity) throws IOException {
		Result<Void> result = netEaseLogic.musicianSign(netEaseEntity);
		return result.getMessage();
	}

}

package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.entity.WeiboEntity;
import me.kuku.yuq.entity.WeiboService;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.WeiboPojo;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@GroupController
public class WeiboController {

	@Inject
	private WeiboLogic weiboLogic;
	@Inject
	private WeiboService weiboService;
	@Inject
	private MessageItemFactory mif;
	@Inject
	private JobManager jobManager;

	@Action("热搜")
	@QMsg(at = true, atNewLine = true)
	public String hotSearch() throws IOException {
		List<String> list = weiboLogic.hotSearch();
		StringBuilder sb = new StringBuilder();
		for (String str: list){
			sb.append(str).append("\n");
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	@Action("微博用户 {username}")
	@QMsg(at = true, atNewLine = true)
	public String wbInfo(String username) throws IOException {
		Result<List<WeiboPojo>> idResult = weiboLogic.getIdByName(username);
		List<WeiboPojo> idList = idResult.getData();
		if (idList == null) return idResult.getMessage();
		return weiboLogic.getUserInfo(idList.get(0).getUserId());
	}

	@Action("微博二维码")
	public void qrcode(long qq, QqEntity qqEntity, Group group) throws IOException {
		Map<String, String> map = weiboLogic.loginByQr1();
		group.sendMessage(mif.at(qq).plus("请使用微博APP扫码登录").plus(mif.imageByUrl("https:" + map.get("url"))));
		jobManager.registerTimer(() -> {
			String id = map.get("id");
			while (true){
				try {
					TimeUnit.SECONDS.sleep(3);
					Result<WeiboEntity> result = weiboLogic.loginByQr2(id);
					if (result.getCode() == 200){
						WeiboEntity newWeiboEntity = result.getData();
						WeiboEntity weiboEntity = weiboService.findByQqEntity(qqEntity);
						if (weiboEntity == null) weiboEntity = WeiboEntity.Companion.getInstance(qqEntity);
						weiboEntity.setPcCookie(newWeiboEntity.getPcCookie());
						weiboEntity.setMobileCookie(newWeiboEntity.getMobileCookie());
						weiboService.save(weiboEntity);
						group.sendMessage(mif.at(qq).plus("绑定微博成功！"));
						break;
					}else if (result.getCode() == 500){
						group.sendMessage(mif.at(qq).plus(result.getMessage()));
						break;
					}
				}catch (Exception e){
					e.printStackTrace();
					break;
				}
			}
		}, 0);
	}

	@Before(only = "weiboMonitor")
	@QMsg(at = true)
	public WeiboEntity before(QqEntity qqEntity){
		WeiboEntity weiboEntity = weiboService.findByQqEntity(qqEntity);
		if (weiboEntity == null)
			throw mif.at(qqEntity.getQq()).plus("您还没有绑定微博，请发送<微博二维码>绑定账号").toThrowable();
		else return weiboEntity;
	}

	@Action("微博监控 {status}")
	@QMsg(at = true)
	public String weiboMonitor(boolean status, WeiboEntity weiboEntity){
		weiboEntity.setMonitor(status);
		weiboService.save(weiboEntity);
		return status ? "微博监控开启成功！" : "微博监控关闭成功！";
	}

}

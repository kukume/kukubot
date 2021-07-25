package me.kuku.simbot.controller;

import catcode.StringTemplate;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.annotation.SkipListenGroup;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.WeiboEntity;
import me.kuku.simbot.entity.WeiboService;
import me.kuku.simbot.logic.WeiboLogic;
import me.kuku.simbot.pojo.WeiboPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@ListenGroup("weibo")
@OnGroup
public class WeiboController {

	@Autowired
	private WeiboLogic weiboLogic;
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Autowired
	private StringTemplate stringTemplate;
	@Autowired
	private WeiboService weiboService;

	@SkipListenGroup
	@Filter("热搜")
	public String hotSearch() throws IOException {
		List<String> list = weiboLogic.hotSearch();
		StringBuilder sb = new StringBuilder();
		for (String str: list){
			sb.append(str).append("\n");
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	@SkipListenGroup
	@RegexFilter("微博用户{{username}}")
	public String wbInfo(@FilterValue("username") String username) throws IOException {
		Result<List<WeiboPojo>> idResult = weiboLogic.getIdByName(username);
		List<WeiboPojo> idList = idResult.getData();
		if (idList == null) return idResult.getMessage();
		return weiboLogic.getUserInfo(idList.get(0).getUserId());
	}

	@SkipListenGroup
	@Filter("微博二维码")
	public void qrcode(GroupMsg groupMsg, MsgSender msgSender, @ContextValue("qq") QqEntity qqEntity) throws IOException {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		Map<String, String> map = weiboLogic.loginByQr1();
		msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + "请使用微博APP扫码登录" +
				stringTemplate.image("https:" + map.get("url")));
		threadPoolTaskExecutor.execute(() -> {
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
						msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + "绑定微博成功！");
						break;
					}else if (result.getCode() == 500){
						msgSender.SENDER.sendGroupMsg(groupMsg, result.getMessage());
						break;
					}
				}catch (Exception e){
					e.printStackTrace();
					break;
				}
			}
		});
	}

	@RegexFilter("微博监控{{statusStr}}")
	public String weiboMonitor(@FilterValue("statusStr") String statusStr, @ContextValue("weiboEntity") WeiboEntity weiboEntity){
		boolean status = statusStr.contains("开");
		weiboEntity.setMonitor(status);
		weiboService.save(weiboEntity);
		return status ? "微博监控开启成功！" : "微博监控关闭成功！";
	}

}

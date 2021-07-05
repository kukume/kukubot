package me.kuku.simbot.controller;

import catcode.CatCodeUtil;
import catcode.CodeTemplate;
import catcode.StringTemplate;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.BiliBiliEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.BiliBiliLogic;
import me.kuku.simbot.logic.ToolLogic;
import me.kuku.simbot.service.BiliBiliService;
import me.kuku.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@OnGroup
@ListenGroup("biliBili")
@Service
public class BiliBiliController {

	@Autowired
	private BiliBiliLogic biliBiliLogic;
	@Autowired
	private ToolLogic toolLogic;
	@Autowired
	private MessageContentBuilderFactory messageContentBuilderFactory;
	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	@Autowired
	private BiliBiliService biliBiliService;
	@Autowired
	private StringTemplate stringTemplate;

	@Filter("哔哩哔哩二维码")
	@ListenGroup(value = "", append = false)
	public void biliBiliLoginQr(MsgSender msgSender, GroupMsg groupMsg,
	                            @ContextValue("qq") QqEntity qqEntity) throws IOException {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		CodeTemplate<String> template = CatCodeUtil.getInstance().getStringTemplate();
		InputStream is = null;
		String url;
		try {
			url = biliBiliLogic.loginByQr1();
			is = toolLogic.creatQr(url);
			MessageContent messageContent = messageContentBuilderFactory.getMessageContentBuilder()
					.at(qq).text("请使用哔哩哔哩APP扫码登录：").image(is).build();
			msgSender.SENDER.sendGroupMsg(groupMsg, messageContent);
		} catch (IOException e) {
			String s = template.at(qq) + "二维码获取失败，请重试！";
			msgSender.SENDER.sendGroupMsg(groupMsg, s);
			return;
		} finally {
			IOUtils.close(is);
		}
		AtomicInteger i = new AtomicInteger();
		threadPoolTaskExecutor.execute(() -> {
			while (true){
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (i.incrementAndGet() >= 20){
					msgSender.SENDER.sendGroupMsg(groupMsg, template.at(qq) + "您的二维码已失效！");
					break;
				}
				try {
					Result<BiliBiliEntity> res = biliBiliLogic.loginByQr2(url);
					switch (res.getCode()){
						case 500:
							msgSender.SENDER.sendGroupMsg(groupMsg, template.at(qq) + res.getMessage());
							return;
						case 200:
							BiliBiliEntity biliBiliEntity = biliBiliService.findByQqEntity(qqEntity);
							if (biliBiliEntity == null) biliBiliEntity = new BiliBiliEntity(qqEntity);
							BiliBiliEntity newBiliBiliEntity = res.getData();
							biliBiliEntity.setCookie(newBiliBiliEntity.getCookie());
							biliBiliEntity.setToken(newBiliBiliEntity.getToken());
							biliBiliEntity.setUserid(newBiliBiliEntity.getUserid());
							biliBiliService.save(biliBiliEntity);
							msgSender.SENDER.sendGroupMsg(groupMsg, template.at(qq) + "绑定哔哩哔哩成功！");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@RegexFilter("哔哩哔哩{{type,监控|任务}}{{statusStr}}")
	public String status(@ContextValue("biliBiliEntity") BiliBiliEntity biliBiliEntity,
	                   @FilterValue("statusStr") String statusStr, @FilterValue("type") String type){
		boolean status = statusStr.contains("开");
		switch (type){
			case "监控": biliBiliEntity.setMonitor(status); break;
			case "任务": biliBiliEntity.setTask(status); break;
		}
		biliBiliService.save(biliBiliEntity);
		return "哔哩哔哩" + type + (status ? "开启成功" : "关闭成功");
	}

	@RegexFilter("哔哩哔哩举报{{bvId}}")
	public String report(@ContextValue("biliBiliEntity") BiliBiliEntity biliBiliEntity,
	                 @FilterValue("bvId") String bvId, MsgSender msgSender, GroupMsg groupMsg) throws IOException {
		long qq = groupMsg.getAccountInfo().getAccountCodeNumber();
		msgSender.SENDER.sendGroupMsg(groupMsg, stringTemplate.at(qq) + "正在为您举报中！");
		String oid = biliBiliLogic.getOidByBvId(bvId);
		List<Map<String, String>> list = biliBiliLogic.getReplay(biliBiliEntity, oid, 1);
		String msg = null;
		for (Map<String, String> map : list) {
			try {
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			msg = biliBiliLogic.reportComment(biliBiliEntity, oid, map.get("id"), 8);
		}
		return msg;
	}

}

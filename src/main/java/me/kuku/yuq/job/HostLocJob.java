package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.HostLocLogic;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JobCenter
public class HostLocJob {

	@Inject
	private HostLocService hostLocService;
	@Inject
	private HostLocLogic hostLocLogic;
	@Inject
	private GroupService groupService;
	@Inject
	private QqService qqService;

	private int locId = 0;

	@Cron("At::d::07:20:13")
	public void sign(){
		List<HostLocEntity> list = hostLocService.findBySign(true);
		for (HostLocEntity hostLocEntity : list) {
			try {
				String cookie = hostLocEntity.getCookie();
				boolean isLogin = hostLocLogic.isLogin(cookie);
				if (isLogin) hostLocLogic.sign(cookie);
				else {
					QqEntity qqEntity = hostLocEntity.getQqEntity();
					BotUtils.sendMessage(qqEntity,
							"loc签到失败，cookie已失效，请重新绑定，并在群中发送<loc签到>进行手动签到！");
					hostLocService.delete(hostLocEntity);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Cron("1m")
	public void locMonitor() {
		List<Map<String, String>> list = hostLocLogic.post();
		if (list.size() == 0) return;
		List<Map<String, String>> newList = new ArrayList<>();
		if (locId != 0){
			for (Map<String, String> map : list) {
				if (Integer.parseInt(map.get("id")) <= locId) break;
				newList.add(map);
			}
		}
		locId = Integer.parseInt(list.get(0).get("id"));
		List<HostLocEntity> qqList = hostLocService.findByMonitor(true);
		for (Map<String, String> map : newList) {
			String str = "Loc有新帖了！！" + "\n" +
					"标题：" + map.get("title") + "\n" +
					"昵称：" + map.get("name") + "\n" +
					"链接：" + map.get("url");
			for (HostLocEntity hostLocEntity : qqList) {
				QqEntity qqEntity = hostLocEntity.getQqEntity();
				BotUtils.sendMessage(qqEntity, str);
			}
		}
	}

}

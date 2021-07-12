package me.kuku.simbot.scheduled;

import me.kuku.simbot.entity.HostLocEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.HostLocLogic;
import me.kuku.simbot.service.HostLocService;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HostLocScheduled {

	@Resource
	private HostLocService hostLocService;
	@Resource
	private HostLocLogic hostLocLogic;

	private int locId = 0;

	@Scheduled(cron = "13 20 7 * * ?")
	@Transactional
	public void locSign(){
		List<HostLocEntity> list = hostLocService.findBySign(true);
		for (HostLocEntity hostLocEntity : list) {
			try {
				String cookie = hostLocEntity.getCookie();
				boolean isLogin = hostLocLogic.isLogin(cookie);
				if (isLogin) hostLocLogic.sign(cookie);
				else {
					QqEntity qqEntity = hostLocEntity.getQqEntity();
					BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(),
							"loc签到失败，cookie已失效，请重新绑定，并在群中发送<loc签到>进行手动签到！");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Scheduled(cron = "32 */1 * * * ?")
	@Transactional
	public void locMonitor(){
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
				BotUtils.sendPrivateMsg(qqEntity.getGroups(), qqEntity.getQq(), str);
			}
		}
	}


}

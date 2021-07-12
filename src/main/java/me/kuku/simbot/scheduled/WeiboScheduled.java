package me.kuku.simbot.scheduled;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.WeiboEntity;
import me.kuku.simbot.logic.WeiboLogic;
import me.kuku.simbot.pojo.WeiboPojo;
import me.kuku.simbot.service.WeiboService;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeiboScheduled {

	@Resource
	private WeiboService weiboService;
	@Resource
	private WeiboLogic weiboLogic;

	private final Map<Long, Long> userMap = new HashMap<>();

	@Scheduled(cron = "13 */2 * * * ?")
	@Transactional
	public void qqMonitor(){
		List<WeiboEntity> weiboList = weiboService.findByMonitor(true);
		for (WeiboEntity weiboEntity : weiboList) {
			try {
				QqEntity qqEntity = weiboEntity.getQqEntity();
				Long qq = qqEntity.getQq();
				Result<List<WeiboPojo>> result = weiboLogic.getFriendWeibo(weiboEntity);
				List<WeiboPojo> list = result.getData();
				if (list == null || list.size() == 0) continue;
				List<WeiboPojo> newList = new ArrayList<>();
				if (userMap.containsKey(qq)){
					for (WeiboPojo weiboPojo : list) {
						if (weiboPojo.getId() <= userMap.get(qq)) break;
						newList.add(weiboPojo);
					}
					for (WeiboPojo weiboPojo: newList){
						BotUtils.sendPrivateMsg(qqEntity.getGroups(), qq,
								"有新微博了！！\n" + weiboLogic.convertStr(weiboPojo));
					}
				}
				userMap.put(qq, list.get(0).getId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

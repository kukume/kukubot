package me.kuku.simbot.scheduled;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.WeiboEntity;
import me.kuku.simbot.logic.WeiboLogic;
import me.kuku.simbot.pojo.WeiboPojo;
import me.kuku.simbot.service.GroupService;
import me.kuku.simbot.service.WeiboService;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
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
	@Resource
	private GroupService groupService;

	private final Map<Long, Long> userMap = new HashMap<>();
	private final Map<Long, Map<Long, Long>> groupMap = new HashMap<>();

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

	@Scheduled(cron = "24 */2 * * * ?")
	public void groupWeibo(){
		for (GroupEntity groupEntity: groupService.findAll()){
			try {
				Long group = groupEntity.getGroup();
				JSONArray weiboJsonArray = groupEntity.getWeiboJson();
				if (weiboJsonArray.size() == 0) continue;
				if (!groupMap.containsKey(group)) {
					groupMap.put(group, new HashMap<>());
				}
				Map<Long, Long> wbMap = groupMap.get(group);
				for (Object obj : weiboJsonArray) {
					JSONObject jsonObject = (JSONObject) obj;
					Long userId = jsonObject.getLong("id");
					Result<List<WeiboPojo>> result;
					try {
						result = weiboLogic.getWeiboById(userId.toString());
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					List<WeiboPojo> list = result.getData();
					if (list == null || list.size() == 0) continue;
					if (wbMap.containsKey(userId)) {
						List<WeiboPojo> newList = new ArrayList<>();
						for (WeiboPojo weiboPojo : list) {
							if (weiboPojo.getId() <= wbMap.get(userId)) break;
							newList.add(weiboPojo);
						}
						for (WeiboPojo weiboPojo : newList) {
							BotUtils.sendGroupMsg(group, "有新微博了\n" + weiboLogic.convertStr(weiboPojo));
						}

					}
					Long newId = list.get(0).getId();
					if (!wbMap.containsKey(userId) || newId > wbMap.get(userId)) {
						wbMap.put(userId, newId);
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

}

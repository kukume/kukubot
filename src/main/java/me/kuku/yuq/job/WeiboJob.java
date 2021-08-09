package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.WeiboLogic;
import me.kuku.yuq.pojo.WeiboPojo;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JobCenter
public class WeiboJob {
	@Inject
	private WeiboLogic weiboLogic;
	@Inject
	private GroupService groupService;
	@Inject
	private WeiboService weiboService;

	private final Map<Long, Map<Long, Long>> groupMap = new HashMap<>();
	private final Map<Long, Long> userMap = new HashMap<>();

	@Cron("2m")
	public void groupWeibo(){
		for (GroupEntity groupEntity: groupService.findAll()){
			Long group = groupEntity.getGroup();
			JSONArray weiboJsonArray = groupEntity.getWeiboJson();
			if (weiboJsonArray.size() == 0) continue;
			if (!groupMap.containsKey(group)){
				groupMap.put(group, new HashMap<>());
			}
			Map<Long, Long> wbMap = groupMap.get(group);
			for (Object obj: weiboJsonArray){
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
				if (wbMap.containsKey(userId)){
					List<WeiboPojo> newList = new ArrayList<>();
					for (WeiboPojo weiboPojo: list){
						if (weiboPojo.getId() <= wbMap.get(userId)) break;
						newList.add(weiboPojo);
					}
					newList.forEach( weiboPojo -> {
						try {
							FunKt.getYuq().getGroups().get(group)
									.sendMessage(FunKt.getMif().text("有新微博了\n")
											.plus(weiboLogic.convertStr(weiboPojo)));
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
				}
				Long newId = list.get(0).getId();
				if (!wbMap.containsKey(userId) || newId > wbMap.get(userId)){
					wbMap.put(userId, newId);
				}
			}
		}
	}

	@Cron("2m")
	public void qqWeibo() throws IOException {
		List<WeiboEntity> weiboList = weiboService.findByMonitor(true);
		for (WeiboEntity weiboEntity : weiboList) {
			Long qq = weiboEntity.getQqEntity().getQq();
			Result<List<WeiboPojo>> result = null;
			try {
				result = weiboLogic.getFriendWeibo(weiboEntity);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			List<WeiboPojo> list = result.getData();
			if (list == null || list.size() == 0) continue;
			List<WeiboPojo> newList = new ArrayList<>();
			if (userMap.containsKey(qq)) {
				for (WeiboPojo weiboPojo : list) {
					if (weiboPojo.getId() <= userMap.get(qq)) break;
					newList.add(weiboPojo);
				}
				for (WeiboPojo weiboPojo : newList) {
					QqEntity qqEntity = weiboEntity.getQqEntity();
					Message msg = Message.Companion.toMessage("有新微博了！！\n" + weiboLogic.convertStr(weiboPojo));
					try {
						BotUtils.sendMessage(qqEntity, msg);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
			userMap.put(qq, list.get(0).getId());
		}
	}
}

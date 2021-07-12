package me.kuku.simbot.scheduled;

import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.simbot.entity.BiliBiliEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.BiliBiliLogic;
import me.kuku.simbot.pojo.BiliBiliPojo;
import me.kuku.simbot.service.BiliBiliService;
import me.kuku.simbot.utils.BotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class BiliBiliScheduled {

	@Autowired
	private BiliBiliService biliBiliService;
	@Autowired
	private BiliBiliLogic biliBiliLogic;

	private final Map<Long, Map<Long, Boolean>> liveMap = new HashMap<>();
	private final Map<Long, Long> userMap = new HashMap<>();

	@Scheduled(cron = "0 */1 * * * ?")
	@Transactional
	public void liveMonitor(){
		List<BiliBiliEntity> list = biliBiliService.findByLive(true);
		for (BiliBiliEntity biliBiliEntity : list) {
			try{
				Result<List<Map<String, String>>> result = biliBiliLogic.followed(biliBiliEntity);
				if (result.isFailure()) continue;
				QqEntity qqEntity = biliBiliEntity.getQqEntity();
				Long qq = qqEntity.getQq();
				if (!liveMap.containsKey(qq)) liveMap.put(qq, new HashMap<>());
				Map<Long, Boolean> map = liveMap.get(qq);
				for (Map<String, String> upMap : result.getData()) {
					long id = Long.parseLong(upMap.get("id"));
					String name = upMap.get("name");
					JSONObject liveJsonObject = biliBiliLogic.live(String.valueOf(id));
					Boolean b = liveJsonObject.getBoolean("status");
					if (map.containsKey(id)){
						if (map.get(id) != b){
							map.put(id, b);
							String msg;
							if (b) msg = "直播啦！！";
							else msg = "下播了！！";
							BotUtils.sendPrivateMsg(qqEntity.getGroups(), qq,
									"哔哩哔哩开播提醒：\n" +
											name + msg + "\n" +
											"标题：" + liveJsonObject.getString("title") + "\n" +
											"链接：" + liveJsonObject.getString("url"));
						}
					}else map.put(id, b);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Scheduled(cron = "0 */1 * * * ?")
	@Transactional
	public void qqMonitor(){
		List<BiliBiliEntity> biliBiliEntityList = biliBiliService.findByMonitor(true);
		for (BiliBiliEntity biliBiliEntity : biliBiliEntityList) {
			try {
				Long qq = biliBiliEntity.getQqEntity().getQq();
				Result<List<BiliBiliPojo>> result = biliBiliLogic.getFriendDynamic(biliBiliEntity);
				List<BiliBiliPojo> list = result.getData();
				if (list == null) continue;
				List<BiliBiliPojo> newList = new ArrayList<>();
				if (userMap.containsKey(qq)){
					Long oldId = userMap.get(qq);
					for (BiliBiliPojo biliBiliPojo: list){
						if (Long.parseLong(biliBiliPojo.getId()) <= oldId) break;
						newList.add(biliBiliPojo);
					}
					for (BiliBiliPojo biliBiliPojo: newList){
						BotUtils.sendPrivateMsg(biliBiliEntity.getQqEntity().getGroups(), qq,
								"哔哩哔哩有新动态了！！\n" + biliBiliLogic.convertStr(biliBiliPojo));
					}
				}
				userMap.put(qq, Long.valueOf(list.get(0).getId()));
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Scheduled(cron = "12 15 8 * * ?")
	@Transactional
	public void sign(){
		List<BiliBiliEntity> biliBiliEntityList = biliBiliService.findByTask(true);
		for (BiliBiliEntity biliBiliEntity : biliBiliEntityList) {
			try {
				List<Map<String, String>> ranking = biliBiliLogic.getRanking();
				Map<String, String> firstRank = ranking.get(0);
				biliBiliLogic.report(biliBiliEntity, firstRank.get("aid"), firstRank.get("cid"), 300);
				biliBiliLogic.share(biliBiliEntity, firstRank.get("aid"));
				biliBiliLogic.liveSign(biliBiliEntity);
				int[] arr = {2, 2, 1};
				for (int i = 0; i < 3; i++){
					Map<String, String> randomMap = ranking.get((int) (Math.random() * ranking.size()));
					biliBiliLogic.tossCoin(biliBiliEntity, randomMap.get("aid"), arr[i]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


}

package me.kuku.simbot.scheduled;

import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.api.message.results.GroupList;
import love.forte.simbot.api.message.results.SimpleGroupInfo;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.BotManager;
import me.kuku.pojo.Result;
import me.kuku.simbot.entity.BiliBiliEntity;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.logic.BiliBiliLogic;
import me.kuku.simbot.service.BiliBiliService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class BiliBiliScheduled {

	@Autowired
	private BiliBiliService biliBiliService;
	@Autowired
	private BotManager botManager;
	@Autowired
	private BiliBiliLogic biliBiliLogic;

	private final Map<Long, Map<Long, Boolean>> liveMap = new HashMap<>();

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
				if (liveMap.containsKey(qq)) liveMap.put(qq, new HashMap<>());
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
							Set<GroupEntity> groupSet = qqEntity.getGroups();
							List<Bot> botList = botManager.getBots();
							out:for (Bot bot : botList) {
								GroupList groupList = bot.getSender().GETTER.getGroupList();
								for (SimpleGroupInfo simpleGroupInfo : groupList) {
									long group = simpleGroupInfo.getGroupCodeNumber();
									for (GroupEntity groupEntity : groupSet) {
										if (groupEntity.getGroup().equals(group)){
											bot.getSender().SENDER.sendPrivateMsg(qq,
													"哔哩哔哩开播提醒：\n" +
															name + msg + "\n" +
															"标题：" + liveJsonObject.getString("title") + "\n" +
															"链接：" + liveJsonObject.getString("url"));
											break out;
										}
									}
								}
							}
						}
					}else map.put(id, b);
				}
			}catch (Exception e){
				e.printStackTrace();
			}

		}
	}


}

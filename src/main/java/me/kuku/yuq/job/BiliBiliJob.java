package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.*;
import me.kuku.yuq.logic.BiliBiliLogic;
import me.kuku.yuq.pojo.BiliBiliPojo;
import me.kuku.yuq.utils.BotUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JobCenter
public class BiliBiliJob {
	@Inject
	private GroupService groupService;
	@Inject
	private BiliBiliLogic biliBiliLogic;
	@Inject
	private BiliBiliService biliBiliService;
	@Inject
	private MessageItemFactory mif;

	private final Map<Long, Map<Long, Long>> groupMap = new HashMap<>();
	private final Map<Long, Long> userMap = new HashMap<>();
	private final Map<Long, Map<Long, Boolean>> liveMap = new HashMap<>();

	private Message pic(BiliBiliPojo biliBiliPojo){
		List<String> picList = biliBiliPojo.getPicList();
		List<String> forwardPicList = biliBiliPojo.getForwardPicList();
		if (picList.isEmpty() && forwardPicList.isEmpty()) return null;
		picList.addAll(forwardPicList);
		Message message = mif.text("附图：").toMessage();
		for (String s : picList) {
			message = message.plus(mif.imageByUrl(s));
		}
		return message;
	}

	@Cron("2m")
	public void biliBiliGroupMonitor() {
		List<GroupEntity> groupList = groupService.findAll();
		for (GroupEntity groupEntity: groupList){
			JSONArray biliBiliJsonArray = groupEntity.getBiliBiliJson();
			Long group = groupEntity.getGroup();
			if (biliBiliJsonArray.size() == 0) continue;
			if (!groupMap.containsKey(group)){
				groupMap.put(group, new HashMap<>());
			}
			Map<Long, Long> biMap = groupMap.get(group);
			for (Object obj: biliBiliJsonArray){
				JSONObject jsonObject = (JSONObject) obj;
				Long userId = jsonObject.getLong("id");
				Result<List<BiliBiliPojo>> result;
				try {
					result = biliBiliLogic.getDynamicById(userId.toString());
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				List<BiliBiliPojo> list = result.getData();
				if (list == null) continue;
				if (biMap.containsKey(userId)){
					List<BiliBiliPojo> newList = new ArrayList<>();
					for (BiliBiliPojo biliBiliPojo: list){
						if (Long.parseLong(biliBiliPojo.getId()) <= biMap.get(userId)) break;
						newList.add(biliBiliPojo);
					}
					Group groupObj = FunKt.getYuq().getGroups().get(group);
					newList.forEach(biliBiliPojo -> {
						try {
							Message message = pic(biliBiliPojo);
							if (message != null) groupObj.sendMessage(message);
							groupObj.sendMessage(
									FunKt.getMif().text("哔哩哔哩有新动态了\n")
											.plus(biliBiliLogic.convertStr(biliBiliPojo))
							);
						}catch (Exception e){
							e.printStackTrace();
						}
					});
				}
				long newId = Long.parseLong(list.get(0).getId());
				if (!biMap.containsKey(userId) || newId > biMap.get(userId)){
					biMap.put(userId, newId);
				}
			}
		}
	}

	@Cron("2m")
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
							BotUtils.sendMessage(qqEntity,
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

	@Cron("2m")
	public void biliBiliQQMonitor() throws IOException {
		List<BiliBiliEntity> biliBiliList = biliBiliService.findByMonitor(true);
		for (BiliBiliEntity biliBiliEntity: biliBiliList){
			Long qq = biliBiliEntity.getQqEntity().getQq();
			Result<List<BiliBiliPojo>> result;
			try {
				result = biliBiliLogic.getFriendDynamic(biliBiliEntity);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
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
					try {
						QqEntity qqEntity = biliBiliEntity.getQqEntity();
						Message message = pic(biliBiliPojo);
						if (message != null) BotUtils.sendMessage(qqEntity, message);
						BotUtils.sendMessage(qqEntity,
								FunKt.getMif().text("哔哩哔哩有新动态了！！\n").plus(biliBiliLogic.convertStr(biliBiliPojo)));
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
			userMap.put(qq, Long.valueOf(list.get(0).getId()));
		}
	}

	@Cron("1m")
	public void groupLiveMonitor(){
		List<GroupEntity> groupList = groupService.findAll();
		for (GroupEntity groupEntity : groupList) {
			try {
				long group = groupEntity.getGroup();
				JSONArray liveJsonArray = groupEntity.getBiliBiliLiveJson();
				if (!liveMap.containsKey(group)) liveMap.put(group, new HashMap<>());
				Map<Long, Boolean> map = liveMap.get(group);
				for (Object o : liveJsonArray) {
					JSONObject jsonObject = (JSONObject) o;
					Long id = jsonObject.getLong("id");
					JSONObject liveJsonObject = biliBiliLogic.live(id.toString());
					Boolean b = liveJsonObject.getBoolean("status");
					if (map.containsKey(id)){
						if (map.get(id) != b){
							map.put(id, b);
							String msg;
							if (b) msg = "直播啦！！";
							else msg = "下播了！！";
							BotUtils.sendMessage(group, "哔哩哔哩开播提醒：\n" +
									jsonObject.getString("name") + msg + "\n" +
									"标题：" + liveJsonObject.getString("title") + "\n" +
									"链接：" + liveJsonObject.getString("url")
							);
						}
					}else map.put(id, b);
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Cron("At::d::08:00:00")
	public void biliBilliTask() throws IOException {
		List<BiliBiliEntity> list = biliBiliService.findByTask(true);
		for (BiliBiliEntity biliBiliEntity: list){
			List<Map<String, String>> ranking = biliBiliLogic.getRanking();
			Map<String, String> firstRank = ranking.get(0);
			biliBiliLogic.report(biliBiliEntity, firstRank.get("aid"), firstRank.get("cid"), 300);
			biliBiliLogic.share(biliBiliEntity, firstRank.get("aid"));
			biliBiliLogic.liveSign(biliBiliEntity);
//			int[] arr = {2, 2, 1};
//			for (int i = 0; i < 3; i++){
//				Map<String, String> randomMap = ranking.get((int) (Math.random() * ranking.size()));
//				biliBiliLogic.tossCoin(biliBiliEntity, randomMap.get("aid"), arr[i]);
//			}
		}
	}
}

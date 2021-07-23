package me.kuku.simbot.event;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import me.kuku.simbot.entity.GroupEntity;
import me.kuku.simbot.service.GroupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GroupManagerEvent {

	@Resource
	private GroupService groupService;

	@OnGroup
	public void qa(GroupMsg groupMsg, long group, MsgSender msgSender){
		GroupEntity groupEntity = groupService.findByGroup(group);
		JSONArray qaJsonArray = groupEntity.getQaJson();
		String str = groupMsg.getMsg();
		for (int i = 0; i < qaJsonArray.size(); i++){
			JSONObject jsonObject = qaJsonArray.getJSONObject(i);
			String type = jsonObject.getString("type");
			String q = jsonObject.getString("q");
			boolean status = false;
			if ("ALL".equals(type)){
				if (str.equals(q)) status = true;
			}else if (str.contains(jsonObject.getString("q"))) status = true;
			if (status){
//				Integer maxCount = groupEntity.getMaxCommandCountOnTime();
//				if (maxCount == null) maxCount = -1;
//				if (maxCount > 0){
//					String key = "qq" + e.getSender().getId() + q;
//					Integer num = eh.get(key);
//					if (num == null) num = 0;
//					if (num >= maxCount) return;
//					eh.set(key, ++num);
//				}
				String sendMsg = jsonObject.getString("a");
				msgSender.SENDER.sendGroupMsg(group, sendMsg);
			}
		}
	}

}

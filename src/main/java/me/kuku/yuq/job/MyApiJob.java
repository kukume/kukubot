package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import me.kuku.yuq.entity.QQEntity;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.logic.MyApiLogic;
import me.kuku.yuq.pojo.TwitterPojo;
import me.kuku.yuq.service.QQService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JobCenter
@SuppressWarnings("unused")
public class MyApiJob {

    @Inject
    private MyApiLogic myApiLogic;
    @Inject
    private QQService qqService;
    @Inject
    private ToolLogic toolLogic;

    private final Map<Long, Map<Long, Long>> twitterMap = new HashMap<>();

    @Cron("2m")
    public void twitterJob(){
        List<QQEntity> qqList = qqService.findAll();
        for (QQEntity qqEntity: qqList){
            Long qq = qqEntity.getQq();
            JSONArray twitterJsonArray = qqEntity.getTwitterJsonArray();
            if (twitterJsonArray.size() == 0) continue;
            if (!twitterMap.containsKey(qq)){
                twitterMap.put(qq, new HashMap<>());
            }
            Map<Long, Long> map = twitterMap.get(qq);
            for (Object obj: twitterJsonArray){
                JSONObject jsonObject = (JSONObject) obj;
                Long userId = jsonObject.getLong("id");
                List<TwitterPojo> list;
                try {
                    list = myApiLogic.findTweetsById(userId);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                if (list == null) continue;
                if (map.containsKey(userId)){
                    List<TwitterPojo> newList = new ArrayList<>();
                    for (TwitterPojo twitterPojo : list) {
                        if (twitterPojo.getTweetsId() <= map.get(userId)) break;
                        newList.add(twitterPojo);
                    }
                    for (TwitterPojo twitterPojo : newList) {
                        FunKt.getYuq().getGroups().get(qqEntity.getGroupEntity().getGroup()).get(qq)
                                .sendMessage(FunKt.getMif().text("twitter有新帖了").plus(
                                        "昵称：" + twitterPojo.getName() + "\n" +
                                                "时间：" + twitterPojo.getCreatAt() + "\n" +
                                                "内容：" + twitterPojo.getText()
                                ));
                    }
                }
                map.put(userId, list.get(0).getTweetsId());
            }
        }
    }

}

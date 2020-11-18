package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.QQEntity;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.logic.impl.MyApiLogic;
import me.kuku.yuq.pojo.InstagramPojo;
import me.kuku.yuq.pojo.TwitterPojo;
import me.kuku.yuq.service.QQService;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JobCenter
public class MyApiJob {

    @Inject
    private MyApiLogic myApiLogic;
    @Inject
    private QQService qqService;
    @Inject
    private ToolLogic toolLogic;

    private final Map<Long, Map<Long, Long>> twitterMap = new HashMap<>();
    private final Map<Long, Map<Long, Long>> insMap = new HashMap<>();

    @Cron("1m")
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

    @Cron("1m")
    public void insJob(){
        List<QQEntity> qqList = qqService.findAll();
        for (QQEntity qqEntity: qqList){
            Long qq = qqEntity.getQq();
            JSONArray instagramJsonArray = qqEntity.getInstagramJsonArray();
            if (instagramJsonArray.size() == 0) continue;
            if (!insMap.containsKey(qq)){
                insMap.put(qq, new HashMap<>());
            }
            Map<Long, Long> map = insMap.get(qq);
            for (Object obj: instagramJsonArray){
                JSONObject jsonObject = (JSONObject) obj;
                Long userId = jsonObject.getLong("id");
                List<InstagramPojo> list;
                try {
                    list = myApiLogic.findInsPicById(jsonObject.getString("name"), userId, null);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                if (list == null) continue;
                if (map.containsKey(userId)){
                    List<InstagramPojo> newList = new ArrayList<>();
                    for (InstagramPojo instagramPojo: list){
                        if (instagramPojo.getId() <= map.get(userId)) break;
                        newList.add(instagramPojo);
                    }
                    for (InstagramPojo instagramPojo : newList) {
                        Message message = FunKt.getMif().text("ins有新图片了").toMessage();
                        List<String> picList = instagramPojo.getPicList();
                        picList.forEach(str -> {
                            try {
                                byte[] bytes = toolLogic.piXivPicProxy(str);
                                message.plus(FunKt.getMif().imageByInputStream(new ByteArrayInputStream(bytes)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        FunKt.getYuq().getGroups().get(qqEntity.getGroupEntity().getGroup()).get(qq)
                                .sendMessage(message);
                    }
                }
                map.put(userId, list.get(0).getId());
            }
        }
    }
}

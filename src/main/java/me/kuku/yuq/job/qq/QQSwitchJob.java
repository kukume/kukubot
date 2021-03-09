package me.kuku.yuq.job.qq;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.QQJobEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQLoginLogic;
import me.kuku.yuq.logic.QQZoneLogic;
import me.kuku.yuq.service.QQJobService;
import me.kuku.yuq.service.QQLoginService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JobCenter
@SuppressWarnings("unused")
public class QQSwitchJob {
    @Inject
    private QQJobService qqJobService;
    @Inject
    private QQLoginService qqLoginService;
    @Inject
    private QQLoginLogic qqLoginLogic;
    @Inject
    private QQZoneLogic qqZoneLogic;

    @Cron("4h")
    public void qqSign(){
        List<QQJobEntity> list = qqJobService.findByType("autoSign");
        for (QQJobEntity qqJobEntity : list) {
            try {
                QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qqJobEntity.getQq());
                if (qqLoginEntity == null) continue;
                String str = qqLoginLogic.qqSign(qqLoginEntity);
                if (!str.contains("更新QQ")){
                    qqLoginLogic.anotherSign(qqLoginEntity);
                    qqLoginLogic.vipSign(qqLoginEntity);
                    qqLoginLogic.yellowSign(qqLoginEntity);
                    qqLoginLogic.qqVideoSign1(qqLoginEntity);
                    qqLoginLogic.qqVideoSign2(qqLoginEntity);
                    qqLoginLogic.bigVipSign(qqLoginEntity);
                    qqLoginLogic.qqMusicSign(qqLoginEntity);
                    qqLoginLogic.gameSign(qqLoginEntity);
                    qqLoginLogic.qPetSign(qqLoginEntity);
                    qqLoginLogic.motionSign(qqLoginEntity);
                    qqLoginLogic.blueSign(qqLoginEntity);
                    qqLoginLogic.sVipMornSign(qqLoginEntity);
                    qqLoginLogic.weiYunSign(qqLoginEntity);
                    qqLoginLogic.growthLike(qqLoginEntity);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Cron("1m")
    public void mz(){
        List<QQJobEntity> list = qqJobService.findByType("mz");
        for (QQJobEntity qqJobEntity : list) {
            if (qqJobEntity.getDataJsonObject().getBoolean("status")){
                QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qqJobEntity.getQq());
                if (qqLoginEntity == null) continue;
                if (qqLoginEntity.getStatus()){
                    try {
                        for (Map<String, String> map : qqZoneLogic.friendTalk(qqLoginEntity)) {
                            if (map.get("like") == null || !"1".equals(map.get("like"))){
                                qqZoneLogic.likeTalk(qqLoginEntity, map);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Cron("30s")
    public void bubble(){
        List<QQJobEntity> list = qqJobService.findByType("bubble");
        for (QQJobEntity qqJobEntity : list) {
            if (qqJobEntity.getDataJsonObject().getBoolean("status")){
                QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qqJobEntity.getQq());
                if (qqLoginEntity == null) continue;
                if (qqLoginEntity.getStatus()){
                    try {
                        qqLoginLogic.diyBubble(qqLoginEntity, qqJobEntity.getDataJsonObject().getString("text"), null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private final Map<Long, Map<Long, Long>> forwardMap = new HashMap<>();

    @Cron("1m")
    public void autoForward(){
        List<QQJobEntity> list = qqJobService.findByType("autoForward");
        for (QQJobEntity qqJobEntity: list){
            Long qq = qqJobEntity.getQq();
            QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qq);
            if (!Boolean.valueOf(true).equals(qqLoginEntity.getStatus())) continue;
            if (!forwardMap.containsKey(qq)){
                forwardMap.put(qq, new HashMap<>());
            }
            Map<Long, Long> map = forwardMap.get(qq);
            JSONArray jsonArray = qqJobEntity.getDataJsonObject().getJSONArray("content");
            for (int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Long forwardQQ = jsonObject.getLong("qq");
                String content = jsonObject.getString("content");
                List<Map<String, String>> talkList;
                try {
                    talkList = qqZoneLogic.talkByQQ(qqLoginEntity, forwardQQ);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                if (talkList == null || talkList.size() == 0) continue;
                if (map.containsKey(forwardQQ)){
                    Long oldTime = map.get(forwardQQ);
                    List<Map<String, String>> newList = new ArrayList<>();
                    for (Map<String, String> talkMap : talkList) {
                        if (Long.parseLong(talkMap.get("time")) <= oldTime) break;
                        newList.add(talkMap);
                    }
                    for (Map<String, String> talkMap : newList) {
                        try {
                            qqZoneLogic.forwardTalk(qqLoginEntity, talkMap.get("id"), forwardQQ.toString(), content);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                map.put(forwardQQ, Long.valueOf(talkList.get(0).get("time")));
            }
        }
    }
}

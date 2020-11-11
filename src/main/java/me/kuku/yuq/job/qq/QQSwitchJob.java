package me.kuku.yuq.job.qq;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.QQJobEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQLogic;
import me.kuku.yuq.logic.QQZoneLogic;
import me.kuku.yuq.service.QQJobService;
import me.kuku.yuq.service.QQLoginService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@JobCenter
public class QQSwitchJob {
    @Inject
    private QQJobService qqJobService;
    @Inject
    private QQLoginService qqLoginService;
    @Inject
    private QQLogic qqLogic;
    @Inject
    private QQZoneLogic qqZoneLogic;

    @Cron("4h")
    public void qqSign(){
        List<QQJobEntity> list = qqJobService.findByType("autoSign");
        for (QQJobEntity qqJobEntity : list) {
            try {
                QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(qqJobEntity.getQq());
                if (qqLoginEntity == null) continue;
                String str = qqLogic.qqSign(qqLoginEntity);
                if (!str.contains("更新QQ")){
                    qqLogic.anotherSign(qqLoginEntity);
                    qqLogic.vipSign(qqLoginEntity);
                    qqLogic.phoneGameSign(qqLoginEntity);
                    qqLogic.yellowSign(qqLoginEntity);
                    qqLogic.qqVideoSign1(qqLoginEntity);
                    qqLogic.qqVideoSign2(qqLoginEntity);
                    qqLogic.bigVipSign(qqLoginEntity);
                    qqLogic.qqMusicSign(qqLoginEntity);
                    qqLogic.gameSign(qqLoginEntity);
                    qqLogic.qPetSign(qqLoginEntity);
                    qqLogic.tribeSign(qqLoginEntity);
                    qqLogic.motionSign(qqLoginEntity);
                    qqLogic.blueSign(qqLoginEntity);
                    qqLogic.sVipMornSign(qqLoginEntity);
                    qqLogic.weiYunSign(qqLoginEntity);
                    qqLogic.weiShiSign(qqLoginEntity);
                    qqLogic.growthLike(qqLoginEntity);
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
                        qqLogic.diyBubble(qqLoginEntity, qqJobEntity.getDataJsonObject().getString("text"), null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

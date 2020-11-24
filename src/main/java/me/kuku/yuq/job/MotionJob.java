package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.MotionEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.LeXinMotionLogic;
import me.kuku.yuq.logic.QQLoginLogic;
import me.kuku.yuq.logic.XiaomiMotionLogic;
import me.kuku.yuq.service.MotionService;
import me.kuku.yuq.service.QQLoginService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@JobCenter
public class MotionJob {
    @Inject
    private MotionService motionService;
    @Inject
    private LeXinMotionLogic leXinMotionLogic;
    @Inject
    private XiaomiMotionLogic xiaomiMotionLogic;
    @Inject
    private QQLoginService qqLoginService;
    @Inject
    private QQLoginLogic qqLoginLogic;

    @Cron("At::d::08")
    public void motion(){
        List<MotionEntity> list = motionService.findAll();
        for (MotionEntity motionEntity : list) {
            if (motionEntity.getStep() != null && motionEntity.getStep() != 0){
                if (motionEntity.getLeXinCookie() != null) {
                    try {
                        leXinMotionLogic.modifyStepCount(motionEntity.getStep(), motionEntity);
                        QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(motionEntity.getQq());
                        if (qqLoginEntity != null && qqLoginEntity.getStatus()){
                            qqLoginLogic.motionSign(qqLoginEntity);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (motionEntity.getMiLoginToken() != null){
                    try {
                        xiaomiMotionLogic.changeStep(motionEntity.getMiLoginToken(), motionEntity.getStep());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

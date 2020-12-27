package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.message.Message;
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
@SuppressWarnings("unused")
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

    @SuppressWarnings("DuplicatedCode")
    @Cron("At::d::08:00")
    public void motion(){
        List<MotionEntity> list = motionService.findAll();
        for (MotionEntity motionEntity : list) {
            if (motionEntity.getStep() != null && motionEntity.getStep() != 0){
                if (motionEntity.getLeXinStatus() != null && motionEntity.getLeXinStatus()) {
                    try {
                        String result = leXinMotionLogic.modifyStepCount(motionEntity.getStep(), motionEntity);
                        if (result.contains("成功")) {
                            QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(motionEntity.getQq());
                            if (qqLoginEntity != null && qqLoginEntity.getStatus()) {
                                qqLoginLogic.motionSign(qqLoginEntity);
                            }
                        }else {
                            motionEntity.setLeXinStatus(false);
                            motionService.save(motionEntity);
                            Message message = Message.Companion.toMessage("使用乐心运动修改步数失败，请更新乐心运动！！");
                            if (motionEntity.getGroup() == null){
                                try {
                                    FunKt.getYuq().getFriends().get(motionEntity.getQq())
                                            .sendMessage(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                FunKt.getYuq().getGroups().get(motionEntity.getGroup()).get(motionEntity.getQq())
                                        .sendMessage(message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (motionEntity.getMiStatus() != null && motionEntity.getMiStatus()){
                    try {
                        String result = xiaomiMotionLogic.changeStep(motionEntity.getMiLoginToken(), motionEntity.getStep());
                        if (!result.contains("成功")){
                            motionEntity.setMiStatus(false);
                            motionService.save(motionEntity);
                            Message message = Message.Companion.toMessage("使用小米运动修改步数失败，请更新小米运动！！");
                            if (motionEntity.getGroup() == null){
                                try {
                                    FunKt.getYuq().getFriends().get(motionEntity.getQq())
                                            .sendMessage(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                FunKt.getYuq().getGroups().get(motionEntity.getGroup()).get(motionEntity.getQq())
                                        .sendMessage(message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

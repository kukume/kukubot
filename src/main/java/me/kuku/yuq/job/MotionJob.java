package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.message.Message;
import me.kuku.yuq.entity.StepEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQLoginLogic;
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
        List<StepEntity> list = motionService.findAll();
        for (StepEntity stepEntity : list) {
            if (stepEntity.getStep() != null && stepEntity.getStep() != 0){
                if (stepEntity.getLeXinStatus() != null && stepEntity.getLeXinStatus()) {
                    try {
                        String result = leXinMotionLogic.modifyStepCount(stepEntity.getStep(), stepEntity);
                        if (result.contains("成功")) {
                            QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(stepEntity.getQq());
                            if (qqLoginEntity != null && qqLoginEntity.getStatus()) {
                                qqLoginLogic.motionSign(qqLoginEntity);
                            }
                        }else {
                            stepEntity.setLeXinStatus(false);
                            motionService.save(stepEntity);
                            Message message = Message.Companion.toMessage("使用乐心运动修改步数失败，请更新乐心运动！！");
                            if (stepEntity.getGroup() == null){
                                try {
                                    FunKt.getYuq().getFriends().get(stepEntity.getQq())
                                            .sendMessage(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                FunKt.getYuq().getGroups().get(stepEntity.getGroup()).get(stepEntity.getQq())
                                        .sendMessage(message);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (stepEntity.getMiStatus() != null && stepEntity.getMiStatus()){
                    try {
                        String result = xiaomiMotionLogic.changeStep(stepEntity.getMiLoginToken(), stepEntity.getStep());
                        if (!result.contains("成功")){
                            stepEntity.setMiStatus(false);
                            motionService.save(stepEntity);
                            Message message = Message.Companion.toMessage("使用小米运动修改步数失败，请更新小米运动！！");
                            if (stepEntity.getGroup() == null){
                                try {
                                    FunKt.getYuq().getFriends().get(stepEntity.getQq())
                                            .sendMessage(message);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else {
                                FunKt.getYuq().getGroups().get(stepEntity.getGroup()).get(stepEntity.getQq())
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

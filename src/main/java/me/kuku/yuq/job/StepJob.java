package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import me.kuku.yuq.entity.StepEntity;
import me.kuku.yuq.entity.StepService;
import me.kuku.yuq.logic.StepLogic;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@JobCenter
public class StepJob {

    @Inject
    @Named("leXin")
    private StepLogic leXinStepLogic;
    @Inject
    @Named("xiaomi")
    private StepLogic xiaomiStepLogic;
    @Inject
    private StepService stepService;

    // 6点起床跑步很正常
    @Cron("At::d::06:35:12")
    public void step(){
        List<StepEntity> list = stepService.findAll().stream().filter(it -> it.getStep() > 0).collect(Collectors.toList());
        for (StepEntity stepEntity : list) {
            try {
                int step = stepEntity.getStep();
                if (!"".equals(stepEntity.getLeXinCookie())){
                    leXinStepLogic.modifyStepCount(stepEntity, step);
                }
                if (!"".equals(stepEntity.getMiLoginToken())){
                    xiaomiStepLogic.modifyStepCount(stepEntity, step);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
